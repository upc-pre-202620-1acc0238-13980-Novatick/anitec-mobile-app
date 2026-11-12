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
 val sent: Invitation?=null,val farmerContext: String?=null,
 val farms: LinkRead<List<pe.novatick.anitec.features.livestock.domain.Farm>> = LinkRead(),
 val animals: LinkRead<pe.novatick.anitec.features.livestock.domain.Inventory> = LinkRead(),
 val animal: LinkRead<pe.novatick.anitec.features.livestock.domain.AnimalDetail> = LinkRead(),val animalId: String?=null,
 val observations: LinkRead<List<pe.novatick.anitec.features.livestock.domain.AnimalObservation>> = LinkRead(),val observationAnimalId: String?=null)
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
 private var farmTicket=0L;private var animalListTicket=0L;private var animalTicket=0L;private var observationTicket=0L
 private fun context(id: String){if(state.value.farmerContext!=id){farmTicket++;animalListTicket++;animalTicket++;observationTicket++;mutable.update {it.copy(farmerContext=id,farms=LinkRead(),animals=LinkRead(),animal=LinkRead(),animalId=null,observations=LinkRead(),observationAnimalId=null)}}}
 private fun <T> browse(call: suspend ()->T,apply: (LinkRead<T>)->Unit){if(role!=Role.VETERINARIO || !job.isActive)return;scope.launch {
  val value=try{LinkRead(value=call())}catch(e: CancellationException){throw e}catch(e: AuthFailure){LinkRead<T>(error=e)}catch(_:Exception){LinkRead<T>(error=AuthFailure(AuthProblem.INVALID_RESPONSE))}
  ensureActive();apply(value)
 }}
 fun readFarms(id: String){if(role!=Role.VETERINARIO || !job.isActive)return;context(id);val ticket=++farmTicket;mutable.update {it.copy(farms=LinkRead(loading=true))};browse({repo.farms(id)}){value->if(ticket==farmTicket && state.value.farmerContext==id)mutable.update {it.copy(farms=value)}}}
 fun readAnimals(id: String){if(role!=Role.VETERINARIO || !job.isActive)return;context(id);val ticket=++animalListTicket;mutable.update {it.copy(animals=LinkRead(loading=true),animal=LinkRead(),animalId=null)};browse({repo.animals(id)}){value->if(ticket==animalListTicket && state.value.farmerContext==id)mutable.update {it.copy(animals=value)}}}
 fun readAnimal(farmer: String,id: String){if(role!=Role.VETERINARIO || !job.isActive)return;context(farmer);val ticket=++animalTicket;mutable.update {it.copy(animal=LinkRead(loading=true),animalId=id)};browse({repo.animal(farmer,id)}){value->if(ticket==animalTicket && state.value.farmerContext==farmer && state.value.animalId==id)mutable.update {it.copy(animal=value)}}}
 fun clearNotice(){mutable.update {it.copy(error=null,notice=null)}}
 fun consumeNotice(expected: String){mutable.update {if(it.notice==expected)it.copy(notice=null) else it}}
 fun readObservations(farmer: String,id: String){
  if(role!=Role.VETERINARIO || !job.isActive)return
  context(farmer);val ticket=++observationTicket
  mutable.update {it.copy(observations=LinkRead(loading=true),observationAnimalId=id)}
  browse({repo.observations(farmer,id)}){value->if(ticket==observationTicket && state.value.farmerContext==farmer && state.value.observationAnimalId==id)mutable.update {it.copy(observations=value)}}
 }
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
