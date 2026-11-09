package pe.novatick.anitec.core.di

import android.content.Context
import okhttp3.OkHttpClient
import pe.novatick.anitec.BuildConfig
import pe.novatick.anitec.core.session.KeystoreSessionStore
import pe.novatick.anitec.features.auth.infrastructure.*
import pe.novatick.anitec.features.auth.domain.AuthFailure
import pe.novatick.anitec.features.auth.domain.AuthProblem
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Manual application-scoped DI; no extra DI framework or business feature migration. */
class AuthContainer(context: Context) {
    private val client=OkHttpClient.Builder().connectTimeout(10,TimeUnit.SECONDS).readTimeout(10,TimeUnit.SECONDS)
        .callTimeout(15,TimeUnit.SECONDS).retryOnConnectionFailure(false).followRedirects(false).followSslRedirects(false).build()
    private val api: AuthApi=if(BuildConfig.DEBUG) Retrofit.Builder()
        .baseUrl("http://10.0.2.2:18080/api/v1/")
        .client(client).addConverterFactory(GsonConverterFactory.create()).build().create(AuthApi::class.java) else UnconfiguredReleaseApi()
    val registration = RegistrationRepositoryImpl(api)
    // Keep the closing-generation guard for the whole process, including Activity recreation.
    val repository: AuthRepositoryImpl = synchronized(AuthContainer::class.java) {
        shared ?: AuthRepositoryImpl(api,KeystoreSessionStore(context.applicationContext)).also { shared=it }
    }
    companion object { @Volatile private var shared: AuthRepositoryImpl? = null }
}

/** No production origin has been authorized. Do not turn the documentation placeholder into an API host. */
private class UnconfiguredReleaseApi : AuthApi {
    private fun unavailable(): Nothing = throw AuthFailure(AuthProblem.SERVER,code="CLIENT_ENVIRONMENT_NOT_CONFIGURED")
    override suspend fun register(body: RegisterRequest,language: String): Response<Envelope<RegisterDto>> = unavailable()
    override suspend fun verify(body: VerifyRequest): Response<Envelope<Unit>> = unavailable()
    override suspend fun resend(body: ResendRequest,language: String): Response<Envelope<ResendDto>> = unavailable()
    override suspend fun login(body: LoginRequest): Response<Envelope<LoginDto>> = unavailable()
    override suspend fun refresh(body: RefreshRequest): Response<Envelope<TokenDto>> = unavailable()
    override suspend fun logout(body: LogoutRequest): Response<Envelope<Unit>> = unavailable()
    override suspend fun me(bearer: String): Response<Envelope<ProfileDto>> = unavailable()
}
