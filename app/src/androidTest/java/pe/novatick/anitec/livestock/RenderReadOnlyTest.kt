package pe.novatick.anitec.livestock

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** No credentials, staging keys, mutating verbs or remote session creation. */
class RenderReadOnlyTest {
    @Test fun emulatorCanReadHealthButApiRequiresStagingGate() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("renderReadOnly")=="true")
        val client=OkHttpClient.Builder().callTimeout(20,TimeUnit.SECONDS)
            .retryOnConnectionFailure(false).followRedirects(false).build()
        client.newCall(Request.Builder().url("https://anitec-staging.onrender.com/actuator/health").get().build()).execute().use {
            assertEquals(200,it.code)
            assertEquals("UP",JSONObject(it.body!!.string()).getString("status"))
        }
        client.newCall(Request.Builder().url("https://anitec-staging.onrender.com/api/v1/account/me").get().build()).execute().use {
            assertEquals(403,it.code)
            assertEquals("STAGING_ACCESS_DENIED",JSONObject(it.body!!.string()).getString("errorCode"))
        }
    }
}
