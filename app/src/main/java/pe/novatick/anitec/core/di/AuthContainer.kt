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
    private val pool=okhttp3.ConnectionPool()
    private val client=pe.novatick.anitec.core.environment.VariantAccess.configure(OkHttpClient.Builder().connectTimeout(10,TimeUnit.SECONDS).readTimeout(10,TimeUnit.SECONDS)
        .callTimeout(15,TimeUnit.SECONDS).connectionPool(pool).addInterceptor(pe.novatick.anitec.core.network.ReadEofRecovery(pool)).eventListenerFactory(if(BuildConfig.DEBUG) pe.novatick.anitec.core.network.NetworkDiagnostics.factory else okhttp3.EventListener.Factory { okhttp3.EventListener.NONE }).retryOnConnectionFailure(false).followRedirects(false).followSslRedirects(false)).build()
    private val retrofit = Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()
    private val api: AuthApi=retrofit.create(AuthApi::class.java)
    val registration = RegistrationRepositoryImpl(api)
    // Keep the closing-generation guard for the whole process, including Activity recreation.
    val repository: AuthRepositoryImpl = synchronized(AuthContainer::class.java) {
        shared ?: AuthRepositoryImpl(api,KeystoreSessionStore(context.applicationContext)).also { shared=it }
    }
    val livestock = pe.novatick.anitec.features.livestock.infrastructure.LivestockRepositoryImpl(
        retrofit.create(pe.novatick.anitec.features.livestock.infrastructure.LivestockApi::class.java),repository) {repository.profile.value?.id}
    companion object { @Volatile private var shared: AuthRepositoryImpl? = null }
}
