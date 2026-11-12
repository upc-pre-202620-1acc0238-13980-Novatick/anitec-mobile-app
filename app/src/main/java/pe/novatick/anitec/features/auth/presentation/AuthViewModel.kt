package pe.novatick.anitec.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.novatick.anitec.features.auth.application.*
import pe.novatick.anitec.features.auth.domain.*

data class LoginUiState(val busy: Boolean=false, val restoring: Boolean=true, val problem: AuthProblem?=null,
                        val notice: Notice?=null, val logoutBlocked: Boolean=false, val retryLogout: Boolean=false, val unverified: Boolean=false)
enum class Notice { DEMO, RECOVERY, REMOTE_LOGOUT_FAILED, REMOTE_LOGOUT_OK, LOGOUT_INCOMPLETE, REMOTE_ONLY, VERIFIED }
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    val profile=repository.profile
    private val state=MutableStateFlow(LoginUiState())
    val uiState=state.asStateFlow()
    private var action=0L
    private var firstRestore=true
    private class Pending(val email: String,val password: String)
    private var pending: Pending?=null
    private var verificationGeneration=0L
    fun clearVerificationContext() { verificationGeneration++; pending=null; state.value=state.value.copy(unverified=false) }
    fun continueVerification(start: (String,String?)->Unit) {
        val context=pending ?: return
        clearVerificationContext(); start(context.email,context.password)
    }
    init { restore() }
    fun restore() {
        if(state.value.busy) return
        val id=++action
        val initial=firstRestore; firstRestore=false
        state.value=state.value.copy(busy=true,restoring=initial,problem=null)
        viewModelScope.launch {
            try { RestoreSession(repository)() }
            catch(e: CancellationException) { throw e }
            catch(e: AuthFailure) { if(id==action) state.value=state.value.copy(problem=e.problem) }
            finally { if(id==action) state.value=state.value.copy(busy=false,restoring=false) }
        }
    }
    fun login(email: String,password: String,remember: Boolean) {
        if(state.value.busy || state.value.logoutBlocked) return
        val id=++action
        clearVerificationContext()
        val contextId=verificationGeneration
        state.value=LoginUiState(busy=true,restoring=false)
        viewModelScope.launch {
            try { Login(repository)(email,password,remember) }
            catch(e: CancellationException) { throw e }
            catch(e: AuthFailure) { if(id==action) {
                if(e.code=="EMAIL_NOT_VERIFIED" && contextId==verificationGeneration) pending=Pending(email.trim(),password)
                state.value=state.value.copy(problem=e.problem,unverified=pending!=null)
            } }
            finally { if(id==action) state.value=state.value.copy(busy=false) }
        }
    }
    fun notice(n: Notice) { state.value=state.value.copy(notice=n) }
    override fun onCleared() { pending=null }
    fun logout() {
        clearVerificationContext()
        val id=++action
        val revoke: suspend () -> LogoutResult = try { Logout(repository)() } catch(_: Exception) {
            state.value=LoginUiState(restoring=false,problem=AuthProblem.STORAGE); return
        }
        state.value=LoginUiState(restoring=false,busy=true,logoutBlocked=true)
        viewModelScope.launch {
            val result=revoke()
            if(id==action) state.value=LoginUiState(restoring=false,logoutBlocked=!result.local.protected,
                retryLogout=!result.local.protected || !result.remoteConfirmed, notice=when {
                    result.local.protected && result.remoteConfirmed -> Notice.REMOTE_LOGOUT_OK
                    result.local.protected -> Notice.REMOTE_LOGOUT_FAILED
                    result.remoteConfirmed -> Notice.REMOTE_ONLY
                    else -> Notice.LOGOUT_INCOMPLETE
                })
        }
    }
}
