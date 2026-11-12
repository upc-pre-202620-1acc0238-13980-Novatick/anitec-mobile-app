package pe.novatick.anitec.linking
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.core.network.AuthorizedRequests
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.infrastructure.Envelope
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.linking.infrastructure.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
class LinkingRepositoryTest {
 private val server=MockWebServer()
 private val farmer="10000000-0000-4000-8000-000000000001";private val vet="20000000-0000-4000-8000-000000000001"
 private val invitation="30000000-0000-4000-8000-000000000001";private val link="40000000-0000-4000-8000-000000000001"
 private var role=Role.GANADERO
 private val requests=object: AuthorizedRequests {override suspend fun <T> execute(request: suspend (String)->Response<Envelope<T>>):T {
  val r=request("Bearer synthetic");if(!r.isSuccessful)throw AuthFailure(AuthProblem.SERVER,r.code());return r.body()?.data ?: error("Missing body")
 }}
 private lateinit var repo: LinkingRepositoryImpl
 @Before fun setup(){server.start();val api=Retrofit.Builder().baseUrl(server.url("/api/v1/")).client(OkHttpClient.Builder().retryOnConnectionFailure(false).build()).addConverterFactory(GsonConverterFactory.create()).build().create(LinkingApi::class.java)
 repo=LinkingRepositoryImpl(api,requests){Profile(if(role==Role.GANADERO)farmer else vet,"qa@example.invalid","QA","Test",role)}}
 @After fun close(){server.shutdown()}
 private fun reply(data: String,status: Int=200){server.enqueue(MockResponse().setResponseCode(status).setHeader("Content-Type","application/json").setBody("""{"success":true,"message":"ok","timestamp":"2026-11-11T12:00:00Z","data":$data}"""))}
 private fun invitation(status: String)="""{"invitationId":"$invitation","vetEmail":"vet@example.invalid","status":"$status","emailDelivery":"UNCONFIRMED","sentAt":"2026-11-11T12:00:00Z"}"""
 @Test fun postKeepsPendingAndUnconfirmedAndLanguage()=runBlocking {
  reply(invitation("PENDIENTE"),201);val d=repo.invite(" vet@example.invalid ","en");assertEquals("PENDIENTE",d.status);assertEquals("UNCONFIRMED",d.delivery)
  val r=server.takeRequest();assertEquals("POST",r.method);assertEquals("/api/v1/invitations",r.path);assertEquals("en",r.getHeader("Accept-Language"));assertEquals("Bearer synthetic",r.getHeader("Authorization"));assertEquals("""{"vetEmail":"vet@example.invalid"}""",r.body.readUtf8())
 }
 @Test fun wrongRoleNeverDispatchesInvitation()=runBlocking {role=Role.VETERINARIO;try {repo.invite("vet@example.invalid","es");fail()}catch(e: AuthFailure){assertEquals(403,e.status)};assertEquals(0,server.requestCount)}
 @Test fun invalidEmailNeverDispatches()=runBlocking {for(v in listOf("", "bad", "a b@c.invalid", "x".repeat(150)+"@a.invalid")){try{repo.invite(v,"es");fail()}catch(e: AuthFailure){assertEquals(400,e.status)}};assertEquals(0,server.requestCount)}
 @Test fun pendingHasOnlyAuthorizedDtoFields()=runBlocking {role=Role.VETERINARIO;reply("""[{"invitationId":"$invitation","farmerName":"Luis","sentDate":"2026-11-11"}]""");assertEquals("Luis",repo.pending().single().farmerName);assertEquals("/api/v1/invitations/pending",server.takeRequest().path)}
 @Test fun acceptAndRejectUseExactUuidAndAction()=runBlocking {role=Role.VETERINARIO;for(accept in listOf(true,false)){reply(invitation(if(accept)"ACEPTADA" else "RECHAZADA"));repo.respond(invitation,accept);val r=server.takeRequest();assertEquals("PUT",r.method);assertEquals("/api/v1/invitations/$invitation/respond",r.path);assertEquals("""{"action":"${if(accept)"ACCEPT" else "REJECT"}"}""",r.body.readUtf8())}}
 @Test fun alienLinksAreRejected()=runBlocking {reply("""{"activeLinksCount":1,"links":[{"linkId":"$link","farmerId":"$vet","farmerName":"Other","vetId":"$vet","vetName":"Vet","linkedAt":"2026-11-11T12:00:00Z","status":"ACTIVA"}]}""");try{repo.links();fail()}catch(e:AuthFailure){assertEquals(AuthProblem.INVALID_RESPONSE,e.problem)}}
 @Test fun validLinkPreservesIdsAndState()=runBlocking {reply("""{"activeLinksCount":1,"links":[{"linkId":"$link","farmerId":"$farmer","farmerName":"Luis","vetId":"$vet","vetName":"Maria","linkedAt":"2026-11-11T12:00:00Z","status":"ACTIVA"}]}""");assertEquals(link,repo.links().single().id)}
 @Test fun capacityUsesServerLimitIncludingOverCapacity()=runBlocking {role=Role.VETERINARIO;reply("""{"vetId":"$vet","allowedRanchers":3,"activeLinks":4,"lastPlanRevision":2}""");assertEquals(4,repo.capacity().active);assertEquals("/api/v1/vet-links/capacity",server.takeRequest().path)}
 @Test fun farmersUseOnlyServerFarmsAndCounts()=runBlocking {role=Role.VETERINARIO;reply("""{"activeLinksCount":1,"farmers":[{"farmerId":"$farmer","farmerName":"Luis","animalsCount":7,"farms":[]}]}""");assertEquals(7L,repo.farmers().single().animals);assertEquals("/api/v1/vet-links/my-farmers",server.takeRequest().path)}
 @Test fun malformedUuidAndWrongRolesFailBeforeNetwork()=runBlocking {try{repo.revoke("bad");fail()}catch(e:AuthFailure){assertEquals(AuthProblem.INVALID_RESPONSE,e.problem)};for(call in listOf<suspend ()->Any>({repo.pending()},{repo.farmers()},{repo.capacity()},{repo.respond(invitation,true)})){try{call();fail()}catch(e:AuthFailure){assertEquals(403,e.status)}};assertEquals(0,server.requestCount)}
 @Test fun revokeConfirmsServerRevokedState()=runBlocking {reply("""{"linkId":"$link","status":"REVOCADA","revokedAt":"2026-11-11T12:00:00Z"}""");repo.revoke(link);val r=server.takeRequest();assertEquals("DELETE",r.method);assertEquals("/api/v1/vet-links/$link",r.path)}
 @Test fun conflictsAndRateLimitsAreNotRetried()=runBlocking {for(status in listOf(400,401,403,404,409,422,429,500)){server.enqueue(MockResponse().setResponseCode(status));try{repo.invite("vet@example.invalid","es");fail()}catch(e:AuthFailure){assertEquals(status,e.status)};server.takeRequest()};assertEquals(8,server.requestCount)}
}
