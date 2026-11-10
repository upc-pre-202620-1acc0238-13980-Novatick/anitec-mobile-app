package pe.novatick.anitec.livestock
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.features.livestock.presentation.*
@OptIn(ExperimentalCoroutinesApi::class)
class LivestockViewModelTest {
    private open class Fake: LivestockRepository {
        var creates=0;var gate: CompletableDeferred<Unit>?=null
        override suspend fun farms()=emptyList<Farm>()
        override suspend fun farm(id: String)=Farm(id,"Farm",null,"owner","2035-01-01T00:00:00Z")
        override suspend fun createFarm(name: String,location: String?): Farm { creates++;gate?.await();return farm("farm") }
        override suspend fun species()=emptyList<Species>()
        override suspend fun capacity()=Capacity("owner",15,0,0)
        override suspend fun inventory(filter: InventoryFilter): Inventory { if(filter.search=="old") withContext(NonCancellable) { gate?.await() };return Inventory(if(filter.search=="old") 1 else 2,emptyList()) }
    }
    @Test fun staleSearchCannotOverwriteNewFilter()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler));try {
            val fake=Fake();fake.gate=CompletableDeferred();val vm=LivestockViewModel(fake)
            vm.filter(InventoryFilter(search="old"));runCurrent();vm.filter(InventoryFilter(search="new"));runCurrent()
            fake.gate!!.complete(Unit);runCurrent();assertEquals("new",vm.uiState.value.filter.search);assertEquals(2L,vm.uiState.value.inventory.value!!.totalActive);vm.close()
        } finally {Dispatchers.resetMain()}
    }
    @Test fun doubleSaveOneCreationAndCloseDiscardsLateAccountData()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler));try {
            val fake=Fake();fake.gate=CompletableDeferred();val vm=LivestockViewModel(fake)
            vm.createFarm("Farm",null);vm.createFarm("Farm",null);runCurrent();assertEquals(1,fake.creates)
            vm.close();fake.gate!!.complete(Unit);runCurrent();assertNull(vm.uiState.value.createdFarm);assertNull(vm.uiState.value.inventory.value)
        } finally {Dispatchers.resetMain()}
    }
    @Test fun timeoutsStayUncertainAndNeverRetryCreation()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler));try {
            val fake=object: Fake() { override suspend fun createFarm(name: String,location: String?): Farm {creates++;throw AuthFailure(AuthProblem.NETWORK)} }
            val vm=LivestockViewModel(fake);vm.createFarm("Farm",null);advanceUntilIdle();assertEquals("UNCERTAIN",vm.uiState.value.saveError);assertEquals(1,fake.creates);vm.close()
        } finally {Dispatchers.resetMain()}
    }
    @Test fun foundationErrorsAreIndependentAndFailedRefreshMarksStaleCapacity()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler));try {
            var fail=false
            val fake=object: Fake() {
                override suspend fun farms():List<Farm> {throw AuthFailure(AuthProblem.FORBIDDEN,403)}
                override suspend fun capacity():Capacity {if(fail) throw AuthFailure(AuthProblem.NETWORK);return super.capacity()}
            }
            val vm=LivestockViewModel(fake);vm.start();advanceUntilIdle();assertTrue(vm.uiState.value.farms.failed);assertNotNull(vm.uiState.value.inventory.value)
            fail=true;vm.refreshFoundation();advanceUntilIdle();assertTrue(vm.uiState.value.capacity.stale);vm.close()
        } finally {Dispatchers.resetMain()}
    }
    @Test fun animalTimeoutBlocksRepeatedPostAndCapacityIsCheckedBeforeSending()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler));try {
            var posts=0;var capacityReads=0
            val fake=object: Fake() {
                override suspend fun capacity(): Capacity {capacityReads++;return Capacity("owner",15,0,0)}
                override suspend fun createAnimal(input: NewAnimal): AnimalSummary {posts++;throw AuthFailure(AuthProblem.NETWORK)}
            }
            val vm=LivestockViewModel(fake)
            val input=NewAnimal("22222222-2222-4222-8222-222222222222","33333333-3333-4333-8333-333333333333",AnimalSex.DESCONOCIDO)
            vm.createAnimal(input);vm.createAnimal(input);advanceUntilIdle()
            assertEquals(1,posts);assertEquals(1,capacityReads);assertEquals("UNCERTAIN",vm.uiState.value.animalError?.code)
            vm.createAnimal(input);advanceUntilIdle();assertEquals(1,posts);vm.close()
        } finally {Dispatchers.resetMain()}
    }
    @Test fun fullCapacityNeverSendsAnimalPost()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler));try {
            var posts=0
            val fake=object: Fake() {
                override suspend fun capacity()=Capacity("owner",15,15,0)
                override suspend fun createAnimal(input: NewAnimal): AnimalSummary {posts++;throw AuthFailure(AuthProblem.SERVER)}
            }
            val vm=LivestockViewModel(fake)
            vm.createAnimal(NewAnimal("22222222-2222-4222-8222-222222222222","33333333-3333-4333-8333-333333333333",AnimalSex.MACHO))
            advanceUntilIdle();assertEquals(0,posts);assertEquals("INVENTORY_LIMIT_REACHED",vm.uiState.value.animalError?.code);vm.close()
        } finally {Dispatchers.resetMain()}
    }
    @Test fun closingAccountDiscardsLateAnimalCreation()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler));try {
            val gate=CompletableDeferred<Unit>()
            val fake=object: Fake() {
                override suspend fun createAnimal(input: NewAnimal):AnimalSummary {
                    withContext(NonCancellable) {gate.await()}
                    return AnimalSummary("animal","SERVER-1",null,"Bovino",input.speciesId,AnimalStatus.ACTIVO,input.farmId,"owner",0)
                }
            }
            val vm=LivestockViewModel(fake)
            vm.createAnimal(NewAnimal("22222222-2222-4222-8222-222222222222","33333333-3333-4333-8333-333333333333",AnimalSex.MACHO))
            runCurrent();vm.close();gate.complete(Unit);advanceUntilIdle()
            assertNull(vm.uiState.value.createdAnimal);assertNull(vm.uiState.value.animalDetail.value)
        } finally {Dispatchers.resetMain()}
    }
    @Test fun lateDetailCannotReplaceAnotherSelectedUuid()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler));try {
            val gate=CompletableDeferred<Unit>()
            val fake=object: Fake() {
                override suspend fun animal(id: String):AnimalDetail {
                    if(id=="old") withContext(NonCancellable) {gate.await()}
                    return AnimalDetail(id,"SERVER",null,"Bovino","species",AnimalSex.DESCONOCIDO,null,null,AnimalStatus.ACTIVO,"farm","Farm",0,null)
                }
            }
            val vm=LivestockViewModel(fake)
            vm.readAnimal("old");runCurrent();vm.readAnimal("new");runCurrent()
            assertEquals("new",vm.uiState.value.animalDetail.value?.id)
            gate.complete(Unit);runCurrent()
            assertEquals("new",vm.uiState.value.animalDetail.value?.id)
            assertEquals("new",vm.uiState.value.animalDetailId);vm.close()
        } finally {Dispatchers.resetMain()}
    }
}
