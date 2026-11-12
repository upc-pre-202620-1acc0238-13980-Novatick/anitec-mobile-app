package pe.novatick.anitec.linking
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.linking.presentation.*
import pe.novatick.anitec.features.livestock.domain.*
import java.util.TimeZone
@OptIn(ExperimentalCoroutinesApi::class)
class VisitsViewModelTest {
 private open class Fake:LinkingRepository {
  var calls=0;var gate:CompletableDeferred<Unit>?=null;var fail=false;var denied=false
  val visits=mutableListOf<HomeVisit>()
  override suspend fun links()=emptyList<VetLink>();override suspend fun pending()=emptyList<PendingInvitation>()
  override suspend fun farmers()=listOf(LinkedFarmer("owner","Owner",1,emptyList()),LinkedFarmer("other","Other",1,emptyList()))
  override suspend fun capacity()=LinkCapacity("vet",3,1,0)
  override suspend fun invite(email:String,language:String):Invitation=error("Not used");override suspend fun respond(id:String,accept:Boolean):Invitation=error("Not used");override suspend fun revoke(id:String){}
  override suspend fun animals(farmerId:String)=Inventory(1,listOf(AnimalSummary("animal-$farmerId","QA",null,"Bovino","species",AnimalStatus.ACTIVO,"farm",farmerId,0)))
  override suspend fun todayVisits(day:VisitDay):List<HomeVisit>{if(denied)throw AuthFailure(AuthProblem.FORBIDDEN,403);return visits.filter{day.contains(it.time)}}
  override suspend fun scheduleVisit(farmerId:String,animalId:String,scheduledAt:String,reason:String):String{calls++;gate?.await();if(fail)throw AuthFailure(AuthProblem.NETWORK);visits+=HomeVisit("new",null,"Owner",visitLocalMillis("2099-01-02","10:30",TimeZone.getTimeZone("UTC"))!!,"PROGRAMADA",reason,animalId,farmerId);return "new"}
 }
 private fun vm(f:Fake,d:CoroutineDispatcher)=VisitsViewModel(f,Role.VETERINARIO,d,{0L},{TimeZone.getTimeZone("UTC")})
 private fun input(vm:VisitsViewModel){vm.reason("Control");vm.date("2099-01-02");vm.time("10:30")}
 @Test fun doubleTapSendsOnceAndUpdatesAgendaOnlyAfterConfirmation()=runTest{
  val f=Fake();f.gate=CompletableDeferred();val vm=vm(f,StandardTestDispatcher(testScheduler));vm.begin("owner","animal-owner");advanceUntilIdle();input(vm);vm.save();vm.save();runCurrent();assertEquals(1,f.calls);assertNull(vm.state.value.form.confirmed);assertFalse(vm.dismiss());f.gate!!.complete(Unit);advanceUntilIdle();assertEquals("new",vm.state.value.form.confirmed);assertEquals("new",vm.state.value.agenda.value!!.single().id);assertEquals(1L,vm.state.value.revision);vm.close()
 }
 @Test fun changingFarmerClearsPreviouslySelectedAnimal()=runTest{val f=Fake();val vm=vm(f,StandardTestDispatcher(testScheduler));vm.begin();advanceUntilIdle();vm.chooseOwner("owner");advanceUntilIdle();vm.chooseAnimal("animal-owner");vm.chooseOwner("other");assertNull(vm.state.value.form.animal);assertNull(vm.state.value.form.animals.value);advanceUntilIdle();assertEquals("animal-other",vm.state.value.form.animals.value!!.single().id);vm.close()}
 @Test fun contextualReadFailureKeepsIdsForExplicitRetryWithoutEnablingWrite()=runTest{var readFails=true;val f=object:Fake(){override suspend fun animals(farmerId:String):Inventory{if(readFails)throw AuthFailure(AuthProblem.NETWORK);return super.animals(farmerId)}};val vm=vm(f,StandardTestDispatcher(testScheduler));vm.begin("owner","animal-owner");advanceUntilIdle();assertEquals("animal-owner",vm.state.value.form.animal);input(vm);assertFalse(vm.canSave());readFails=false;vm.begin(vm.state.value.form.owner,vm.state.value.form.animal);advanceUntilIdle();input(vm);assertTrue(vm.state.value.form.fixed);assertTrue(vm.canSave());assertEquals(0,f.calls);vm.close()}
 @Test fun invalidInputAndWrongRoleCannotWrite()=runTest{val f=Fake();val vm=vm(f,StandardTestDispatcher(testScheduler));vm.begin("owner","animal-owner");advanceUntilIdle();vm.save();advanceUntilIdle();assertEquals(0,f.calls);assertTrue(vm.state.value.form.attempted);vm.close();val wrong=VisitsViewModel(f,Role.GANADERO,StandardTestDispatcher(testScheduler));wrong.begin();wrong.save();advanceUntilIdle();assertEquals(0,f.calls);assertNull(wrong.state.value.form.farmers.value);wrong.close()}
 @Test fun uncertainWriteBlocksAllAutomaticResubmissionUntilExplicitInspection()=runTest{val f=Fake();f.fail=true;val vm=vm(f,StandardTestDispatcher(testScheduler));vm.begin("owner","animal-owner");advanceUntilIdle();input(vm);vm.save();advanceUntilIdle();assertTrue(vm.state.value.form.uncertain);vm.save();vm.begin();advanceUntilIdle();assertEquals(1,f.calls);assertFalse(vm.dismiss());vm.acknowledgeUncertain();assertTrue(vm.state.value.form.uncertain);vm.checkUncertain();advanceUntilIdle();vm.acknowledgeUncertain();assertFalse(vm.state.value.form.uncertain);assertEquals(1,f.calls);vm.close()}
 @Test fun lateOwnerResponseCannotReplaceNewAnimalChoices()=runTest{val gate=CompletableDeferred<Unit>();val f=object:Fake(){override suspend fun animals(farmerId:String):Inventory{if(farmerId=="owner")withContext(NonCancellable){gate.await()};return super.animals(farmerId)}};val vm=vm(f,StandardTestDispatcher(testScheduler));vm.begin();advanceUntilIdle();vm.chooseOwner("owner");runCurrent();vm.chooseOwner("other");runCurrent();gate.complete(Unit);advanceUntilIdle();assertEquals("animal-other",vm.state.value.form.animals.value!!.single().id);vm.close()}
 @Test fun detailPermissionFailureDoesNotDisplayOldVisit()=runTest{val f=Fake();val vm=vm(f,StandardTestDispatcher(testScheduler));vm.select(HomeVisit("id",null,"Owner",0,"PROGRAMADA",null));f.denied=true;vm.loadDetail("id",VisitDay(0,1));advanceUntilIdle();assertNull(vm.state.value.detail);assertEquals(403,vm.state.value.detailError!!.status);vm.close()}
 @Test fun cancellationErasesLatePostResultAndAllSessionData()=runTest{val gate=CompletableDeferred<Unit>();val f=object:Fake(){override suspend fun scheduleVisit(farmerId:String,animalId:String,scheduledAt:String,reason:String):String{withContext(NonCancellable){gate.await()};return "late"}};val vm=vm(f,StandardTestDispatcher(testScheduler));vm.begin("owner","animal-owner");advanceUntilIdle();input(vm);vm.save();runCurrent();vm.close();gate.complete(Unit);advanceUntilIdle();assertNull(vm.state.value.form.confirmed);assertNull(vm.state.value.agenda.value);assertNull(vm.state.value.form.owner)}
}
