package pe.novatick.anitec.auth
import okhttp3.*
import okhttp3.mockwebserver.*
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.core.network.ReadEofRecovery
import java.util.concurrent.TimeUnit
import java.io.IOException

class ReadEofRecoveryTest {
    private fun client(timeout: Long=2000): OkHttpClient {
        val pool=ConnectionPool()
        return OkHttpClient.Builder().connectionPool(pool).addInterceptor(ReadEofRecovery(pool))
            .retryOnConnectionFailure(false).callTimeout(timeout,TimeUnit.MILLISECONDS).build()
    }
    @Test fun getHeaderEofRecoversOnce() {
        MockWebServer().use {server->
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
            server.enqueue(MockResponse().setBody("confirmed"))
            client().newCall(Request.Builder().url(server.url("/read")).build()).execute().use {assertEquals("confirmed",it.body!!.string())}
            assertEquals(2,server.requestCount)
        }
    }
    @Test fun secondEofIsNotRetriedAgain() {
        MockWebServer().use {server->
            repeat(2) {server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))}
            assertTrue(runCatching {client().newCall(Request.Builder().url(server.url("/read")).build()).execute()}.exceptionOrNull() is IOException)
            assertEquals(2,server.requestCount)
        }
    }
    @Test fun postPutDeleteNeverRepeatAfterLostResponse() {
        for(method in listOf("POST","PUT","DELETE")) MockWebServer().use {server->
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
            val request=Request.Builder().url(server.url("/mutation")).method(method,RequestBody.create(null,"synthetic")).build()
            assertTrue(runCatching {client().newCall(request).execute()}.exceptionOrNull() is IOException)
            assertEquals(1,server.requestCount)
        }
    }
    @Test fun httpErrorsAreNotTransportRecoveryCandidates() {
        for(status in listOf(401,403,500,503)) MockWebServer().use {server->
            server.enqueue(MockResponse().setResponseCode(status))
            client().newCall(Request.Builder().url(server.url("/read")).build()).execute().use {assertEquals(status,it.code)}
            assertEquals(1,server.requestCount)
        }
    }
    @Test fun timeoutIsNotRetried() {
        MockWebServer().use {server->
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            assertTrue(runCatching {client(200).newCall(Request.Builder().url(server.url("/read")).build()).execute()}.exceptionOrNull() is IOException)
            assertEquals(1,server.requestCount)
        }
    }
}
