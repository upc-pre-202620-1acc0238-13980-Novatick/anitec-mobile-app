package pe.novatick.anitec.features.linking.presentation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*

data class LinkRead<T>(val value: T?=null,val loading: Boolean=false,val error: AuthFailure?=null)
data class LinkingState(
 val links: LinkRead<List<VetLink>> = LinkRead(),val pending: LinkRead<List<PendingInvitation>> = LinkRead(),
 val farmers: LinkRead<List<LinkedFarmer>> = LinkRead(),val capacity: LinkRead<LinkCapacity> = LinkRead(),
 val busy: Boolean=false,val error: AuthFailure?=null,val notice: String?=null,val uncertain: Boolean=false,
 val sent: Invitation?=null)
/** Owned by the authenticated composition; disposal cancels work and removes all account data. */
class LinkingViewModel(private val repo: LinkingRepository,val role: Role,dispatcher: CoroutineDispatcher=Dispatchers.Main.immediate) {
 private val job=SupervisorJob();private val scope=CoroutineScope(job+dispatcher)
 private val mutable=MutableStateFlow(LinkingState());val state=mutable.asStateFlow()
 private var generation=0L
 fun close(){generation++;job.cancel();mutable.value=LinkingState()}
 fun refresh(){
  if(!job.isActive)return
  val g=++generation
  mutable.update {it.copy(links=LinkRead(loading=true),pending=LinkRead(loading=role==Role.VETERINARIO),farmers=LinkRead(loading=role==Role.VETERINARIO),capacity=LinkRead(loading=role==Role.VETERINARIO))}
  fun <T> read(call: suspend ()->T,apply: (LinkingState,LinkRead<T>)->LinkingState){scope.launch {
   val result=try {LinkRead(value=call())} catch(e: CancellationException){throw e} catch(e: AuthFailure){LinkRead<T>(error=e)} catch(_: Exception){LinkRead<T>(error=AuthFailure(AuthProblem.INVALID_RESPONSE))}
   ensureActive();if(g==generation) mutable.update {apply(it,result)}
  }}
  read(repo::links){s,v->s.copy(links=v)}
  if(role==Role.VETERINARIO){read(repo::pending){s,v->s.copy(pending=v)};read(repo::farmers){s,v->s.copy(farmers=v)};read(repo::capacity){s,v->s.copy(capacity=v)}}
 }
 fun clearNotice(){mutable.update {it.copy(error=null,notice=null)}}
 fun invite(email: String,language: String){
  if(role!=Role.GANADERO || !validVetEmail(email) || state.value.sent!=null)return
  mutate(true){val sent=repo.invite(email,language);currentCoroutineContext().ensureActive();mutable.update {it.copy(sent=sent,notice=if(sent.delivery=="SENT") "sent" else "unconfirmed")}}
 }
 // This explicit acknowledgement permits a conscious new operation; never an automatic retry.
 fun acknowledgeUncertain(){if(!state.value.busy)mutable.update {it.copy(uncertain=false,error=null)}}
 fun newInvite(){if(!state.value.busy)mutable.update {it.copy(sent=null,error=null,notice=null)}}
 fun respond(id: String,accept: Boolean){if(role!=Role.VETERINARIO)return;mutate(false){repo.respond(id,accept);currentCoroutineContext().ensureActive();mutable.update {it.copy(notice=if(accept) "accepted" else "rejected")}}}
 fun revoke(id: String){if(role!=Role.GANADERO)return;mutate(false){repo.revoke(id);currentCoroutineContext().ensureActive();mutable.update {it.copy(notice="revoked")}}}
 private fun mutate(invite: Boolean,call: suspend ()->Unit){
  if(!job.isActive || state.value.busy || state.value.uncertain)return
  mutable.update {it.copy(busy=true,error=null,notice=null)}
  scope.launch {
   try {call();ensureActive();refresh()}
   catch(e: CancellationException){throw e}
   catch(e: AuthFailure){ensureActive();mutable.update {it.copy(error=e,uncertain=e.problem in listOf(AuthProblem.NETWORK,AuthProblem.INVALID_RESPONSE) || e.status>=500)};if(!invite) refresh()}
   catch(_: Exception){ensureActive();mutable.update {it.copy(error=AuthFailure(AuthProblem.INVALID_RESPONSE),uncertain=true)}}
   finally {if(job.isActive)mutable.update {it.copy(busy=false)}}
  }
 }
}
