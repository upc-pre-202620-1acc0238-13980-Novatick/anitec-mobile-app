package pe.novatick.anitec.livestock

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.features.livestock.presentation.*

@OptIn(ExperimentalCoroutinesApi::class)
class AnimalMutationTest {
    private val base=AnimalDetail("11111111-1111-4111-8111-111111111111","BOV-024","Luna","Bovino","33333333-3333-4333-8333-333333333333",AnimalSex.HEMBRA,"Holstein","2021-03-12",AnimalStatus.ACTIVO,"22222222-2222-4222-8222-222222222222","Farm",7,"https://example.invalid/photo.png")
    private inner class Fake: LivestockRepository {
        var value=base;var updates=0;var deletes=0;var failure: AuthFailure?=null;var gate: CompletableDeferred<Unit>?=null;var late=false
        var staleRead: CompletableDeferred<Unit>?=null;var animalReads=0
        override suspend fun animal(id: String): AnimalDetail {
            if(animalReads++==0 && staleRead!=null) {val old=value;withContext(NonCancellable) {staleRead!!.await()};return old}
            return value
        }
        override suspend fun updateAnimal(id: String,input: AnimalEdit): AnimalSummary {
            updates++
            if(late) withContext(NonCancellable) {gate?.await()} else gate?.await()
            failure?.let {throw it}
            value=value.copy(code=input.code,name=input.name,breed=input.breed,birthDate=input.birthDate,photoUrl=input.photoUrl,version=value.version+1)
            return summary()
        }
        private fun summary()=AnimalSummary(value.id,value.code,value.name,value.species,value.speciesId,value.status,value.farmId,"owner",value.version)
        override suspend fun deactivateAnimal(id: String): AnimalDeactivation {deletes++;gate?.await();failure?.let {throw it};value=value.copy(status=AnimalStatus.INACTIVO,version=value.version+1);return AnimalDeactivation(id,value.status)}
        override suspend fun farms()=emptyList<Farm>()
        override suspend fun farm(id: String)=Farm(id,"Farm",null,"owner","2035-01-01T00:00:00Z")
        override suspend fun createFarm(name: String,location: String?)=farm(base.farmId)
        override suspend fun species()=emptyList<Species>()
        override suspend fun inventory(filter: InventoryFilter)=Inventory(if(value.status==AnimalStatus.ACTIVO) 1 else 0,listOf(summary()).filter {filter.status==null || it.status==filter.status})
        override suspend fun capacity()=Capacity("owner",15,if(value.status==AnimalStatus.ACTIVO) 1 else 0,0)
    }
    private fun check(block: suspend TestScope.(Fake,LivestockViewModel)->Unit)=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fake=Fake();val vm=LivestockViewModel(fake)
        try {block(fake,vm)} finally {vm.close();Dispatchers.resetMain()}
    }
    @Test fun editSnapshotPreservesOptionalPhotoAndIdentityVersion() {
        val draft=AnimalEdit.from(base)
        assertEquals(base.photoUrl,draft.photoUrl);assertEquals(7L,draft.version);assertEquals(base.breed,draft.breed)
        assertTrue(animalEditErrors(base,draft.copy(name="Changed")).isEmpty())
        assertTrue("code" in animalEditErrors(base,draft.copy(code=" ")))
        assertTrue("version" in animalEditErrors(base,draft.copy(version=8)))
        assertTrue("name" in animalEditErrors(base,draft.copy(name="x".repeat(101))))
        assertTrue("birthDate" in animalEditErrors(base,draft.copy(birthDate="2999-01-01")))
    }
    @Test fun doubleSaveCreatesOneWriteAndRefreshesDetailInventoryAndCapacity()=check {fake,vm ->
        fake.gate=CompletableDeferred();val draft=AnimalEdit.from(base).copy(name="Updated")
        vm.updateAnimal(base,draft);vm.updateAnimal(base,draft);runCurrent();assertEquals(1,fake.updates);assertTrue(vm.uiState.value.mutation.busy)
        fake.gate!!.complete(Unit);advanceUntilIdle()
        assertEquals("EDIT",vm.uiState.value.mutation.completed);assertEquals("Updated",vm.uiState.value.animalDetail.value!!.name)
        assertEquals(8L,vm.uiState.value.animalDetail.value!!.version);assertEquals("Updated",vm.uiState.value.inventory.value!!.content.single().name)
        assertEquals(base.photoUrl,fake.value.photoUrl);assertEquals(base.farmId,fake.value.farmId)
    }
    @Test fun invalidInputDoesNotWrite()=check {fake,vm ->
        vm.updateAnimal(base,AnimalEdit.from(base).copy(code=""));advanceUntilIdle();assertEquals(0,fake.updates);assertEquals("VALIDATION_ERROR",vm.uiState.value.mutation.error!!.code)
    }
    @Test fun versionConflictNeverAutomaticallyResubmitsOrOverwrites()=check {fake,vm ->
        fake.failure=AuthFailure(AuthProblem.SERVER,409,"ANIMAL_VERSION_CONFLICT")
        vm.updateAnimal(base,AnimalEdit.from(base));advanceUntilIdle();vm.updateAnimal(base,AnimalEdit.from(base));advanceUntilIdle()
        assertEquals(1,fake.updates);assertEquals("ANIMAL_VERSION_CONFLICT",vm.uiState.value.mutation.error!!.code)
        fake.value=base.copy(version=8,name="Other writer");vm.reloadMutation(base.id);advanceUntilIdle()
        assertEquals("Other writer",vm.uiState.value.animalDetail.value!!.name);assertNull(vm.uiState.value.mutation.error)
    }
    @Test fun uncertainWriteBlocksAutomaticRetryUntilExplicitReconciliation()=check {fake,vm ->
        fake.failure=AuthFailure(AuthProblem.NETWORK);vm.updateAnimal(base,AnimalEdit.from(base));advanceUntilIdle()
        assertEquals("UNCERTAIN",vm.uiState.value.mutation.error!!.code);vm.updateAnimal(base,AnimalEdit.from(base));advanceUntilIdle();assertEquals(1,fake.updates)
        vm.reloadMutation(base.id);advanceUntilIdle();assertEquals(1,fake.updates);assertNull(vm.uiState.value.mutation.error)
    }
    @Test fun duplicateConfirmationDeactivatesOnceAndFreesCapacityFromReads()=check {fake,vm ->
        fake.gate=CompletableDeferred();vm.deactivateAnimal(base);vm.deactivateAnimal(base);runCurrent();assertEquals(1,fake.deletes)
        fake.gate!!.complete(Unit);advanceUntilIdle();assertEquals("DEACTIVATE",vm.uiState.value.mutation.completed)
        assertEquals(AnimalStatus.INACTIVO,vm.uiState.value.animalDetail.value!!.status);assertEquals(0L,vm.uiState.value.capacity.value!!.activeAnimals)
        assertTrue(vm.uiState.value.inventory.value!!.content.isEmpty())
    }
    @Test fun staleDeactivationSnapshotIsRejectedBeforeDelete()=check {fake,vm ->
        fake.value=base.copy(version=8);vm.deactivateAnimal(base);advanceUntilIdle();assertEquals(0,fake.deletes);assertEquals("ANIMAL_VERSION_CONFLICT",vm.uiState.value.mutation.error!!.code)
    }
    @Test fun forbiddenResponseClearsStaleAnimalDetail()=check {fake,vm ->
        vm.readAnimal(base.id);advanceUntilIdle();fake.failure=AuthFailure(AuthProblem.FORBIDDEN,403,"FORBIDDEN")
        vm.updateAnimal(base,AnimalEdit.from(base));advanceUntilIdle();assertNull(vm.uiState.value.animalDetail.value);assertEquals(403,vm.uiState.value.animalDetailError!!.status)
    }
    @Test fun lateWriteAfterSessionCloseCannotRepopulateState()=check {fake,vm ->
        fake.gate=CompletableDeferred();fake.late=true;vm.updateAnimal(base,AnimalEdit.from(base));runCurrent();vm.close();fake.gate!!.complete(Unit);advanceUntilIdle()
        assertEquals(LivestockState(),vm.uiState.value)
    }
    @Test fun latePreEditReadCannotReplaceSuccessfulUpdatedSnapshot()=check {fake,vm ->
        fake.staleRead=CompletableDeferred();vm.readAnimal(base.id);runCurrent()
        vm.updateAnimal(base,AnimalEdit.from(base).copy(name="New snapshot"));runCurrent()
        assertEquals("New snapshot",vm.uiState.value.animalDetail.value!!.name)
        fake.staleRead!!.complete(Unit);advanceUntilIdle()
        assertEquals("New snapshot",vm.uiState.value.animalDetail.value!!.name)
    }
}
