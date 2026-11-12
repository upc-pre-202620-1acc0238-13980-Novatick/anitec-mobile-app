package pe.novatick.anitec.livestock
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.features.livestock.presentation.*
@OptIn(ExperimentalCoroutinesApi::class)
class AnimalObservationTest {
    private open class Fake: LivestockRepository {
        var posts=0;var gate: CompletableDeferred<Unit>?=null;var uncertain=false
        override suspend fun observations(id: String):List<AnimalObservation> {
            if(id=="old") withContext(NonCancellable) {gate?.await()}
            return listOf(AnimalObservation(id,"Text",null,"2026-01-01"))
        }
        override suspend fun addObservation(id: String,text: String):AnimalObservation {posts++;withContext(NonCancellable) {gate?.await()};if(uncertain) throw AuthFailure(AuthProblem.NETWORK);return AnimalObservation(id,text,null,null)}
        override suspend fun farms()=emptyList<Farm>()
        override suspend fun farm(id: String):Farm=error("unused")
        override suspend fun createFarm(name: String,location: String?):Farm=error("unused")
        override suspend fun species()=emptyList<Species>()
        override suspend fun inventory(filter: InventoryFilter)=Inventory(0,emptyList())
        override suspend fun capacity()=Capacity("owner",1,0,0)
    }
    private fun test(block: suspend TestScope.()->Unit)=runTest {Dispatchers.setMain(StandardTestDispatcher(testScheduler));try {block()} finally {Dispatchers.resetMain()}}
    @Test fun blankDoesNotPostAndContractHasNoInventedMaximum()=test {
        val f=Fake();val vm=AnimalObservationViewModel(f);vm.save("id"," ");advanceUntilIdle();assertEquals(0,f.posts);assertTrue(validObservationText("x".repeat(1000)));assertFalse(validObservationText("\n "));vm.close()
    }
    @Test fun doubleSaveOnePostAndConfirmedRefresh()=test {
        val f=Fake();f.gate=CompletableDeferred();val vm=AnimalObservationViewModel(f);vm.save("id","text");vm.save("id","text");runCurrent();assertEquals(1,f.posts);f.gate!!.complete(Unit);advanceUntilIdle();assertEquals("id",vm.uiState.value.confirmedFor);assertEquals("id",vm.uiState.value.items.value!!.single().id);vm.close()
    }
    @Test fun uncertaintyRequiresFreshReadAndExplicitManualDecision()=test {
        val f=Fake();f.uncertain=true;val vm=AnimalObservationViewModel(f);vm.load("id");advanceUntilIdle();vm.save("id","text");advanceUntilIdle();vm.acknowledge("id");vm.save("id","text");advanceUntilIdle();assertEquals(1,f.posts);assertNull(vm.uiState.value.items.value);vm.load("id");advanceUntilIdle();assertEquals("UNCERTAIN",vm.uiState.value.errors["id"]?.code);vm.acknowledge("id");f.uncertain=false;vm.save("id","manual");advanceUntilIdle();assertEquals(2,f.posts);vm.close()
    }
    @Test fun oldUuidResponseCannotReplaceNewAnimal()=test {
        val f=Fake();f.gate=CompletableDeferred();val vm=AnimalObservationViewModel(f);vm.load("old");runCurrent();vm.load("new");runCurrent();f.gate!!.complete(Unit);advanceUntilIdle();assertEquals("new",vm.uiState.value.items.value!!.single().id);vm.close()
    }
    @Test fun closedSessionCannotReceiveLatePostOrRead()=test {
        val f=Fake();f.gate=CompletableDeferred();val vm=AnimalObservationViewModel(f);vm.save("id","text");runCurrent();vm.close();f.gate!!.complete(Unit);advanceUntilIdle();assertNull(vm.uiState.value.confirmedFor);assertNull(vm.uiState.value.items.value);assertFalse(vm.uiState.value.busy)
    }
    @Test fun failedReadCanBeRetriedWithoutAWrite()=test {
        var reads=0
        val f=object: Fake() {override suspend fun observations(id: String):List<AnimalObservation> {reads++;if(reads==1) throw AuthFailure(AuthProblem.SERVER,403);return emptyList()}}
        val vm=AnimalObservationViewModel(f);vm.load("id");advanceUntilIdle();assertTrue(vm.uiState.value.items.failed)
        vm.load("id");advanceUntilIdle();assertEquals(emptyList<AnimalObservation>(),vm.uiState.value.items.value);assertEquals(0,f.posts);vm.close()
    }
    @Test fun definiteRejectionAllowsCorrectionAndLateSaveDoesNotChangeSelectedAnimal()=test {
        val f=object: Fake() {override suspend fun addObservation(id: String,text: String):AnimalObservation {posts++;if(posts==1) throw AuthFailure(AuthProblem.SERVER,400);withContext(NonCancellable) {gate?.await()};return AnimalObservation(id,text,null,null)}}
        val vm=AnimalObservationViewModel(f);vm.save("old","first");advanceUntilIdle();assertEquals(400,vm.uiState.value.errors["old"]?.status)
        f.gate=CompletableDeferred();vm.save("old","fixed");runCurrent();vm.load("new");runCurrent();f.gate!!.complete(Unit);advanceUntilIdle()
        assertEquals(2,f.posts);assertEquals("new",vm.uiState.value.animalId);assertEquals("new",vm.uiState.value.items.value!!.single().id);vm.close()
    }
}
