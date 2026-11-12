package pe.novatick.anitec.linking
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.linking.presentation.*
import pe.novatick.anitec.features.livestock.domain.*

@OptIn(ExperimentalCoroutinesApi::class)
class BasicCareViewModelTest {
 private open class Fake:LinkingRepository {
  var calls=0;var fail=false;var gate:CompletableDeferred<Unit>?=null
  override suspend fun createCare(farmerId:String,input:NewBasicCare):BasicCare{calls++;gate?.await();if(fail)throw AuthFailure(AuthProblem.NETWORK);return BasicCare("care",input.animalId,input.title,input.description,input.date,"vet","Vet")}
  override suspend fun care(id:String,animalId:String):BasicCare{if(fail)throw AuthFailure(AuthProblem.FORBIDDEN,403);return BasicCare(id,animalId,"Control","Description","2020-01-01","vet","Vet")}
  override suspend fun links()=emptyList<VetLink>();override suspend fun invite(email:String,language:String):Invitation=error("unused");override suspend fun pending()=emptyList<PendingInvitation>();override suspend fun respond(id:String,accept:Boolean):Invitation=error("unused");override suspend fun farmers()=emptyList<LinkedFarmer>();override suspend fun capacity():LinkCapacity=error("unused");override suspend fun revoke(id:String){}
 }
 private fun fill(vm:BasicCareViewModel){vm.begin("owner","60000000-0000-4000-8000-000000000001");vm.title("Control");vm.description("Description");vm.date("2020-02-29")}
 @Test fun validatesCivilDatesRequiredFieldsAnd150CharacterBoundary(){val input=NewBasicCare("60000000-0000-4000-8000-000000000001","x".repeat(150),"Description","2020-02-29");assertTrue(careInputValid(input,"2020-02-29"));assertFalse(careInputValid(input.copy(title="x".repeat(151)),"2020-02-29"));assertFalse(careInputValid(input.copy(description=" "),"2020-02-29"));assertFalse(careInputValid(input.copy(date="2021-02-29"),"2022-01-01"));assertFalse(careInputValid(input,"2020-02-28"))}
 @Test fun doubleTapConfirmsOnlyAfterSingleWrite()=runTest{val f=Fake();f.gate=CompletableDeferred();val vm=BasicCareViewModel(f,Role.VETERINARIO,StandardTestDispatcher(testScheduler));fill(vm);vm.save();vm.save();runCurrent();assertEquals(1,f.calls);assertNull(vm.state.value.saved);f.gate!!.complete(Unit);advanceUntilIdle();assertEquals("care",vm.state.value.saved!!.id);vm.close()}
 @Test fun uncertainWriteStaysLockedWithoutAutomaticRetry()=runTest{val f=Fake();f.fail=true;val vm=BasicCareViewModel(f,Role.VETERINARIO,StandardTestDispatcher(testScheduler));fill(vm);vm.save();advanceUntilIdle();assertTrue(vm.state.value.uncertain);vm.save();vm.begin("owner","other");advanceUntilIdle();assertEquals(1,f.calls);assertNull(vm.state.value.saved);vm.close()}
 @Test fun ownerCannotSaveButCanReadAndFailedRefreshClearsDetail()=runTest{val f=Fake();val vm=BasicCareViewModel(f,Role.GANADERO,StandardTestDispatcher(testScheduler));fill(vm);vm.save();advanceUntilIdle();assertEquals(0,f.calls);assertEquals(403,vm.state.value.error!!.status);vm.read("care","animal");advanceUntilIdle();assertNotNull(vm.state.value.detail);f.fail=true;vm.read("care","animal");advanceUntilIdle();assertNull(vm.state.value.detail);assertEquals(403,vm.state.value.readError!!.status);vm.close()}
 @Test fun lateCompletionAfterLogoutCannotRestoreData()=runTest{val f=Fake();f.gate=CompletableDeferred();val vm=BasicCareViewModel(f,Role.VETERINARIO,StandardTestDispatcher(testScheduler));fill(vm);vm.save();runCurrent();vm.close();f.gate!!.complete(Unit);advanceUntilIdle();assertNull(vm.state.value.saved);assertEquals("",vm.state.value.animalId)}
}
