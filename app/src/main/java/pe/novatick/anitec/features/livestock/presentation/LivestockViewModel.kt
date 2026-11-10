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
data class LivestockState(val farms: ReadState<List<Farm>> = ReadState(),val species: ReadState<List<Species>> = ReadState(),
    val capacity: ReadState<Capacity> = ReadState(),val inventory: ReadState<Inventory> = ReadState(),val filter: InventoryFilter=InventoryFilter(),
    val selectedFarm: ReadState<Farm> = ReadState(),val saving: Boolean=false,val saveError: String?=null,val createdFarm: Farm?=null)
/** One instance per authenticated composition; never persisted or shared between users. */
class LivestockViewModel(private val repository: LivestockRepository) : ViewModel() {
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
    fun clearCreation() { state.update { it.copy(createdFarm=null,saveError=null) } }
    fun close() { closed=true;sequence++;viewModelScope.cancel();state.value=LivestockState() }
}
