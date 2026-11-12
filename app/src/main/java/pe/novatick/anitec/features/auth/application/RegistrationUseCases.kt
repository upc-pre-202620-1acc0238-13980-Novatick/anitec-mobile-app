package pe.novatick.anitec.features.auth.application
import pe.novatick.anitec.features.auth.domain.*
class RegisterAccount(private val repository: RegistrationRepository) {
    suspend operator fun invoke(form: RegistrationForm,language: String) = repository.register(form,language)
}
class VerifyAccount(private val repository: RegistrationRepository) {
    suspend operator fun invoke(email: String,code: String,password: String?) = repository.verify(email,code,password)
}
class ResendVerification(private val repository: RegistrationRepository) {
    suspend operator fun invoke(email: String,password: String?,language: String) = repository.resend(email,password,language)
}
