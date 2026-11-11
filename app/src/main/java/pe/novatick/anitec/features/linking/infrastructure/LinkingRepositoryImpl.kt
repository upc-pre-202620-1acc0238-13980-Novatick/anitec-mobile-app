package pe.novatick.anitec.features.linking.infrastructure
import kotlinx.coroutines.CancellationException
import pe.novatick.anitec.core.network.AuthorizedRequests
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*
import retrofit2.Response
import java.util.UUID
import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Locale

class LinkingRepositoryImpl(private val api: LinkingApi,private val requests: AuthorizedRequests,private val profile: ()->Profile?) : LinkingRepository {
 private fun invalid(): Nothing=throw AuthFailure(AuthProblem.INVALID_RESPONSE)
 private fun id(s: String?): String=s?.takeIf {runCatching {UUID.fromString(it).toString()==it.lowercase()}.getOrDefault(false)} ?: invalid()
 private fun text(s: String?)=s?.takeIf(String::isNotBlank) ?: invalid()
 private fun count(n: Long?)=n?.takeIf {it>=0} ?: invalid()
 private fun date(s: String?,civil: Boolean=false): String=text(s).also {
  val raw=if(civil) it else it.replace(Regex("\\.\\d+Z$"),"Z")
  val parser=SimpleDateFormat(if(civil) "yyyy-MM-dd" else "yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).apply {isLenient=false}
  val pos=ParsePosition(0);if(parser.parse(raw,pos)==null || pos.index!=raw.length) invalid()
 }
 private fun role(expected: Role): Profile=(profile() ?: throw AuthFailure(AuthProblem.RELOGIN)).also {if(it.role!=expected) throw AuthFailure(AuthProblem.FORBIDDEN,403)}
 private suspend fun <T> mapped(block: suspend ()->T): T=try {block()} catch(e: CancellationException){throw e} catch(e: AuthFailure){throw e} catch(_: Exception){invalid()}
 private fun <T> expected(r: Response<T>,code: Int=200)=r.also {if(it.isSuccessful && it.code()!=code) invalid()}
 private fun invitation(d: InvitationDto,expectedStatus: String)=Invitation(id(d.invitationId),text(d.vetEmail),text(d.status).also {if(it!=expectedStatus) invalid()},text(d.emailDelivery).also {if(it !in listOf("SENT","UNCONFIRMED")) invalid()},date(d.sentAt))
 override suspend fun links()=mapped {
  val me=profile() ?: throw AuthFailure(AuthProblem.RELOGIN)
  val d=requests.execute {expected(api.links(it))};val list=d.links ?: invalid()
  if(count(d.activeLinksCount)!=list.size.toLong()) invalid()
  list.map {if(it.status!="ACTIVA") invalid();val farmer=id(it.farmerId);val vet=id(it.vetId)
   if((me.role==Role.GANADERO && farmer!=me.id)||(me.role==Role.VETERINARIO && vet!=me.id)) invalid()
   VetLink(id(it.linkId),farmer,text(it.farmerName),vet,text(it.vetName),date(it.linkedAt))
  }.also {if(it.map {v->v.id}.distinct().size!=it.size) invalid()}
 }
 override suspend fun invite(email: String,language: String)=mapped {
  role(Role.GANADERO);if(!validVetEmail(email)) throw AuthFailure(AuthProblem.SERVER,400,"VALIDATION_ERROR")
  invitation(requests.execute {expected(api.invite(it,if(language=="en") "en" else "es",InviteRequest(email.trim())),201)},"PENDIENTE")
 }
 override suspend fun pending()=mapped {
  role(Role.VETERINARIO);requests.execute {expected(api.pending(it))}.map {PendingInvitation(id(it.invitationId),text(it.farmerName),date(it.sentDate,true))}
   .also {if(it.map {v->v.id}.distinct().size!=it.size) invalid()}
 }
 override suspend fun respond(id: String,accept: Boolean)=mapped {
  role(Role.VETERINARIO);val target=this.id(id)
  invitation(requests.execute {expected(api.respond(it,target,RespondRequest(if(accept) "ACCEPT" else "REJECT")))},if(accept) "ACEPTADA" else "RECHAZADA").also {if(it.id!=target) invalid()}
 }
 override suspend fun farmers()=mapped {
  role(Role.VETERINARIO);val d=requests.execute {expected(api.farmers(it))};val list=d.farmers ?: invalid()
  if(count(d.activeLinksCount)!=list.size.toLong()) invalid()
  list.map {LinkedFarmer(id(it.farmerId),text(it.farmerName),count(it.animalsCount),(it.farms ?: invalid()).map {f->LinkedFarm(id(f.farmId),text(f.farmName),count(f.animalsCount))})}
   .also {if(it.map {f->f.id}.distinct().size!=it.size) invalid()}
 }
 override suspend fun capacity()=mapped {
  val me=role(Role.VETERINARIO);val d=requests.execute {expected(api.capacity(it))}
  LinkCapacity(id(d.vetId).also {if(it!=me.id) invalid()},d.allowedRanchers?.takeIf {it>=0} ?: invalid(),d.activeLinks?.takeIf {it>=0} ?: invalid(),count(d.lastPlanRevision))
 }
 override suspend fun revoke(id: String)=mapped {
  role(Role.GANADERO);val target=this.id(id);val d=requests.execute {expected(api.revoke(it,target))}
  if(this.id(d.linkId)!=target || d.status!="REVOCADA") invalid();date(d.revokedAt);Unit
 }
}
