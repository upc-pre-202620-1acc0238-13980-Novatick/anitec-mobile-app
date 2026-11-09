package pe.novatick.anitec.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.novatick.anitec.features.auth.application.*
import pe.novatick.anitec.features.auth.domain.*

enum class RegistrationStep { FORM, VERIFY, COMPLETE }
enum class RegistrationNotice { PENDING, GENERIC_RESEND, AUTHORIZED_RESEND, CHANGE_EMAIL }
data class RegistrationUiState(val step: RegistrationStep=RegistrationStep.FORM, val email: String="",
    val busy: Boolean=false, val issue: RegistrationIssue?=null, val errorCode: String?=null,
    val fields: List<String> = emptyList(), val waitUntil: Long?=null, val verifyWaitUntil: Long?=null, val remaining: Int?=null,
    val notice: RegistrationNotice?=null, val codeVersion: Long=0,
    val passwordPrompt: Boolean=false, val draftRevision: Long=0)
class RegistrationViewModel(private val repository: RegistrationRepository, private val now: ()->Long=System::currentTimeMillis) : ViewModel() {
    private val state=MutableStateFlow(RegistrationUiState())
    val uiState=state.asStateFlow()
    private var password: String?=null
    private var generation=0L
    private var resendDraft=""
    fun passwordDraftForEntry(): String = resendDraft
    fun updatePasswordDraft(value: String) { resendDraft=value; state.value=state.value.copy(draftRevision=state.value.draftRevision+1) }
    fun dismissPasswordPrompt() { resendDraft=""; state.value=state.value.copy(passwordPrompt=false,issue=null) }
    fun abandon(changingEmail: Boolean=false) {
        generation++; password=null; resendDraft=""
        state.value=RegistrationUiState(notice=if(changingEmail) RegistrationNotice.CHANGE_EMAIL else null)
    }
    fun continueVerification(email: String,credential: String?) {
        abandon()
        if(!RegistrationValidation.email(email)) { state.value=state.value.copy(issue=RegistrationIssue.FIELDS); return }
        password=credential?.takeIf { it.length<=256 }
        state.value=RegistrationUiState(step=RegistrationStep.VERIFY,email=email.trim(),notice=RegistrationNotice.PENDING)
    }
    fun register(form: RegistrationForm,language: String) {
        if(state.value.busy) return
        try { RegistrationValidation.validate(form) } catch(e: RegistrationFailure) { fail(e); return }
        val id=++generation
        password=null
        state.value=state.value.copy(busy=true,issue=null,errorCode=null,fields=emptyList())
        viewModelScope.launch { try {
            val result=RegisterAccount(repository)(form,language)
            if(id==generation) {
                password=form.password
                state.value=RegistrationUiState(step=RegistrationStep.VERIFY,email=result.email,notice=RegistrationNotice.PENDING)
            }
        } catch(e: CancellationException) { throw e } catch(e: RegistrationFailure) { if(id==generation) fail(e) }
        finally { if(id==generation) state.value=state.value.copy(busy=false) } }
    }
    fun verify(code: String) {
        if(state.value.busy || state.value.step!=RegistrationStep.VERIFY || now()<(state.value.verifyWaitUntil ?: 0)) return
        if(!RegistrationValidation.otp(code)) { fail(RegistrationFailure(RegistrationIssue.OTP)); return }
        val email=state.value.email; val secret=password
        run { VerifyAccount(repository)(email,code,secret) }
    }
    fun resend(credential: String?,language: String) {
        if(state.value.busy || state.value.step!=RegistrationStep.VERIFY || now()<(state.value.waitUntil ?: 0)) return
        val secret=credential?.takeIf { it.isNotEmpty() } ?: password
        if(secret==null) { state.value=state.value.copy(passwordPrompt=true,issue=null,errorCode=null); return }
        if(secret.length>256) { state.value=state.value.copy(passwordPrompt=true,issue=RegistrationIssue.CONTEXT); return }
        resendDraft=""
        state.value=state.value.copy(passwordPrompt=false)
        password=secret
        val email=state.value.email
        run(resend=true) { ResendVerification(repository)(email,secret,language) }
    }
    private fun fail(e: RegistrationFailure,verifying: Boolean=false) {
        state.value=state.value.copy(issue=e.issue,errorCode=e.code,fields=e.fields,
            waitUntil=if(verifying) state.value.waitUntil else listOfNotNull(state.value.waitUntil,e.retryAt).maxOrNull(),
            verifyWaitUntil=if(verifying) listOfNotNull(state.value.verifyWaitUntil,e.retryAt).maxOrNull() else state.value.verifyWaitUntil)
    }
    private fun <T> run(resend: Boolean=false,action: suspend ()->T) {
        val id=generation
        state.value=state.value.copy(busy=true,issue=null,errorCode=null,fields=emptyList())
        viewModelScope.launch { try {
            val result=action()
            if(id==generation) {
                if(resend) {
                    val value=result as ResendResult
                    if(!value.authorized) password=null
                    state.value=state.value.copy(notice=if(value.authorized) RegistrationNotice.AUTHORIZED_RESEND else RegistrationNotice.GENERIC_RESEND,
                        remaining=value.remaining,waitUntil=value.nextAt,codeVersion=state.value.codeVersion+if(value.authorized) 1 else 0)
                } else { password=null; resendDraft=""; state.value=RegistrationUiState(step=RegistrationStep.COMPLETE,email=state.value.email) }
            }
        } catch(e: CancellationException) { throw e } catch(e: RegistrationFailure) { if(id==generation) fail(e,verifying=!resend) }
        finally { if(id==generation) state.value=state.value.copy(busy=false) } }
    }
    override fun onCleared() { password=null; resendDraft=""; generation++ }
}
