package pe.novatick.anitec.features.auth.infrastructure

import androidx.annotation.Keep
import retrofit2.Response
import retrofit2.http.*

// Nullable wire fields are validated before mapping; Gson must not bypass domain invariants.
@Keep
data class Envelope<T>(val success: Boolean?, val message: String?, val timestamp: String?, val data: T?)
@Keep
data class FieldError(val field: String?, val error: String?)
@Keep
data class ApiError(val success: Boolean?, val message: String?, val timestamp: String?, val errorCode: String?, val details: List<FieldError>?)
@Keep
class LoginRequest(val email: String, val password: String)
@Keep
class RefreshRequest(val refreshToken: String, val operationId: String)
@Keep
class LogoutRequest(val refreshToken: String)
@Keep
class LoginDto(val accountId: String?, val email: String?, val role: String?, val accessToken: String?, val refreshToken: String?, val expiresIn: Long?)
@Keep
class TokenDto(val accessToken: String?, val refreshToken: String?, val expiresIn: Long?)
@Keep
class ProfileDto(val id: String?, val email: String?, val name: String?, val lastName: String?, val role: String?,
                 val emailVerified: Boolean?, val badge: String?, val createdAt: String?, val address: String?,
                 val dni: String?, val phone: String?, val photoUrl: String?)
@Keep
class RegisterRequest(val name: String, val lastName: String, val email: String, val password: String, val role: String) {
    override fun toString() = "RegisterRequest(REDACTED)"
}
@Keep
class VerifyRequest(val email: String, val code: String, val password: String?) {
    override fun toString() = "VerifyRequest(REDACTED)"
}
@Keep
class ResendRequest(val email: String, val password: String?) {
    override fun toString() = "ResendRequest(REDACTED)"
}
@Keep
class RegisterDto(val accountId: String?, val email: String?, val emailDelivery: String?)
@Keep
class ResendDto(val email: String?, val emailDelivery: String?, val remainingResends: Int?, val nextResendAt: String?)
interface AuthApi {
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest, @Header("Accept-Language") language: String): Response<Envelope<RegisterDto>>
    @POST("auth/verify") suspend fun verify(@Body body: VerifyRequest): Response<Envelope<Unit>>
    @POST("auth/resend-code") suspend fun resend(@Body body: ResendRequest, @Header("Accept-Language") language: String): Response<Envelope<ResendDto>>
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): Response<Envelope<LoginDto>>
    @POST("auth/refresh-token") suspend fun refresh(@Body body: RefreshRequest): Response<Envelope<TokenDto>>
    @POST("auth/logout") suspend fun logout(@Body body: LogoutRequest): Response<Envelope<Unit>>
    @GET("account/me") suspend fun me(@Header("Authorization") bearer: String): Response<Envelope<ProfileDto>>
}
