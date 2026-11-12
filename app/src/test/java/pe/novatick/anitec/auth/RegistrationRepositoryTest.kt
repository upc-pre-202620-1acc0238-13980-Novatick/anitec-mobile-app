package pe.novatick.anitec.auth

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.infrastructure.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RegistrationRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: RegistrationRepository
    private val email="new@example.invalid"
    private val id="11111111-1111-4111-8111-111111111111"
    @Before fun setup() {
        server=MockWebServer(); server.start()
        val client=okhttp3.OkHttpClient.Builder().retryOnConnectionFailure(false).callTimeout(2,TimeUnit.SECONDS).build()
        val api=Retrofit.Builder().client(client).baseUrl(server.url("api/v1/"))
            .addConverterFactory(GsonConverterFactory.create()).build().create(AuthApi::class.java)
        repository=RegistrationRepositoryImpl(api) { 1000L }
    }
    @After fun stop() { server.shutdown() }
    private fun form(password: String="Aa1!test",role: Role=Role.GANADERO,name: String="New",lastName: String="Person",email: String=this.email,
        confirmation: String=password,terms: Boolean=true)=RegistrationForm(name,lastName,email,password,confirmation,role,terms)
    private fun ok(status: Int,data: String?=null)=MockResponse().setResponseCode(status).setHeader("Content-Type","application/json")
        .setBody("""{"success":true,"message":"ok","timestamp":"2035-01-01T00:00:00Z"${if(data==null) "" else ",\"data\":"+data}}""")
    private fun registered()=ok(201,"""{"accountId":"$id","email":"$email","emailDelivery":"UNCONFIRMED"}""")
    private fun error(status: Int,code: String,details: String="[]")=MockResponse().setResponseCode(status)
        .setBody("""{"success":false,"message":"untrusted","timestamp":"2035-01-01T00:00:00Z","errorCode":"$code","details":$details}""")
    private suspend fun failure(block: suspend ()->Unit): RegistrationFailure {
        try { block(); fail("Expected controlled failure") } catch(e: RegistrationFailure) { return e }
        error("unreachable")
    }
    @Test fun asciiBoundaries72And73BytesNeverTruncate() {
        val at="Aa1!"+"x".repeat(68)
        RegistrationValidation.validate(form(at))
        assertEquals(72,at.toByteArray(Charsets.UTF_8).size)
        val issue=runCatching { RegistrationValidation.validate(form(at+"x")) }.exceptionOrNull() as RegistrationFailure
        assertEquals(RegistrationIssue.PASSWORD_BYTES,issue.issue)
    }
    @Test fun unicodeBoundaries72And73BytesIncludeSupplementaryCharacters() {
        for(at in listOf("Aa1!"+"é".repeat(34),"Aa1!"+"😀".repeat(17))) {
            assertEquals(72,at.toByteArray(Charsets.UTF_8).size)
            RegistrationValidation.validate(form(at))
            assertEquals(RegistrationIssue.PASSWORD_BYTES,
                (runCatching { RegistrationValidation.validate(form(at+"a")) }.exceptionOrNull() as RegistrationFailure).issue)
        }
    }
    @Test fun validationEnforcesComplexityConfirmationTermsAndFields() {
        val invalid=listOf(form(name=" "),form(lastName="x".repeat(101)),form(email="bad"),form(email="x".repeat(151)+"@test.invalid"),
            form(password="aaa11111"),form(password="AAaa11\n!"),form(confirmation="different"),form(terms=false))
        for(f in invalid) assertTrue(runCatching { RegistrationValidation.validate(f) }.exceptionOrNull() is RegistrationFailure)
        assertFalse(form().toString().contains("Aa1!"))
        assertFalse(RegisterRequest("n","l",email,"secret","GANADERO").toString().contains("secret"))
        assertFalse(VerifyRequest(email,"001234","secret").toString().contains("001234"))
    }
    @Test fun otpIsAsciiAndPreservesLeadingZeros() {
        assertTrue(RegistrationValidation.otp("001234"))
        for(v in listOf("", "12345", "1234567", "１２３４５６", "٠١٢٣٤٥", " 12345")) assertFalse(RegistrationValidation.otp(v))
    }
    @Test fun registrationBothRolesUsesPublicHeadersAndUnmodifiedPassword()=runBlocking {
        for(role in Role.entries) {
            server.enqueue(registered())
            val secret=" Aa1!étest "
            val result=repository.register(form(secret,role),"en");assertEquals(email,result.email)
            val request=server.takeRequest();assertEquals("/api/v1/auth/register",request.path)
            assertNull(request.getHeader("Authorization"));assertEquals("en",request.getHeader("Accept-Language"))
            val json=Gson().fromJson(request.body.readUtf8(),Map::class.java)
            assertEquals(secret,json["password"]);assertEquals(role.name,json["role"])
            assertFalse(json.containsKey("confirmation"));assertFalse(json.containsKey("terms"))
        }
    }
    @Test fun verify200WithoutDataIsSuccessAndKeepsCodeString()=runBlocking {
        server.enqueue(ok(200));repository.verify(email,"001234",null)
        val request=server.takeRequest();assertEquals("/api/v1/auth/verify",request.path);assertNull(request.getHeader("Authorization"))
        val data=Gson().fromJson(request.body.readUtf8(),Map::class.java)
        assertEquals("001234",data["code"]);assertFalse(data.containsKey("password"))
    }
    @Test fun resendGeneric202DoesNotConfirmEmission()=runBlocking {
        server.enqueue(ok(202));val result=repository.resend(email,null,"es")
        assertFalse(result.authorized);assertNull(result.remaining);assertNull(result.nextAt)
        val request=server.takeRequest();assertNull(request.getHeader("Authorization"));assertEquals("es",request.getHeader("Accept-Language"))
    }
    @Test fun resendAuthorizedCarriesServerLimitsWithoutClaimingDelivery()=runBlocking {
        server.enqueue(ok(202,"""{"email":"$email","emailDelivery":"UNCONFIRMED","remainingResends":4,"nextResendAt":"2035-01-01T00:01:00.123456Z"}"""))
        val result=repository.resend(email,"Aa1!"+"x".repeat(100),"en")
        assertTrue(result.authorized);assertEquals(4,result.remaining);assertNotNull(result.nextAt)
        assertEquals(1,server.requestCount) // No login/refresh/auto retry.
    }
    @Test fun verifyAndResendKeep256CharacterContextContract()=runBlocking {
        server.enqueue(ok(200));repository.verify(email,"000001","x".repeat(256))
        assertEquals(RegistrationIssue.CONTEXT,failure { repository.verify(email,"000001","x".repeat(257)) }.issue)
        assertEquals(RegistrationIssue.CONTEXT,failure { repository.resend(email,"x".repeat(257),"es") }.issue)
        assertEquals(1,server.requestCount)
    }
    @Test fun badRegistrationNeverReachesTransport()=runBlocking {
        assertEquals(RegistrationIssue.PASSWORD_BYTES,failure { repository.register(form("Aa1!"+"x".repeat(69)),"es") }.issue)
        assertEquals(0,server.requestCount)
    }
    @Test fun errorsPreserveCodesAndFieldNamesWithoutRetryMetadataAsField()=runBlocking {
        server.enqueue(error(400,"VALIDATION_ERROR","""[{"field":"email","error":"invalid"}]"""))
        val bad=failure { repository.register(form(),"es") };assertEquals(listOf("email"),bad.fields)
        server.enqueue(error(409,"EMAIL_ALREADY_REGISTERED"));assertEquals("EMAIL_ALREADY_REGISTERED",failure { repository.register(form(),"es") }.code)
        for(code in listOf("INVALID_CODE","CODE_EXPIRED","CODE_ATTEMPTS_EXCEEDED")) {
            server.enqueue(error(422,code));assertEquals(code,failure { repository.verify(email,"001234","secret") }.code)
        }
        server.enqueue(error(409,"VERIFICATION_NOT_PENDING"));assertEquals("VERIFICATION_NOT_PENDING",failure { repository.resend(email,"secret","es") }.code)
        server.enqueue(error(503,"EMAIL_UNAVAILABLE"));assertEquals("EMAIL_UNAVAILABLE",failure { repository.register(form(),"es") }.code)
        assertEquals(7,server.requestCount)
    }
    @Test fun rateLimitUsesMostConservativeServerDeadline()=runBlocking {
        server.enqueue(error(429,"RESEND_COOLDOWN","""[{"field":"retryAt","error":"2035-01-01T00:00:00Z"}]""").setHeader("Retry-After","60"))
        val e=failure { repository.resend(email,"secret","es") }
        assertTrue(e.retryAt!!>61000);assertTrue(e.fields.isEmpty());assertEquals(1,server.requestCount)
    }
    @Test fun missingRequiredDataAndMalformedResponsesAreRejected()=runBlocking {
        server.enqueue(ok(201));assertEquals("INVALID_RESPONSE",failure { repository.register(form(),"es") }.code)
        server.enqueue(MockResponse().setResponseCode(200).setBody("invalid"));assertEquals("INVALID_RESPONSE",failure { repository.verify(email,"001234",null) }.code)
        server.enqueue(ok(202,"""{"email":"other@example.invalid"}"""));assertEquals("INVALID_RESPONSE",failure { repository.resend(email,"secret","es") }.code)
    }
    @Test fun lostMutationResponseNeverRetriesAutomatically()=runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        assertEquals("NETWORK_UNCERTAIN",failure { repository.register(form(),"es") }.code)
        assertEquals(1,server.requestCount)
    }
    @Test fun existingVerificationContextDoesNotInheritRegistrationEmailLimit()=runBlocking {
        val longEmail="a".repeat(60)+"@"+"b".repeat(60)+"."+"c".repeat(60)+".invalid"
        assertTrue(longEmail.length>150)
        server.enqueue(ok(200));repository.verify(longEmail,"001234",null)
        server.enqueue(ok(202));assertFalse(repository.resend(longEmail,null,"es").authorized)
        assertEquals(2,server.requestCount)
        assertEquals(RegistrationIssue.FIELDS,
            (runCatching { RegistrationValidation.validate(form(email=longEmail)) }.exceptionOrNull() as RegistrationFailure).issue)
    }
}
