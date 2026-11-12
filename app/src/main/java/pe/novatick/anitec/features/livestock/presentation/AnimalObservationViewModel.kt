package pe.novatick.anitec.features.livestock.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*

data class ObservationState(val animalId: String?=null,val items: ReadState<List<AnimalObservation>> = ReadState(),
    val busy: Boolean=false,val errors: Map<String,AuthFailure> = emptyMap(),val confirmedFor: String?=null)
class AnimalObservationViewModel(private val repository: LivestockRepository): ViewModel() {
    private val state=MutableStateFlow(ObservationState());val uiState=state.asStateFlow()
    private var closed=false;private var ticket=0L;private var read: Job?=null
    fun load(id: String) {
        if(closed) return
        read?.cancel();val current=++ticket
        state.update {it.copy(animalId=id,items=ReadState(loading=true))}
        read=viewModelScope.launch {
            try {val value=repository.observations(id);if(!closed && current==ticket) state.update {it.copy(items=ReadState(value))}}
            catch(e: CancellationException) {throw e}
            catch(_: AuthFailure) {if(!closed && current==ticket) state.update {it.copy(items=ReadState(failed=true))}}
        }
    }
    fun begin() {state.update {it.copy(confirmedFor=null)}}
    fun acknowledge(id: String) {
        if(!closed && !state.value.busy && state.value.animalId==id && state.value.items.value!=null && !state.value.items.failed)
            state.update {it.copy(errors=it.errors-id)}
    }
    fun save(id: String,text: String) {
        if(closed || state.value.busy || state.value.errors[id]?.code=="UNCERTAIN") return
        if(!validObservationText(text)) {state.update {it.copy(errors=it.errors+(id to AuthFailure(AuthProblem.SERVER,400,"VALIDATION_ERROR")))};return}
        state.update {it.copy(busy=true,errors=it.errors-id,confirmedFor=null)}
        viewModelScope.launch {
            try {
                repository.addObservation(id,text)
                if(!closed) {state.update {it.copy(busy=false,confirmedFor=id)};if(state.value.animalId==null || state.value.animalId==id) load(id)}
            } catch(e: CancellationException) {throw e}
            catch(e: AuthFailure) {if(!closed) {
                val failure=if(e.problem==AuthProblem.NETWORK || e.problem==AuthProblem.INVALID_RESPONSE || e.status>=500) AuthFailure(e.problem,e.status,"UNCERTAIN") else e
                if(failure.code=="UNCERTAIN") {read?.cancel();ticket++}
                state.update {it.copy(busy=false,errors=it.errors+(id to failure),items=if(failure.code=="UNCERTAIN") ReadState() else it.items)}
            }}
        }
    }
    fun close() {closed=true;ticket++;viewModelScope.cancel();state.value=ObservationState()}
}
