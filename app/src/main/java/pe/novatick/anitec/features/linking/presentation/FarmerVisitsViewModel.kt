package pe.novatick.anitec.features.linking.presentation

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*

data class FarmerVisitsState(val items:List<FarmerVisit>?=null,val loading:Boolean=false,val error:AuthFailure?=null)
/** Owned by the authenticated farmer's composition; never retains another session's results. */
class FarmerVisitsViewModel(private val repo:LinkingRepository,private val role:Role,
 dispatcher:CoroutineDispatcher=Dispatchers.Main.immediate,private val now:()->Long=System::currentTimeMillis) {
 private val job=SupervisorJob();private val scope=CoroutineScope(job+dispatcher);private var ticket=0L
 private val mutable=MutableStateFlow(FarmerVisitsState());val state=mutable.asStateFlow()
 fun close(){ticket++;job.cancel();mutable.value=FarmerVisitsState()}
 fun load(){if(!job.isActive)return;val request=++ticket;mutable.value=FarmerVisitsState(loading=true)
  scope.launch{try{
   if(role!=Role.GANADERO)throw AuthFailure(AuthProblem.FORBIDDEN,403)
   val items=repo.upcomingVisits().filter{it.visit.status=="PROGRAMADA" && it.visit.time>now()}.sortedWith(compareBy<FarmerVisit>{it.visit.time}.thenBy{it.visit.id})
   ensureActive();if(request==ticket)mutable.value=FarmerVisitsState(items=items)
  }catch(e:CancellationException){throw e}catch(e:Exception){ensureActive();if(request==ticket)mutable.value=FarmerVisitsState(error=e as? AuthFailure ?: AuthFailure(AuthProblem.INVALID_RESPONSE))}}
 }
}
