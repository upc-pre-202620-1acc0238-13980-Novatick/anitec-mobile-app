package pe.novatick.anitec.features.linking.presentation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.livestock.domain.AnimalSummary
import java.util.TimeZone

data class VisitFormState(val owner:String?=null,val animal:String?=null,val fixed:Boolean=false,
 val farmers:LinkRead<List<LinkedFarmer>> = LinkRead(),val animals:LinkRead<List<AnimalSummary>> = LinkRead(),
 val reason:String="",val date:String="",val time:String="",val attempted:Boolean=false,val busy:Boolean=false,
 val uncertain:Boolean=false,val error:AuthFailure?=null,val confirmed:String?=null,val checked:Boolean=false)
data class VisitsState(val day:VisitDay=VisitDay.current(),val agenda:LinkRead<List<HomeVisit>> = LinkRead(),
 val detail:HomeVisit?=null,val detailLoading:Boolean=false,val detailError:AuthFailure?=null,val form:VisitFormState=VisitFormState(),val revision:Long=0)
/** Authenticated composition owns this state; cancellation never preserves a previous user's visit data. */
class VisitsViewModel(private val repo:LinkingRepository,private val role:Role,dispatcher:CoroutineDispatcher=Dispatchers.Main.immediate,
 private val now:()->Long=System::currentTimeMillis,private val zone:()->TimeZone=TimeZone::getDefault) {
 private val job=SupervisorJob();private val scope=CoroutineScope(job+dispatcher)
 private val mutable=MutableStateFlow(VisitsState());val state=mutable.asStateFlow()
 private var agendaTicket=0L;private var formTicket=0L;private var animalTicket=0L;private var detailTicket=0L
 fun close(){job.cancel();mutable.value=VisitsState()}
 private fun allowed()=job.isActive && role==Role.VETERINARIO
 fun focusDay(day:VisitDay){if(allowed()){agendaTicket++;mutable.update{it.copy(day=day,agenda=LinkRead())}}}
 private suspend fun <T> read(call:suspend()->T):LinkRead<T> = try{LinkRead(value=call())}catch(e:CancellationException){throw e}catch(e:AuthFailure){LinkRead(error=e)}catch(_:Exception){LinkRead(error=AuthFailure(AuthProblem.INVALID_RESPONSE))}
 fun load(day:VisitDay=state.value.day){if(!allowed())return;val ticket=++agendaTicket;mutable.update{it.copy(day=day,agenda=LinkRead(loading=true))};scope.launch{
  val result=read{repo.todayVisits(day)};ensureActive();if(ticket==agendaTicket)mutable.update{it.copy(agenda=result)}
 }}
 fun select(visit:HomeVisit){if(!allowed())return;detailTicket++;mutable.update{it.copy(detail=visit,detailError=null,detailLoading=false)}}
 fun loadDetail(id:String,day:VisitDay){if(!allowed())return;val ticket=++detailTicket
  mutable.update{it.copy(detail=null,detailLoading=true,detailError=null)};scope.launch{val result=read{repo.todayVisits(day)};ensureActive();if(ticket==detailTicket)mutable.update{
   it.copy(detail=result.value?.firstOrNull{v->v.id==id},detailLoading=false,detailError=result.error ?: if(result.value?.none{v->v.id==id}==true)AuthFailure(AuthProblem.SERVER,404) else null)
  }}
 }
 fun begin(owner:String?=null,animal:String?=null){if(!allowed() || state.value.form.busy || state.value.form.uncertain)return
  val ticket=++formTicket;animalTicket++;mutable.update{it.copy(form=VisitFormState(owner=owner,animal=animal,fixed=owner!=null && animal!=null,farmers=LinkRead(loading=true)))}
  scope.launch{val result=read{repo.farmers()};ensureActive();if(ticket==formTicket){mutable.update{it.copy(form=it.form.copy(farmers=result))};if(owner!=null)chooseOwner(owner,animal)}}
 }
 fun chooseOwner(owner:String,selectedAnimal:String?=null){if(!allowed() || state.value.form.busy || state.value.form.uncertain)return
  val current=state.value.form;if(current.farmers.value?.none{it.id==owner}!=false)return
  val form=formTicket;val ticket=++animalTicket
  mutable.update{it.copy(form=it.form.copy(owner=owner,animal=selectedAnimal,animals=LinkRead(loading=true),error=null))}
  scope.launch{val result=read{repo.animals(owner).content};ensureActive();if(form==formTicket && ticket==animalTicket)mutable.update{
   val chosen=if(current.fixed)selectedAnimal else selectedAnimal?.takeIf{id->result.value?.any{a->a.id==id}==true}
   it.copy(form=it.form.copy(animals=result,animal=chosen))
  }}
 }
 fun chooseAnimal(id:String){edit{if(it.animals.value?.any{a->a.id==id}==true)it.copy(animal=id) else it}}
 private fun edit(change:(VisitFormState)->VisitFormState){if(allowed())mutable.update{if(it.form.busy || it.form.uncertain)it else it.copy(form=change(it.form).copy(error=null,confirmed=null))}}
 fun reason(value:String)=edit{it.copy(reason=value)}
 fun date(value:String)=edit{it.copy(date=value)}
 fun time(value:String)=edit{it.copy(time=value)}
 fun canSave(f:VisitFormState=state.value.form)=visitInputValid(f.owner,f.animal,f.reason,f.date,f.time,now(),zone()) && f.animals.value?.any{it.id==f.animal}==true
 fun save(){if(!allowed())return;val f=state.value.form;if(f.busy || f.uncertain || f.confirmed!=null)return
  mutable.update{it.copy(form=it.form.copy(attempted=true))};if(!canSave(f))return
  val form=formTicket;val millis=visitLocalMillis(f.date,f.time,zone())!!;val utc=VisitDay.current(millis,zone()).utc(millis)
  mutable.update{it.copy(form=it.form.copy(busy=true,error=null))};scope.launch{
   try{val id=repo.scheduleVisit(f.owner!!,f.animal!!,utc,f.reason);ensureActive();if(form==formTicket){mutable.update{it.copy(form=it.form.copy(busy=false,confirmed=id),revision=it.revision+1)};load(VisitDay.current(millis,zone()))}}
   catch(e:CancellationException){throw e}
   catch(e:Exception){ensureActive();val error=e as? AuthFailure ?: AuthFailure(AuthProblem.INVALID_RESPONSE);if(form==formTicket)mutable.update{
    it.copy(form=it.form.copy(busy=false,error=error,uncertain=error.problem in listOf(AuthProblem.NETWORK,AuthProblem.INVALID_RESPONSE) || error.status>=500,checked=false))
   }}
  }
 }
 fun checkUncertain(){val f=state.value.form;if(!f.uncertain || f.busy || !allowed())return;val millis=visitLocalMillis(f.date,f.time,zone()) ?: return
  mutable.update{it.copy(form=it.form.copy(busy=true,checked=false))};val ticket=formTicket
  scope.launch{val result=read{repo.todayVisits(VisitDay.current(millis,zone()))};ensureActive();if(ticket==formTicket)mutable.update{
   it.copy(day=VisitDay.current(millis,zone()),agenda=result,form=it.form.copy(busy=false,checked=result.value!=null && result.error==null))
  }}
 }
 fun acknowledgeUncertain(){if(allowed() && state.value.form.checked && !state.value.form.busy)mutable.update{it.copy(form=it.form.copy(uncertain=false,error=null,checked=false))}}
 fun dismiss():Boolean{if(state.value.form.busy || state.value.form.uncertain)return false;formTicket++;animalTicket++;mutable.update{it.copy(form=VisitFormState())};return true}
}
