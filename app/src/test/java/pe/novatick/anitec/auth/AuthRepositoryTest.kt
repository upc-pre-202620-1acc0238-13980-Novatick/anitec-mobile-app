package pe.novatick.anitec.auth

import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.core.session.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.infrastructure.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var api: AuthApi
    private val account="11111111-1111-4111-8111-111111111111"
    private var time=100000L
    private class MemoryStore : SessionStore {
        var record: SessionRecord?=null
        override fun read()=record
        override fun write(record: SessionRecord) { this.record=record }
        override fun clear() { record=null }
    }
    private lateinit var store: MemoryStore
    @Before fun setup() {
        server=MockWebServer(); server.start();store=MemoryStore()
        api=Retrofit.Builder().client(okhttp3.OkHttpClient.Builder().retryOnConnectionFailure(false).callTimeout(2,TimeUnit.SECONDS).build()).baseUrl(server.url("api/v1/")).addConverterFactory(GsonConverterFactory.create()).build().create(AuthApi::class.java)
    }
    @After fun teardown() { server.shutdown() }
    private fun repo()=AuthRepositoryImpl(api,store) { time }
    private fun response(data: String)=MockResponse().setHeader("Content-Type","application/json")
        .setBody("""{"success":true,"message":"ok","timestamp":"2035-01-01T00:00:00Z","data":$data}""")
    private fun login(role: String="GANADERO")=response("""{"accountId":"$account","email":"person@example.invalid","role":"$role","accessToken":"synthetic-access","refreshToken":"synthetic-refresh","expiresIn":30}""")
    private fun profile(role: String="GANADERO")=response("""{"id":"$account","email":"person@example.invalid","name":"Test","lastName":"Person","role":"$role","emailVerified":true,"phone":null,"photoUrl":null,"address":null,"dni":null,"badge":"","createdAt":"2035-01-01T00:00:00Z"}""")
    private fun pair()=response("""{"accessToken":"synthetic-new-access","refreshToken":"synthetic-new-refresh","expiresIn":30}""")
    private fun error(status: Int,code: String)=MockResponse().setResponseCode(status).setHeader("Content-Type","application/json").setBody("""{"success":false,"message":"not for matching","timestamp":"2035-01-01T00:00:00Z","errorCode":"$code","details":[{"field":"email","error":"invalid"}]}""")
    private suspend fun authenticated(r: AuthRepositoryImpl,remember: Boolean=true,role: String="GANADERO") {
        server.enqueue(login(role));server.enqueue(profile(role));r.login("person@example.invalid","synthetic-password",remember)
    }
    private suspend fun failure(block: suspend ()->Unit): AuthFailure {
        try { block();fail("Expected AuthFailure") } catch(f: AuthFailure) { return f }
        error("unreachable")
    }
    @Test fun loginBothRolesUsesServerRoleAndValidatesProtectedProfile()=runBlocking {
        for(role in Role.entries) {
            val r=repo();authenticated(r,role=role.name)
            assertEquals(role,r.profile.value!!.role)
            assertEquals("/api/v1/auth/login",server.takeRequest().path)
            val me=server.takeRequest();assertEquals("/api/v1/account/me",me.path)
            assertEquals("Bearer synthetic-access",me.getHeader("Authorization"))
        }
    }
    @Test fun invalidCredentialsUseErrorCodeAndFields()=runBlocking {
        server.enqueue(error(401,"INVALID_CREDENTIALS"));val r=repo()
        val f=failure { r.login("x","x",true) }
        assertEquals(AuthProblem.CREDENTIALS,f.problem);assertEquals("INVALID_CREDENTIALS",f.code)
        assertEquals("email",f.fields.single().first);assertNull(r.profile.value);assertNull(store.record)
    }
    @Test fun unrememberedSessionIsNotRestoredInNewProcessEquivalent()=runBlocking {
        val r=repo();authenticated(r,false);assertNotNull(r.profile.value);assertNull(store.record)
        val next=repo();next.restore();assertNull(next.profile.value);assertEquals(2,server.requestCount)
    }
    @Test fun rememberedSessionRequiresProtectedAuthorizationOnRestore()=runBlocking {
        authenticated(repo());server.enqueue(profile());val next=repo();next.restore()
        assertNotNull(next.profile.value);assertEquals(3,server.requestCount)
    }
    @Test fun restoredUnauthorizedSessionIsCleared()=runBlocking {
        authenticated(repo());server.enqueue(error(401,"SESSION_INVALID"));val r=repo()
        assertEquals(AuthProblem.UNAUTHORIZED,failure { r.restore() }.problem)
        assertNull(store.record);assertNull(r.profile.value)
    }
    @Test fun forbiddenNeverRefreshesAndPreservesSessionForRetry()=runBlocking {
        authenticated(repo());server.enqueue(error(403,"FORBIDDEN"));val r=repo()
        assertEquals(AuthProblem.FORBIDDEN,failure { r.restore() }.problem)
        assertEquals(3,server.requestCount);assertNotNull(store.record);assertNull(r.profile.value)
    }
    @Test fun concurrentRefreshHasOneRotation()=runBlocking {
        val r=repo();authenticated(r);server.enqueue(pair().setBodyDelay(200,TimeUnit.MILLISECONDS))
        coroutineScope { (1..12).map { async(Dispatchers.Default) { r.refresh() } }.awaitAll() }
        assertEquals(3,server.requestCount);assertNull(store.record!!.pendingId)
    }
    @Test fun pendingConfirmationSurvivesRestartAndReusesExactOperation()=runBlocking {
        val r=repo();authenticated(r);server.enqueue(error(503,"REFRESH_CONFIRMATION_PENDING"))
        assertEquals("REFRESH_CONFIRMATION_PENDING",failure { r.refresh() }.code)
        assertNotNull(store.record!!.pendingId)
        server.enqueue(pair());server.enqueue(profile());repo().restore()
        server.takeRequest();server.takeRequest()
        val first=server.takeRequest().body.readUtf8();val retry=server.takeRequest().body.readUtf8()
        assertEquals(first,retry)
        val request=Gson().fromJson(first,Map::class.java)
        assertEquals(4,java.util.UUID.fromString(request["operationId"] as String).version())
    }
    @Test fun expiredPendingOperationRequiresLoginWithoutAnotherRotation()=runBlocking {
        val r=repo();authenticated(r);server.enqueue(error(503,"REFRESH_CONFIRMATION_PENDING"));failure { r.refresh() }
        time+=60000
        assertEquals(AuthProblem.RELOGIN,failure { repo().restore() }.problem)
        assertNull(store.record);assertEquals(3,server.requestCount)
    }
    @Test fun lateRefreshCannotRestoreLoggedOutSession()=runBlocking {
        val r=repo();authenticated(r);server.enqueue(pair().setBodyDelay(300,TimeUnit.MILLISECONDS))
        val job=async { runCatching { r.refresh() } }
        while(server.requestCount<3) delay(5)
        val revoke=r.logoutLocally();assertNull(r.profile.value);assertNull(store.record)
        job.await();assertNull(r.profile.value);assertNull(store.record)
        server.enqueue(response("null"));assertTrue(revoke().remoteConfirmed);assertEquals(4,server.requestCount)
        val requests=(1..4).map { server.takeRequest() };assertEquals("/api/v1/auth/logout",requests.last().path)
        assertNull(requests.last().getHeader("Authorization"))
    }
    @Test fun failedRemoteLogoutStillClearsLocalSession()=runBlocking {
        val r=repo();authenticated(r);val revoke=r.logoutLocally()
        assertNull(r.profile.value);assertNull(store.record)
        server.enqueue(error(500,"INTERNAL_ERROR"));assertFalse(revoke().remoteConfirmed)
    }
    @Test fun profileIdentityMismatchNeverAuthenticates()=runBlocking {
        server.enqueue(login());server.enqueue(profile("VETERINARIO"));val r=repo()
        assertEquals(AuthProblem.INVALID_RESPONSE,failure { r.login("x","x",true) }.problem)
        assertNull(r.profile.value);assertNull(store.record)
    }
    @Test fun missingTokenOrUnknownRoleIsRejected()=runBlocking {
        server.enqueue(response("""{"accountId":"$account","role":"ADMIN"}"""))
        assertEquals(AuthProblem.INVALID_RESPONSE,failure { repo().login("x","x",true) }.problem)
    }
    @Test fun storageFailureCannotPublishAuthenticatedIdentity()=runBlocking {
        val broken=object: SessionStore {
            override fun read(): SessionRecord?=null
            override fun clear() {}
            override fun write(record: SessionRecord) { throw java.io.IOException("test") }
        }
        val r=AuthRepositoryImpl(api,broken) { time };server.enqueue(login());server.enqueue(profile())
        assertEquals(AuthProblem.STORAGE,failure { r.login("x","x",true) }.problem);assertNull(r.profile.value)
    }
    @Test fun networkFailureOnRestoreDoesNotRevokeRememberedSession()=runBlocking {
        authenticated(repo());server.shutdown();val r=repo()
        assertEquals(AuthProblem.NETWORK,failure { r.restore() }.problem)
        assertNotNull(store.record);assertNull(r.profile.value)
    }
    @Test fun lostRefreshResponseRetainsPendingRequestForProcessRestart()=runBlocking {
        val r=repo();authenticated(r)
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        assertEquals(AuthProblem.NETWORK,failure { r.refresh() }.problem)
        assertNotNull(store.record!!.pendingId)
        server.enqueue(pair());server.enqueue(profile());repo().restore()
        server.takeRequest();server.takeRequest()
        assertEquals(server.takeRequest().body.readUtf8(),server.takeRequest().body.readUtf8())
    }
    @Test fun expiredAccessDuringProfileValidationRotatesOnceThenRevalidates()=runBlocking {
        authenticated(repo());server.enqueue(error(401,"ACCESS_TOKEN_EXPIRED"));server.enqueue(pair());server.enqueue(profile())
        val r=repo();r.restore();assertNotNull(r.profile.value);assertEquals(5,server.requestCount)
    }
    @Test fun serverErrorDuringRestoreDoesNotSilentlyAuthorizeOrEraseSession()=runBlocking {
        authenticated(repo());server.enqueue(error(503,"AUTHENTICATION_UNAVAILABLE"));val r=repo()
        assertEquals(AuthProblem.SERVER,failure { r.restore() }.problem)
        assertNull(r.profile.value);assertNotNull(store.record)
    }

    @Test fun lateLoginCannotRestoreLoggedOutSession()=runBlocking {
        val r=repo();server.enqueue(login().setBodyDelay(200,TimeUnit.MILLISECONDS));server.enqueue(profile())
        val job=async { runCatching { r.login("x","x",true) } }
        while(server.requestCount<1) delay(5)
        r.logoutLocally();job.await();assertNull(r.profile.value);assertNull(store.record)
    }

}
