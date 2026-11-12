package pe.novatick.anitec.features.linking.presentation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.LinkingRepository
import pe.novatick.anitec.features.livestock.domain.*
import java.text.SimpleDateFormat
import java.util.*

data class BasicCareState(val animalId:String="",val farmerId:String="",val title:String="",val description:String="",val date:String="",
 val attempted:Boolean=false,val busy:Boolean=false,val uncertain:Boolean=false,val error:AuthFailure?=null,val saved:BasicCare?=null,
 val detail:BasicCare?=null,val reading:Boolean=false,val readError:AuthFailure?=null)
class BasicCareViewModel(private val repo:LinkingRepository,private val role:Role,dispatcher:CoroutineDispatcher=Dispatchers.Main.immediate){
 private val job=SupervisorJob();private val scope=CoroutineScope(job+dispatcher);private var formTicket=0L;private var readTicket=0L
 private val mutable=MutableStateFlow(BasicCareState());val state=mutable.asStateFlow()
 fun close(){job.cancel();mutable.value=BasicCareState()}
 fun begin(farmer:String,animal:String){if(!job.isActive || state.value.busy || state.value.uncertain)return;formTicket++;mutable.value=BasicCareState(animalId=animal,farmerId=farmer,date=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date()))}
 private fun edit(change:(BasicCareState)->BasicCareState){if(job.isActive && !state.value.busy && !state.value.uncertain)mutable.update{change(it).copy(error=null)}}
 fun title(value:String)=edit{it.copy(title=value)};fun description(value:String)=edit{it.copy(description=value)};fun date(value:String)=edit{it.copy(date=value)}
 fun save(){val s=state.value;if(!job.isActive || s.busy || s.uncertain || s.saved!=null)return
  mutable.update{it.copy(attempted=true)};if(role!=Role.VETERINARIO){mutable.update{it.copy(error=AuthFailure(AuthProblem.FORBIDDEN,403))};return}
  val input=NewBasicCare(s.animalId,s.title,s.description,s.date);if(!careInputValid(input))return
  val ticket=formTicket;mutable.update{it.copy(busy=true,error=null)};scope.launch{
   try{val result=repo.createCare(s.farmerId,input);ensureActive();if(ticket==formTicket)mutable.update{it.copy(busy=false,saved=result)}}
   catch(e:CancellationException){throw e}catch(e:Exception){ensureActive();if(ticket==formTicket){val error=e as? AuthFailure ?: AuthFailure(AuthProblem.INVALID_RESPONSE);mutable.update{it.copy(busy=false,error=error,uncertain=error.problem in listOf(AuthProblem.NETWORK,AuthProblem.INVALID_RESPONSE) || error.status>=500)}}}
  }
 }
 fun consumeSaved(){mutable.update{it.copy(saved=null)}}
 fun read(id:String,animal:String){if(!job.isActive)return;val ticket=++readTicket;mutable.update{it.copy(detail=null,reading=true,readError=null)};scope.launch{
  try{val result=repo.care(id,animal);ensureActive();if(ticket==readTicket)mutable.update{it.copy(detail=result,reading=false)}}
  catch(e:CancellationException){throw e}catch(e:Exception){ensureActive();if(ticket==readTicket)mutable.update{it.copy(reading=false,readError=e as? AuthFailure ?: AuthFailure(AuthProblem.INVALID_RESPONSE))}}
 }}
}
