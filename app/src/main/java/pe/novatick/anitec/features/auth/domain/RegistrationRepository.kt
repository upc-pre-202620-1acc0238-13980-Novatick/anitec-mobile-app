package pe.novatick.anitec.features.auth.domain

enum class RegistrationIssue { FIELDS, PASSWORD_BYTES, PASSWORD_COMPLEXITY, CONFIRMATION, TERMS, OTP, CONTEXT }
class RegistrationFailure(val issue: RegistrationIssue?=null, val status: Int=0, val code: String?=null,
    val fields: List<String> = emptyList(), val retryAt: Long?=null) : Exception("Registration request failed")
class RegistrationForm(val name: String, val lastName: String, val email: String, val password: String,
    val confirmation: String, val role: Role, val terms: Boolean) {
    override fun toString() = "RegistrationForm(REDACTED)"
}
object RegistrationValidation {
    fun email(value: String) = Regex("""[^\s@]+@[^\s@]+\.[^\s@]+""").matches(value.trim())
    fun otp(value: String) = value.length==6 && value.all { it in '0'..'9' }
    fun validate(form: RegistrationForm) {
        if(form.name.trim().isEmpty() || form.name.trim().length>100 || form.lastName.trim().isEmpty() ||
            form.lastName.trim().length>100 || form.email.trim().length>150 || !email(form.email)) throw RegistrationFailure(RegistrationIssue.FIELDS)
        if(form.password.toByteArray(Charsets.UTF_8).size>72) throw RegistrationFailure(RegistrationIssue.PASSWORD_BYTES)
        if(!Regex("""^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9\s]).{8,}$""").matches(form.password))
            throw RegistrationFailure(RegistrationIssue.PASSWORD_COMPLEXITY)
        if(form.password!=form.confirmation) throw RegistrationFailure(RegistrationIssue.CONFIRMATION)
        if(!form.terms) throw RegistrationFailure(RegistrationIssue.TERMS)
    }
}
data class RegistrationResult(val accountId: String, val email: String)
data class ResendResult(val authorized: Boolean, val remaining: Int?=null, val nextAt: Long?=null)
interface RegistrationRepository {
    suspend fun register(form: RegistrationForm, language: String): RegistrationResult
    suspend fun verify(email: String, code: String, password: String?)
    suspend fun resend(email: String, password: String?, language: String): ResendResult
}
