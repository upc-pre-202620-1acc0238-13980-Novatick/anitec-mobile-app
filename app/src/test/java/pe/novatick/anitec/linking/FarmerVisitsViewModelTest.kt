package pe.novatick.anitec.linking
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.linking.presentation.*

@OptIn(ExperimentalCoroutinesApi::class)
class FarmerVisitsViewModelTest {
 private open class Fake:LinkingRepository {
  var items=listOf(row(3),row(1),row(2));var error=false;var calls=0
  override suspend fun upcomingVisits():List<FarmerVisit>{calls++;if(error)throw AuthFailure(AuthProblem.NETWORK);return items}
  override suspend fun links()=emptyList<VetLink>();override suspend fun invite(email:String,language:String):Invitation=error("unused")
  override suspend fun pending()=emptyList<PendingInvitation>();override suspend fun respond(id:String,accept:Boolean):Invitation=error("unused")
  override suspend fun farmers()=emptyList<LinkedFarmer>();override suspend fun capacity():LinkCapacity=error("unused");override suspend fun revoke(id:String){}
 }
 companion object{private fun row(n:Int)=FarmerVisit(HomeVisit("v$n","Nina","Farmer",n*1000L,"PROGRAMADA",null),"AN-001","Vet")}
 @Test fun preservesAllSortedFutureVisitsAndRefreshCanReturnFewer()=runTest{
  val f=Fake();f.items=(5 downTo 1).map{row(it)}+row(0)+row(6).let{it.copy(visit=it.visit.copy(status="CANCELADA"))}
  val vm=FarmerVisitsViewModel(f,Role.GANADERO,StandardTestDispatcher(testScheduler)){500};vm.load();assertTrue(vm.state.value.loading);advanceUntilIdle();assertEquals(listOf("v1","v2","v3","v4","v5"),vm.state.value.items!!.map{it.visit.id})
  f.items=listOf(row(1));vm.load();advanceUntilIdle();assertEquals(1,vm.state.value.items!!.size);vm.close()
 }
 @Test fun failureClearsOldDataAndEmptyIsOnlyShownAfterSuccessfulRead()=runTest{val f=Fake();val vm=FarmerVisitsViewModel(f,Role.GANADERO,StandardTestDispatcher(testScheduler)){0};vm.load();advanceUntilIdle();f.error=true;vm.load();advanceUntilIdle();assertNull(vm.state.value.items);assertNotNull(vm.state.value.error);f.error=false;f.items=emptyList();vm.load();advanceUntilIdle();assertTrue(vm.state.value.items!!.isEmpty());assertNull(vm.state.value.error);vm.close()}
 @Test fun wrongRoleCannotRead()=runTest{val f=Fake();val vm=FarmerVisitsViewModel(f,Role.VETERINARIO,StandardTestDispatcher(testScheduler));vm.load();advanceUntilIdle();assertEquals(0,f.calls);assertEquals(403,vm.state.value.error!!.status);vm.close()}
 @Test fun staleCompletionAndLogoutCannotRestorePreviousAccountData()=runTest{
  val gate=CompletableDeferred<Unit>();val f=object:Fake(){override suspend fun upcomingVisits():List<FarmerVisit>{calls++;return if(calls==1){withContext(NonCancellable){gate.await()};listOf(row(1))}else listOf(row(2))}}
  val vm=FarmerVisitsViewModel(f,Role.GANADERO,StandardTestDispatcher(testScheduler)){0};vm.load();runCurrent();vm.load();runCurrent();assertEquals("v2",vm.state.value.items!!.single().visit.id);vm.close();gate.complete(Unit);advanceUntilIdle();assertNull(vm.state.value.items);vm.load();advanceUntilIdle();assertEquals(2,f.calls)
 }
}
