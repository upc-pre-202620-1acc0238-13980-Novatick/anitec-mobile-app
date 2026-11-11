package pe.novatick.anitec.features.livestock.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.features.livestock.application.*
data class ReadState<T>(val value: T?=null,val loading: Boolean=false,val failed: Boolean=false) {
    val stale get()=failed && value!=null
}
data class AnimalMutation(val busy: Boolean=false,val error: AuthFailure?=null,val completed: String?=null)
data class LivestockState(val mutation: AnimalMutation=AnimalMutation(),val farms: ReadState<List<Farm>> = ReadState(),val species: ReadState<List<Species>> = ReadState(),
    val capacity: ReadState<Capacity> = ReadState(),val inventory: ReadState<Inventory> = ReadState(),val filter: InventoryFilter=InventoryFilter(),
    val registering: Boolean=false,val animalError: AuthFailure?=null,val createdAnimal: AnimalSummary?=null,
    val animalDetail: ReadState<AnimalDetail> = ReadState(),val animalDetailError: AuthFailure?=null,val animalDetailId: String?=null,
    val selectedFarm: ReadState<Farm> = ReadState(),val saving: Boolean=false,val saveError: String?=null,val createdFarm: Farm?=null)
/** One instance per authenticated composition; never persisted or shared between users. */
class LivestockViewModel(private val repository: LivestockRepository) : ViewModel() {
    val observations=AnimalObservationViewModel(repository)
    private val state=MutableStateFlow(LivestockState());val uiState=state.asStateFlow()
    private var query: Job?=null;private var detail: Job?=null;private var sequence=0L
    private var closed=false
    private var foundationSequence=0L
    fun start() { refreshFoundation();filter(state.value.filter) }
    fun refreshFoundation() {
        if(closed) return
        val ticket=++foundationSequence
        viewModelScope.launch {
            state.update { it.copy(farms=it.farms.copy(loading=true,failed=false)) }
            try { val v=repository.farms();if(!closed && ticket==foundationSequence) state.update { it.copy(farms=ReadState(v)) } }
            catch(e: CancellationException) { throw e } catch(e: AuthFailure) { if(!closed && ticket==foundationSequence) state.update { it.copy(farms=if(e.status==403) ReadState(failed=true) else it.farms.copy(loading=false,failed=true)) } }
        }
        viewModelScope.launch {
            state.update { it.copy(species=it.species.copy(loading=true,failed=false)) }
            try { val v=repository.species();if(!closed && ticket==foundationSequence) state.update { it.copy(species=ReadState(v)) } }
            catch(e: CancellationException) { throw e } catch(e: AuthFailure) { if(!closed && ticket==foundationSequence) state.update { it.copy(species=if(e.status==403) ReadState(failed=true) else it.species.copy(loading=false,failed=true)) } }
        }
        viewModelScope.launch {
            state.update { it.copy(capacity=it.capacity.copy(loading=true,failed=false)) }
            try { val v=repository.capacity();if(!closed && ticket==foundationSequence) state.update { it.copy(capacity=ReadState(v)) } }
            catch(e: CancellationException) { throw e } catch(e: AuthFailure) { if(!closed && ticket==foundationSequence) state.update { it.copy(capacity=if(e.status==403) ReadState(failed=true) else it.capacity.copy(loading=false,failed=true)) } }
        }
    }
    fun filter(value: InventoryFilter,debounce: Boolean=false) {
        if(closed) return
        query?.cancel();val id=++sequence
        state.update { it.copy(filter=value,inventory=ReadState(loading=true)) }
        query=viewModelScope.launch {
            if(debounce) delay(300)
            try { val v=QueryInventory(repository)(value);if(!closed && id==sequence) state.update { it.copy(inventory=ReadState(v)) } }
            catch(e: CancellationException) { throw e } catch(_: AuthFailure) { if(!closed && id==sequence) state.update { it.copy(inventory=ReadState(failed=true)) } }
        }
    }
    fun selectFarm(id: String?) {
        detail?.cancel();state.update { it.copy(selectedFarm=ReadState()) };filter(state.value.filter.copy(farmId=id))
        if(id!=null) detail=viewModelScope.launch {
            state.update { it.copy(selectedFarm=ReadState(loading=true)) }
            try { val v=repository.farm(id);if(!closed && state.value.filter.farmId==id) state.update { it.copy(selectedFarm=ReadState(v)) } }
            catch(e: CancellationException) { throw e } catch(_: AuthFailure) { if(!closed && state.value.filter.farmId==id) state.update { it.copy(selectedFarm=ReadState(failed=true),inventory=ReadState(failed=true)) } }
        }
    }
    fun createFarm(name: String,location: String?) {
        if(closed || state.value.saving) return
        state.update { it.copy(saving=true,saveError=null,createdFarm=null) }
        viewModelScope.launch {
            try { val v=CreateFarm(repository)(name,location);if(!closed) {
                state.update { it.copy(saving=false,createdFarm=v,farms=ReadState((it.farms.value.orEmpty().filterNot { f->f.id==v.id })+v)) }
                refreshFoundation();selectFarm(v.id)
            } } catch(e: CancellationException) { throw e } catch(e: AuthFailure) { if(!closed) state.update { it.copy(saving=false,saveError=if(e.problem==AuthProblem.NETWORK) "UNCERTAIN" else e.code ?: "ERROR") } }
        }
    }
    private var animalRead: Job?=null
    private var animalSequence=0L
    private var localAnimalValidation=false
    fun beginAnimal() {if(!state.value.registering && state.value.animalError?.code!="UNCERTAIN") state.update {it.copy(animalError=null,createdAnimal=null)};refreshFoundation()}
    /** Only dismiss validation generated locally; backend failures and uncertain writes remain visible. */
    fun clearCorrectedAnimalInputError(input: NewAnimal) {
        if(closed || state.value.registering || !localAnimalValidation || animalInputErrors(input).isNotEmpty()) return
        localAnimalValidation=false
        state.update {it.copy(animalError=null)}
    }
    fun acknowledgeAnimalUncertainty() {if(!state.value.registering) state.update {it.copy(animalError=null)}}
    fun consumeAnimal() {state.update {it.copy(createdAnimal=null)}}
    fun createAnimal(input: NewAnimal) {
        if(closed || state.value.registering || state.value.animalError?.code=="UNCERTAIN") return
        val errors=animalInputErrors(input)
        if(errors.isNotEmpty()) {localAnimalValidation=true;state.update {it.copy(animalError=AuthFailure(AuthProblem.SERVER,400,"VALIDATION_ERROR",errors.map {it to "invalid"}))};return}
        localAnimalValidation=false
        state.update {it.copy(registering=true,animalError=null)}
        viewModelScope.launch {
            var sent=false
            try {
                val capacity=repository.capacity()
                if(closed) return@launch
                state.update {it.copy(capacity=ReadState(capacity))}
                if(capacity.remaining==0L) throw AuthFailure(AuthProblem.SERVER,409,"INVENTORY_LIMIT_REACHED")
                sent=true
                val animal=repository.createAnimal(input)
                if(!closed) {
                    state.update {it.copy(registering=false,createdAnimal=animal)}
                    state.update {it.copy(filter=InventoryFilter(farmId=animal.farmId))}
                    refreshFoundation();selectFarm(animal.farmId)
                }
            } catch(e: CancellationException) {throw e}
            catch(e: AuthFailure) {if(!closed) state.update {it.copy(registering=false,animalError=
                if(sent && (e.problem==AuthProblem.NETWORK || e.problem==AuthProblem.INVALID_RESPONSE || e.status>=500)) AuthFailure(e.problem,e.status,"UNCERTAIN") else e)}
                if(!closed && e.code=="INVENTORY_LIMIT_REACHED") refreshFoundation()
            }
        }
    }
    fun beginMutation() {if(!state.value.mutation.busy) state.update {it.copy(mutation=AnimalMutation())}}
    fun consumeMutation() {state.update {it.copy(mutation=AnimalMutation())}}
    fun reloadMutation(id: String) {
        if(state.value.mutation.busy) return
        state.update {it.copy(mutation=AnimalMutation())};readAnimal(id)
    }
    fun updateAnimal(base: AnimalDetail,input: AnimalEdit) {
        if(closed || state.value.mutation.busy || state.value.mutation.error?.code in listOf("UNCERTAIN","ANIMAL_VERSION_CONFLICT")) return
        val errors=animalEditErrors(base,input)
        if(errors.isNotEmpty()) {state.update {it.copy(mutation=AnimalMutation(error=AuthFailure(AuthProblem.SERVER,400,"VALIDATION_ERROR",errors.map {it to "invalid"})))};return}
        mutate(base,"EDIT") { repository.updateAnimal(base.id,input) }
    }
    fun deactivateAnimal(base: AnimalDetail) {
        if(closed || state.value.mutation.busy || state.value.mutation.error?.code in listOf("UNCERTAIN","ANIMAL_VERSION_CONFLICT")) return
        mutate(base,"DEACTIVATE") {
            // DELETE has no version contract: detect an already stale snapshot, never invent a parameter.
            // A race after this read remains a documented server-contract limitation.
            val latest=repository.animal(base.id)
            if(latest.version!=base.version || latest.status!=base.status) throw AuthFailure(AuthProblem.SERVER,409,"ANIMAL_VERSION_CONFLICT")
            repository.deactivateAnimal(base.id)
        }
    }
    private fun mutate(base: AnimalDetail,kind: String,operation: suspend ()->Any) {
        animalRead?.cancel();animalSequence++ // Older reads cannot restore a pre-write snapshot.
        state.update {it.copy(mutation=AnimalMutation(busy=true))}
        viewModelScope.launch {
            try {
                operation()
                if(!closed) {
                    state.update {it.copy(mutation=AnimalMutation(completed=kind))}
                    readAnimal(base.id);refreshFoundation();filter(state.value.filter)
                }
            } catch(e: CancellationException) {throw e}
            catch(e: AuthFailure) {if(!closed) {
                val error=if(e.problem==AuthProblem.NETWORK || e.problem==AuthProblem.INVALID_RESPONSE || e.status>=500) AuthFailure(e.problem,e.status,"UNCERTAIN") else e
                state.update {it.copy(mutation=AnimalMutation(error=error))}
                if(e.status==403 || e.status==404) state.update {it.copy(animalDetail=ReadState(failed=true),animalDetailError=e)}
            }}
        }
    }
    fun readAnimal(id: String) {
        if(closed) return
        animalRead?.cancel();val ticket=++animalSequence
        state.update {it.copy(animalDetail=ReadState(loading=true),animalDetailError=null,animalDetailId=id)}
        animalRead=viewModelScope.launch {
            try {val value=repository.animal(id);if(!closed && ticket==animalSequence) state.update {it.copy(animalDetail=ReadState(value))}}
            catch(e: CancellationException) {throw e}
            catch(e: AuthFailure) {if(!closed && ticket==animalSequence) state.update {it.copy(animalDetail=ReadState(failed=true),animalDetailError=e)}}
        }
    }
    fun clearCreation() { state.update { it.copy(createdFarm=null,saveError=null) } }
    fun close() { observations.close();closed=true;sequence++;viewModelScope.cancel();state.value=LivestockState() }
}
