package pe.novatick.anitec.features.auth.domain

import kotlinx.coroutines.flow.StateFlow

enum class Role { GANADERO, VETERINARIO }
data class Profile(val id: String, val email: String, val name: String, val lastName: String, val role: Role)
enum class AuthProblem { CREDENTIALS, UNAUTHORIZED, FORBIDDEN, NETWORK, SERVER, INVALID_RESPONSE, STORAGE, RELOGIN }
class AuthFailure(val problem: AuthProblem, val status: Int = 0, val code: String? = null,
                  val fields: List<Pair<String, String>> = emptyList()) : Exception(problem.name)
data class LogoutResult(val local: pe.novatick.anitec.core.session.InvalidationResult, val remoteConfirmed: Boolean)
interface AuthRepository {
    val profile: StateFlow<Profile?>
    suspend fun login(email: String, password: String, remember: Boolean)
    suspend fun restore()
    suspend fun refresh()
    /** Clears local identity before returning the remote-revocation operation. */
    fun logoutLocally(): suspend () -> LogoutResult
}
