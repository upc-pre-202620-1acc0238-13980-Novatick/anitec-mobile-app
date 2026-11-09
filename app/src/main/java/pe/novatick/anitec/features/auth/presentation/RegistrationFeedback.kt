package pe.novatick.anitec.features.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.RegistrationIssue

@Composable
fun RegistrationFeedback(state: RegistrationUiState) {
    val error=when(state.issue) {
        RegistrationIssue.PASSWORD_BYTES -> R.string.reg_password_bytes
        RegistrationIssue.PASSWORD_COMPLEXITY -> R.string.reg_password_complexity
        RegistrationIssue.CONFIRMATION -> R.string.reg_confirmation_error
        RegistrationIssue.TERMS -> R.string.reg_terms_error
        RegistrationIssue.OTP -> R.string.reg_otp_error
        RegistrationIssue.CONTEXT -> R.string.reg_context_error
        RegistrationIssue.FIELDS -> R.string.reg_fields_error
        null -> when(state.errorCode) {
            "EMAIL_ALREADY_REGISTERED" -> R.string.reg_existing
            "CODE_EXPIRED" -> R.string.reg_expired
            "CODE_ATTEMPTS_EXCEEDED" -> R.string.reg_attempts
            "INVALID_CODE" -> R.string.reg_invalid_code
            "RESEND_COOLDOWN", "VERIFICATION_RESEND_LIMIT", "VERIFICATION_ORIGIN_LIMIT" -> R.string.reg_wait
            "VERIFICATION_NOT_PENDING" -> R.string.reg_not_pending
            "EMAIL_UNAVAILABLE" -> R.string.reg_unavailable
            "VALIDATION_ERROR" -> R.string.reg_fields_error
            "STAGING_ACCESS_DENIED" -> R.string.reg_environment
            "NETWORK_UNCERTAIN" -> R.string.reg_uncertain
            null -> null
            else -> R.string.reg_error
        }
    }
    error?.let { Text(stringResource(it),color=androidx.compose.material3.MaterialTheme.colorScheme.error) }
    if(state.fields.isNotEmpty()) {
        val labels=state.fields.map { stringResource(when(it) {
            "name" -> R.string.reg_names
            "lastName" -> R.string.reg_last_names
            "email" -> R.string.reg_email
            "password" -> R.string.reg_password
            "code" -> R.string.reg_verify
            else -> R.string.reg_fields_error
        }) }
        Text(labels.joinToString(", "),color=androidx.compose.material3.MaterialTheme.colorScheme.error)
    }
    state.notice?.let { Text(stringResource(when(it) {
        RegistrationNotice.PENDING -> R.string.reg_pending
        RegistrationNotice.GENERIC_RESEND -> R.string.reg_generic_resend
        RegistrationNotice.AUTHORIZED_RESEND -> R.string.reg_authorized_resend
        RegistrationNotice.CHANGE_EMAIL -> R.string.reg_change_notice
    })) }
    state.remaining?.let { Text(stringResource(R.string.reg_remaining,it)) }
}
