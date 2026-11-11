package pe.novatick.anitec.core.environment

import okhttp3.*
import okhttp3.mockwebserver.*
import okio.ByteString.Companion.toByteString
import org.junit.After
import org.junit.Test
import org.junit.Assert.*

class StagingHeaderTest {
    // Synthetic bytes for isolated tests; no operational credential is used.
    private val synthetic=ByteArray(32) {(it+1).toByte()}.toByteString().base64()
    @After fun clear() {MemoryStagingKey.clear()}
    @Test fun onlyCanonical32ByteKeysAreAccepted() {
        assertTrue(MemoryStagingKey.valid(synthetic))
        for(value in listOf("",synthetic.trimEnd('=')," $synthetic",synthetic+"\n","-".repeat(43)+"=","AAAA")) assertFalse(MemoryStagingKey.valid(value))
    }
    @Test fun officialHttpsApiGetsOneHeaderAndJwtIsUnchanged() {
        val request=Request.Builder().url("https://anitec-staging.onrender.com/api/v1/account/me")
            .header("Authorization","Bearer synthetic-jwt").addHeader(StagingHeader.NAME,"old-one").addHeader(StagingHeader.NAME,"old-two").build()
        val prepared=StagingHeader.prepare(request,synthetic)
        assertEquals(listOf(synthetic),prepared.headers.values(StagingHeader.NAME))
        assertEquals("Bearer synthetic-jwt",prepared.header("Authorization"))
    }
    @Test fun noHeaderForOtherOriginsPathsPortsOrUserInfo() {
        for(url in listOf("http://anitec-staging.onrender.com/api/v1/account/me",
            "https://anitec-staging.onrender.com.evil.invalid/api/v1/account/me",
            "https://other.invalid/api/v1/account/me","https://anitec-staging.onrender.com:444/api/v1/account/me",
            "https://user@anitec-staging.onrender.com/api/v1/account/me",
            "https://anitec-staging.onrender.com/actuator/health","https://anitec-staging.onrender.com/other")) {
            val request=Request.Builder().url(url).header(StagingHeader.NAME,synthetic).build()
            assertNull(StagingHeader.prepare(request,synthetic).header(StagingHeader.NAME))
        }
    }
    @Test fun clearingMemoryRemovesAccessWithoutTouchingJwt() {
        MemoryStagingKey.set(synthetic);assertTrue(MemoryStagingKey.ready.value)
        MemoryStagingKey.clear();assertFalse(MemoryStagingKey.ready.value);assertNull(MemoryStagingKey.read())
        val r=Request.Builder().url("https://anitec-staging.onrender.com/api/v1/animals").header("Authorization","Bearer synthetic-jwt").build()
        assertNull(StagingHeader.prepare(r,MemoryStagingKey.read()).header(StagingHeader.NAME))
        assertEquals("Bearer synthetic-jwt",StagingHeader.prepare(r,null).header("Authorization"))
    }
    @Test fun alphaClientDoesNotFollowRedirects() {
        MemoryStagingKey.set(synthetic)
        MockWebServer().use {first->MockWebServer().use {second->
            first.enqueue(MockResponse().setResponseCode(302).setHeader("Location",second.url("/target")))
            val client=VariantAccess.configure(OkHttpClient.Builder().retryOnConnectionFailure(false)).build()
            client.newCall(Request.Builder().url(first.url("/start")).build()).execute().use {assertEquals(302,it.code)}
            assertEquals(1,first.requestCount);assertEquals(0,second.requestCount)
            assertNull(first.takeRequest().getHeader(StagingHeader.NAME))
        }}
    }
}
