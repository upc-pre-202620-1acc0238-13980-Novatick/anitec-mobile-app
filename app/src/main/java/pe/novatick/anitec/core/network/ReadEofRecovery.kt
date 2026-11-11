package pe.novatick.anitec.core.network

import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/** Recover one header EOF on a read, without enabling OkHttp's retries for mutations. */
internal class ReadEofRecovery(private val pool: ConnectionPool): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request=chain.request()
        return try {chain.proceed(request)} catch(e: IOException) {
            if(request.method!="GET" || chain.call().isCanceled() ||
                e.message?.contains("unexpected end of stream",ignoreCase=true)!=true) throw e
            // Only idle connections are closed; in-flight requests are not interrupted.
            pool.evictAll()
            // Same Call: cancellation and the original overall timeout remain in force.
            chain.proceed(request)
        }
    }
}
