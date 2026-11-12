package pe.novatick.anitec.features.linking.infrastructure
import kotlinx.coroutines.CancellationException
import pe.novatick.anitec.core.network.AuthorizedRequests
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*
import retrofit2.Response
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.features.livestock.infrastructure.LivestockApi
import java.util.UUID
import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Locale

class LinkingRepositoryImpl(private val api: LinkingApi,private val requests: AuthorizedRequests,private val livestock: LivestockApi?=null,private val profile: ()->Profile?) : LinkingRepository {
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
 override suspend fun todayVisits(day: VisitDay)=mapped {
  role(Role.VETERINARIO)
  val result=requests.execute {expected(api.visits(it,day.utc(day.start),day.utc(day.end)))}.map {d->
   id(d.animalId);id(d.farmerId)
   if(d.type !in listOf("VISITA","CONTROL") || d.status !in listOf("PROGRAMADA","COMPLETADA","CANCELADA"))invalid()
   val raw=date(d.scheduledAt).replace(Regex("\\.\\d+Z$"),"Z")
   val instant=SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).apply {timeZone=java.util.TimeZone.getTimeZone("UTC")}.parse(raw)!!.time
   HomeVisit(id(d.id),d.animalName,text(d.farmerName),instant,d.status!!,d.reason)
  }
  if(result.map {it.id}.distinct().size!=result.size)invalid()
  result.filter {day.contains(it.time)}.sortedBy {it.time}
 }
 override suspend fun observations(farmerId: String,animalId: String)=mapped {
  animal(farmerId,animalId) // Fresh link and selected-farmer animal scope before reading history.
  val wire=livestock ?: invalid()
  requests.execute {expected(wire.observations(it,id(animalId)))}.map {d->
   AnimalObservation(id(d.id),text(d.text),d.authorName?.takeIf(String::isNotBlank),d.date?.let {date(it,true)})
  }.also {if(it.map {o->o.id}.distinct().size!=it.size) invalid()}
 }
 // Reuse Livestock wire DTOs, but keep veterinarian scope separate from the owner's mutation repository.
 private suspend fun allowedFarmer(farmerId: String): LinkedFarmer {
  role(Role.VETERINARIO);val target=id(farmerId)
  return farmers().firstOrNull {it.id==target} ?: throw AuthFailure(AuthProblem.FORBIDDEN,403)
 }
 override suspend fun farms(farmerId: String)=mapped {
  val owner=allowedFarmer(farmerId);val wire=livestock ?: invalid()
  val result=requests.execute {expected(wire.farms(it))}.onEach {id(it.ownerId);id(it.id)}.filter {it.ownerId==owner.id}.map {
   val farm=id(it.id);if(owner.farms.none {f->f.id==farm}) invalid()
   Farm(farm,text(it.name),it.location,owner.id,date(it.createdAt))
  }
  if(result.map {it.id}.distinct().size!=result.size) invalid()
  result
 }
 override suspend fun animals(farmerId: String)=mapped {
  val owner=allowedFarmer(farmerId);val wire=livestock ?: invalid()
  // The global totalActive includes ALL linked owners. Sum scoped farm totals instead.
  var active=0L;val animals=mutableListOf<AnimalSummary>()
  for(farm in owner.farms){
   val result=requests.execute {expected(wire.farmAnimals(it,farm.id,null,null,null))};active=Math.addExact(active,count(result.totalActive))
   (result.content ?: invalid()).forEach {a->
    if(a.ownerId!=owner.id || a.farmId!=farm.id) invalid()
    animals+=AnimalSummary(id(a.id),text(a.code),a.name,text(a.species),id(a.speciesId),AnimalStatus.entries.firstOrNull {it.name==a.status} ?: invalid(),id(a.farmId),id(a.ownerId),count(a.version))
   }
  }
  if(animals.map {it.id}.distinct().size!=animals.size) invalid()
  Inventory(active,animals)
 }
 override suspend fun animal(farmerId: String,animalId: String)=mapped {
  val owner=allowedFarmer(farmerId);val wire=livestock ?: invalid();val target=id(animalId)
  val d=requests.execute {expected(wire.animal(it,target))};val farm=id(d.farmId)
  if(id(d.id)!=target || owner.farms.none {it.id==farm}) throw AuthFailure(AuthProblem.FORBIDDEN,403)
  val species=id(d.speciesId)
  d.birthDate?.let {if("birthDate" in animalInputErrors(NewAnimal(farm,species,AnimalSex.DESCONOCIDO,birthDate=it))) invalid()}
  AnimalDetail(target,text(d.code),d.name,text(d.species),species,AnimalSex.entries.firstOrNull {it.name==d.sex} ?: invalid(),d.breed,d.birthDate,
   AnimalStatus.entries.firstOrNull {it.name==d.status} ?: invalid(),farm,text(d.farmName),count(d.version),d.photoUrl)
 }

}
