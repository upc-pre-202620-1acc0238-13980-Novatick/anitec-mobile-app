package pe.novatick.anitec.linking
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.linking.presentation.*
@OptIn(ExperimentalCoroutinesApi::class)
class LinkingViewModelTest {
 private open class Fake:LinkingRepository {
  var calls=0;var gate:CompletableDeferred<Unit>?=null
  override suspend fun links()=emptyList<VetLink>()
  override suspend fun pending()=emptyList<PendingInvitation>()
  override suspend fun farmers()=emptyList<LinkedFarmer>()
  override suspend fun capacity()=LinkCapacity("vet",3,0,0)
  override suspend fun invite(email:String,language:String):Invitation {calls++;gate?.await();return Invitation("i",email,"PENDIENTE","UNCONFIRMED","2026-11-11T12:00:00Z")}
  override suspend fun respond(id:String,accept:Boolean)=Invitation(id,"email","ACEPTADA","UNCONFIRMED","date")
  override suspend fun revoke(id:String){}
 }
 @Test fun doubleTapDispatchesOnceAndOnlyServerConfirmationIsSuccess()=runTest {
  val f=Fake();f.gate=CompletableDeferred();val vm=LinkingViewModel(f,Role.GANADERO,StandardTestDispatcher(testScheduler));vm.invite("a@b.invalid","es");vm.invite("a@b.invalid","es");runCurrent();assertEquals(1,f.calls);assertNull(vm.state.value.sent);f.gate!!.complete(Unit);advanceUntilIdle();assertEquals("unconfirmed",vm.state.value.notice);assertTrue(vm.state.value.links.value!!.isEmpty());vm.close()
 }
 @Test fun uncertainPostBlocksAnotherAttemptAndDoesNotPretendSaved()=runTest {
  val f=object:Fake(){override suspend fun invite(email:String,language:String):Invitation{calls++;throw AuthFailure(AuthProblem.NETWORK)}}
  val vm=LinkingViewModel(f,Role.GANADERO,StandardTestDispatcher(testScheduler));vm.invite("a@b.invalid","es");advanceUntilIdle();vm.invite("a@b.invalid","es");advanceUntilIdle();assertEquals(1,f.calls);assertTrue(vm.state.value.uncertain);assertNull(vm.state.value.sent);vm.close()
 }
 @Test fun queriesAreIndependentAndFailureIsNotEmpty()=runTest {
  val f=object:Fake(){override suspend fun pending():List<PendingInvitation>{throw AuthFailure(AuthProblem.FORBIDDEN,403)}}
  val vm=LinkingViewModel(f,Role.VETERINARIO,StandardTestDispatcher(testScheduler));vm.refresh();advanceUntilIdle();assertNull(vm.state.value.pending.value);assertEquals(403,vm.state.value.pending.error!!.status);assertEquals(3,vm.state.value.capacity.value!!.allowed);vm.close()
 }
 @Test fun disposalErasesDataEvenWhenLateResponseIgnoresCancellation()=runTest {
  val f=object:Fake(){override suspend fun invite(email:String,language:String):Invitation {withContext(NonCancellable){gate?.await()};return super.invite(email,language)}}
  f.gate=CompletableDeferred();val vm=LinkingViewModel(f,Role.GANADERO,StandardTestDispatcher(testScheduler));vm.invite("a@b.invalid","es");runCurrent();vm.close();f.gate!!.complete(Unit);advanceUntilIdle();assertEquals(LinkingState(),vm.state.value)
 }
 @Test fun refreshClearsPreviouslyAuthorizedDetailWhenAccessFails()=runTest {
  var denied=false
  val f=object:Fake(){override suspend fun farmers():List<LinkedFarmer>{if(denied)throw AuthFailure(AuthProblem.FORBIDDEN,403);return listOf(LinkedFarmer("f","Farmer",1,emptyList()))}}
  val vm=LinkingViewModel(f,Role.VETERINARIO,StandardTestDispatcher(testScheduler));vm.refresh();advanceUntilIdle();assertNotNull(vm.state.value.farmers.value);denied=true;vm.refresh();advanceUntilIdle();assertNull(vm.state.value.farmers.value);vm.close()
 }
 @Test fun farmerNeverQueriesVetOnlyResources()=runTest {
  val f=object:Fake(){override suspend fun pending():List<PendingInvitation>{error("Wrong role")};override suspend fun farmers():List<LinkedFarmer>{error("Wrong role")};override suspend fun capacity():LinkCapacity{error("Wrong role")}}
  val vm=LinkingViewModel(f,Role.GANADERO,StandardTestDispatcher(testScheduler));vm.refresh();advanceUntilIdle();assertNull(vm.state.value.pending.error);assertNotNull(vm.state.value.links.value);vm.close()
 }
}
