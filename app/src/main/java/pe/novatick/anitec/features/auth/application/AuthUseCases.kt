package pe.novatick.anitec.features.auth.application
import pe.novatick.anitec.features.auth.domain.AuthRepository
class Login(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, remember: Boolean) = repository.login(email,password,remember)
}
class RestoreSession(private val repository: AuthRepository) { suspend operator fun invoke() = repository.restore() }
class Logout(private val repository: AuthRepository) { operator fun invoke(): suspend () -> pe.novatick.anitec.features.auth.domain.LogoutResult = repository.logoutLocally() }
