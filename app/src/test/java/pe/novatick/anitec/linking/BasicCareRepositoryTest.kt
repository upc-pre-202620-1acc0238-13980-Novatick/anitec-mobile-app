package pe.novatick.anitec.linking
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.core.network.AuthorizedRequests
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.infrastructure.Envelope
import pe.novatick.anitec.features.linking.infrastructure.*
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.features.livestock.infrastructure.LivestockApi
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class BasicCareRepositoryTest {
 private val server=MockWebServer();private var role=Role.VETERINARIO
 private val owner="10000000-0000-4000-8000-000000000001";private val vet="20000000-0000-4000-8000-000000000001";private val animal="60000000-0000-4000-8000-000000000001";private val farm="50000000-0000-4000-8000-000000000001";private val care="90000000-0000-4000-8000-000000000001"
 private lateinit var repo:LinkingRepositoryImpl
 private val input get()=NewBasicCare(animal," Control ","Description","2020-02-29")
 @Before fun setup(){server.start();val retrofit=Retrofit.Builder().baseUrl(server.url("/api/v1/")).buildConverter().build();val auth=object:AuthorizedRequests{override suspend fun <T> execute(request:suspend(String)->Response<Envelope<T>>):T{val r=request("Bearer synthetic");if(!r.isSuccessful)throw AuthFailure(AuthProblem.SERVER,r.code());return r.body()!!.data!!}};repo=LinkingRepositoryImpl(retrofit.create(LinkingApi::class.java),auth,retrofit.create(LivestockApi::class.java)){Profile(if(role==Role.GANADERO)owner else vet,"qa@example.invalid","QA","User",role)}}
 private fun Retrofit.Builder.buildConverter()=addConverterFactory(GsonConverterFactory.create())
 @After fun stop(){server.shutdown()}
 private fun reply(data:String,code:Int=200){server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type","application/json").setBody("""{"success":true,"data":$data}"""))}
 private fun detail(id:String=care)="""{"id":"$id","animalId":"$animal","title":"Control","description":"Description","attentionDate":"2020-02-29","veterinarianId":"$vet","vetName":"QA Vet"}"""
 private fun scope(){reply("""{"activeLinksCount":1,"farmers":[{"farmerId":"$owner","farmerName":"Farmer","animalsCount":1,"farms":[{"farmId":"$farm","farmName":"Farm","animalsCount":1}]}]}""");reply("""{"id":"$animal","code":"AN-001","species":"Bovino","speciesId":"70000000-0000-4000-8000-000000000001","sex":"HEMBRA","status":"ACTIVO","farmId":"$farm","farmName":"Farm","version":0,"recentAttentions":[]}""")}
 @Test fun exactFourFieldPost201AndServerTitleTrimming()=runBlocking{scope();reply(detail(),201);assertEquals(care,repo.createCare(owner,input).id);server.takeRequest();server.takeRequest();val request=server.takeRequest();assertEquals("/api/v1/attentions",request.path);assertEquals("POST",request.method);val json=com.google.gson.JsonParser.parseString(request.body.readUtf8()).asJsonObject;assertEquals(setOf("animalId","title","description","attentionDate"),json.keySet());assertEquals(input.title,json["title"].asString);assertEquals(input.date,json["attentionDate"].asString)}
 @Test fun roleAndValidationNeverWrite()=runBlocking{role=Role.GANADERO;try{repo.createCare(owner,input);fail()}catch(e:AuthFailure){assertEquals(403,e.status)};role=Role.VETERINARIO;try{repo.createCare(owner,input.copy(title="x".repeat(151)));fail()}catch(e:AuthFailure){assertEquals(400,e.status)};assertEquals(0,server.requestCount)}
 @Test fun ownerCanReadButWrongAnimalAndHttpErrorsNeverBecomeData()=runBlocking{role=Role.GANADERO;reply(detail());assertEquals(care,repo.care(care,animal).id);assertEquals("/api/v1/attentions/$care",server.takeRequest().path);reply(detail());try{repo.care(care,farm);fail()}catch(e:AuthFailure){assertEquals(AuthProblem.INVALID_RESPONSE,e.problem)};server.enqueue(MockResponse().setResponseCode(403));try{repo.care(care,animal);fail()}catch(e:AuthFailure){assertEquals(403,e.status)}}
 @Test fun failedPostDoesNotRetryOrInventSuccess()=runBlocking{scope();server.enqueue(MockResponse().setResponseCode(503));try{repo.createCare(owner,input);fail()}catch(e:AuthFailure){assertEquals(503,e.status)};assertEquals(3,server.requestCount)}
 @Test fun latestMetadataKeepsUnknownDistinctFromEmptyAndSortsCivilDates(){
  assertNull(pe.novatick.anitec.features.livestock.infrastructure.recentCare(null));assertTrue(pe.novatick.anitec.features.livestock.infrastructure.recentCare(emptyList())!!.isEmpty())
  val rows=listOf(pe.novatick.anitec.features.livestock.infrastructure.CareSummaryDto(care,"Old","Vet","2020-02-29","Old"),pe.novatick.anitec.features.livestock.infrastructure.CareSummaryDto(animal,"New","Vet","2021-01-01","New"))
  assertEquals("New",pe.novatick.anitec.features.livestock.infrastructure.recentCare(rows)!!.first().title)
 }
}
