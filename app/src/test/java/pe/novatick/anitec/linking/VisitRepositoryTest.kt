package pe.novatick.anitec.linking
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.core.network.AuthorizedRequests
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.infrastructure.Envelope
import pe.novatick.anitec.features.linking.infrastructure.*
import pe.novatick.anitec.features.livestock.infrastructure.LivestockApi
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
class VisitRepositoryTest {
 private val server=MockWebServer();private var role=Role.VETERINARIO
 private val owner="10000000-0000-4000-8000-000000000001";private val animal="60000000-0000-4000-8000-000000000001";private val farm="50000000-0000-4000-8000-000000000001";private val vet="20000000-0000-4000-8000-000000000001";private val visit="80000000-0000-4000-8000-000000000001";private val at="2099-01-02T15:30:00Z"
 private lateinit var repo:LinkingRepositoryImpl
 @Before fun start(){server.start();val retrofit=Retrofit.Builder().baseUrl(server.url("/api/v1/")).client(OkHttpClient.Builder().retryOnConnectionFailure(false).readTimeout(1,java.util.concurrent.TimeUnit.SECONDS).build()).addConverterFactory(GsonConverterFactory.create()).build();val requests=object:AuthorizedRequests{override suspend fun <T> execute(request:suspend(String)->Response<Envelope<T>>):T{val r=request("Bearer synthetic");if(!r.isSuccessful)throw AuthFailure(AuthProblem.SERVER,r.code());return r.body()!!.data!!}};repo=LinkingRepositoryImpl(retrofit.create(LinkingApi::class.java),requests,retrofit.create(LivestockApi::class.java)){Profile(vet,"qa@example.invalid","QA","Vet",role)}}
 @After fun stop(){server.shutdown()}
 private fun reply(data:String){server.enqueue(MockResponse().setHeader("Content-Type","application/json").setBody("""{"success":true,"message":"ok","data":$data}"""))}
 private fun scope(){reply("""{"activeLinksCount":1,"farmers":[{"farmerId":"$owner","farmerName":"Owner","animalsCount":1,"farms":[{"farmId":"$farm","farmName":"Farm","animalsCount":1}]}]}""");reply("""{"id":"$animal","code":"QA","species":"Bovino","speciesId":"70000000-0000-4000-8000-000000000001","sex":"HEMBRA","status":"ACTIVO","farmId":"$farm","farmName":"Farm","version":0}""")}
 private fun response(id:String=visit)="""{"id":"$id","animalId":"$animal","farmerId":"$owner","scheduledAt":"$at","type":"VISITA","status":"PROGRAMADA","reason":"Control"}"""
 @Test fun scheduleUsesOfficialPayloadAndRequires201()=runBlocking{scope();server.enqueue(MockResponse().setResponseCode(201).setHeader("Content-Type","application/json").setBody("""{"success":true,"data":${response()}}"""));assertEquals(visit,repo.scheduleVisit(owner,animal,at,"Control"));server.takeRequest();server.takeRequest();val request=server.takeRequest();assertEquals("POST",request.method);assertEquals("/api/v1/visits/schedule",request.path);val json=com.google.gson.JsonParser.parseString(request.body.readUtf8()).asJsonObject;assertEquals(animal,json["animalId"].asString);assertEquals(at,json["scheduledAt"].asString);assertEquals("Control",json["reason"].asString);assertEquals(3,json.size())}
 @Test fun invalidInputAndRoleFailBeforeDispatch()=runBlocking{role=Role.GANADERO;try{repo.scheduleVisit(owner,animal,at,"Control");fail()}catch(e:AuthFailure){assertEquals(403,e.status)};role=Role.VETERINARIO;for(reason in listOf(" ","x".repeat(256)))try{repo.scheduleVisit(owner,animal,at,reason);fail()}catch(e:AuthFailure){assertEquals(400,e.status)};assertEquals(0,server.requestCount)}
 @Test fun unlinkedFarmerCannotDispatchPost()=runBlocking{reply("""{"activeLinksCount":0,"farmers":[]}""");try{repo.scheduleVisit(owner,animal,at,"Control");fail()}catch(e:AuthFailure){assertEquals(403,e.status)};assertEquals(1,server.requestCount)}
 @Test fun serverFailureIsNotRetried()=runBlocking{scope();server.enqueue(MockResponse().setResponseCode(503));try{repo.scheduleVisit(owner,animal,at,"Control");fail()}catch(e:AuthFailure){assertEquals(503,e.status)};assertEquals(3,server.requestCount)}
 @Test fun malformedSuccessDoesNotPretendPersistenceOrRetry()=runBlocking{scope();server.enqueue(MockResponse().setResponseCode(201).setHeader("Content-Type","application/json").setBody("""{"success":true,"data":${response("bad-id")}}"""));try{repo.scheduleVisit(owner,animal,at,"Control");fail()}catch(e:AuthFailure){assertEquals(AuthProblem.INVALID_RESPONSE,e.problem)};assertEquals(3,server.requestCount)}
}
