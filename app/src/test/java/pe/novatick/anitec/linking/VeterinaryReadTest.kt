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
class VeterinaryReadTest {
 private val server=MockWebServer();private var role=Role.VETERINARIO
 private val owner="10000000-0000-4000-8000-000000000001";private val vet="20000000-0000-4000-8000-000000000001"
 private val farm="50000000-0000-4000-8000-000000000001";private val otherFarm="50000000-0000-4000-8000-000000000002";private val animal="60000000-0000-4000-8000-000000000001";private val species="70000000-0000-4000-8000-000000000001"
 private lateinit var repo:LinkingRepositoryImpl
 @Before fun setup(){server.start();val retrofit=Retrofit.Builder().baseUrl(server.url("/api/v1/")).client(OkHttpClient.Builder().retryOnConnectionFailure(false).build()).addConverterFactory(GsonConverterFactory.create()).build()
  val requests=object:AuthorizedRequests{override suspend fun <T> execute(request:suspend(String)->Response<Envelope<T>>):T{val r=request("Bearer synthetic");if(!r.isSuccessful)throw AuthFailure(AuthProblem.FORBIDDEN,r.code());return r.body()!!.data!!}}
  repo=LinkingRepositoryImpl(retrofit.create(LinkingApi::class.java),requests,retrofit.create(LivestockApi::class.java)){Profile(vet,"qa@example.invalid","QA","Test",role)}
 }
 @After fun close(){server.shutdown()}
 private fun reply(data:String){server.enqueue(MockResponse().setHeader("Content-Type","application/json").setBody("""{"success":true,"message":"ok","timestamp":"2026-11-11T12:00:00Z","data":$data}"""))}
 private fun allowed(){reply("""{"activeLinksCount":1,"farmers":[{"farmerId":"$owner","farmerName":"Owner","animalsCount":1,"farms":[{"farmId":"$farm","farmName":"Farm","animalsCount":1}]}]}""")}
 private fun detail(farmId:String=farm)="""{"id":"$animal","code":"QA-001","name":"Luna","species":"Bovino","speciesId":"$species","sex":"HEMBRA","status":"ACTIVO","farmId":"$farmId","farmName":"Farm","version":2,"birthDate":"2021-01-01"}"""
 @Test fun todayVisitReadUsesUtcBoundsAndExcludesNextDay()=runBlocking {
  val day=pe.novatick.anitec.features.linking.domain.VisitDay.current(0,java.util.TimeZone.getTimeZone("UTC"))
  fun visit(id:String,at:String)="""{"id":"$id","animalId":"$animal","animalName":"Luna","farmerId":"$owner","farmerName":"Owner","type":"VISITA","scheduledAt":"$at","status":"PROGRAMADA"}"""
  reply("["+visit(animal,"1970-01-01T00:00:00Z")+","+visit(farm,"1970-01-02T00:00:00Z")+"]")
  assertEquals(1,repo.todayVisits(day).size);val request=server.takeRequest();assertEquals("GET",request.method);assertEquals("1970-01-01T00:00:00Z",request.requestUrl!!.queryParameter("from"));assertEquals("1970-01-02T00:00:00Z",request.requestUrl!!.queryParameter("to"))
 }
 @Test fun dayWindowUsesCivilTimezoneAndHandlesDaylightSaving(){
  val parser=java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",java.util.Locale.US).apply{timeZone=java.util.TimeZone.getTimeZone("UTC")}
  val day=pe.novatick.anitec.features.linking.domain.VisitDay.current(parser.parse("2024-11-03T12:00:00Z")!!.time,java.util.TimeZone.getTimeZone("America/New_York"))
  assertEquals(25*60*60*1000L,day.end-day.start);assertFalse(day.contains(day.end));assertTrue(day.contains(day.start))
 }
 @Test fun visitFailureIsNotAnEmptyDayAndOwnerCannotQuery()=runBlocking {
  val day=pe.novatick.anitec.features.linking.domain.VisitDay.current();role=Role.GANADERO
  try{repo.todayVisits(day);fail()}catch(e:AuthFailure){assertEquals(403,e.status)};assertEquals(0,server.requestCount)
  role=Role.VETERINARIO;server.enqueue(MockResponse().setResponseCode(403));try{repo.todayVisits(day);fail()}catch(e:AuthFailure){assertEquals(403,e.status)};assertEquals(1,server.requestCount)
 }
 @Test fun farmLocationIsFreeTextAndOnlySelectedOwnersFarmsAreExposed()=runBlocking {
  allowed();reply("""[{"id":"$farm","name":"Farm","location":"Camino norte","ownerId":"$owner","createdAt":"2026-11-11T12:00:00Z"},{"id":"$otherFarm","name":"Other","location":"Private","ownerId":"$vet","createdAt":"2026-11-11T12:00:00Z"}]""")
  val farms=repo.farms(owner);assertEquals(1,farms.size);assertEquals("Camino norte",farms.single().location);assertEquals("/api/v1/vet-links/my-farmers",server.takeRequest().path);assertEquals("/api/v1/farms",server.takeRequest().path)
 }
 @Test fun inventoryUsesScopedFarmTotalsAndKeepsUuidOwnerAndStates()=runBlocking {
  allowed();reply("""{"totalActive":1,"content":[{"id":"$animal","code":"QA-001","name":"Luna","species":"Bovino","speciesId":"$species","status":"ACTIVO","farmId":"$farm","ownerId":"$owner","version":2}]}""")
  val found=repo.animals(owner);assertEquals(1L,found.totalActive);assertEquals(animal,found.content.single().id);assertEquals(owner,found.content.single().ownerId)
  server.takeRequest();assertEquals("/api/v1/farms/$farm/animals",server.takeRequest().path)
 }
 @Test fun animalDetailIsReadOnlyAndBoundToTheSelectedFarmer()=runBlocking {allowed();reply(detail());val d=repo.animal(owner,animal);assertEquals(animal,d.id);assertEquals(farm,d.farmId);server.takeRequest();val r=server.takeRequest();assertEquals("GET",r.method);assertEquals("/api/v1/animals/$animal",r.path)}
 @Test fun detailOfAnotherLinkedFarmIsNotExposedForThisFarmer()=runBlocking {allowed();reply(detail(otherFarm));try{repo.animal(owner,animal);fail()}catch(e:AuthFailure){assertEquals(403,e.status)}}
 @Test fun unlinkedOwnerFailsBeforeAnimalDispatch()=runBlocking {reply("""{"activeLinksCount":0,"farmers":[]}""");try{repo.animal(owner,animal);fail()}catch(e:AuthFailure){assertEquals(403,e.status)};assertEquals(1,server.requestCount)}
 @Test fun farmerCannotEnterVeterinarianReadRoutes()=runBlocking {role=Role.GANADERO;try{repo.animals(owner);fail()}catch(e:AuthFailure){assertEquals(403,e.status)};assertEquals(0,server.requestCount)}
 @Test fun revokedAccessHttp403IsPreservedAndNotRetried()=runBlocking {allowed();server.enqueue(MockResponse().setResponseCode(403));try{repo.animals(owner);fail()}catch(e:AuthFailure){assertEquals(403,e.status)};assertEquals(2,server.requestCount)}
 @Test fun mismatchedInventoryOwnerFailsClosed()=runBlocking {allowed();reply("""{"totalActive":1,"content":[{"id":"$animal","code":"QA-001","species":"Bovino","speciesId":"$species","status":"ACTIVO","farmId":"$farm","ownerId":"$vet","version":2}]}""");try{repo.animals(owner);fail()}catch(e:AuthFailure){assertEquals(AuthProblem.INVALID_RESPONSE,e.problem)}}
 @Test fun observationsRequireFreshOwnerScopeAndOnlyDispatchGet()=runBlocking {
  allowed();reply(detail());reply("""[{"id":"80000000-0000-4000-8000-000000000001","text":"Actual observation","authorName":"Owner","date":"2026-11-10"}]""")
  val result=repo.observations(owner,animal);assertEquals("2026-11-10",result.single().date);assertEquals("Owner",result.single().authorName)
  server.takeRequest();server.takeRequest();val request=server.takeRequest();assertEquals("GET",request.method);assertEquals("/api/v1/animals/$animal/observations",request.path)
 }
 @Test fun foreignAnimalCannotDispatchObservationRead()=runBlocking {
  allowed();reply(detail(otherFarm));try{repo.observations(owner,animal);fail()}catch(e:AuthFailure){assertEquals(403,e.status)};assertEquals(2,server.requestCount)
 }
 @Test fun observation403IsPreservedWithoutRetry()=runBlocking {
  allowed();reply(detail());server.enqueue(MockResponse().setResponseCode(403));try{repo.observations(owner,animal);fail()}catch(e:AuthFailure){assertEquals(403,e.status)};assertEquals(3,server.requestCount)
 }
 @Test fun invalidObservationDateAndDuplicateIdsFailClosed()=runBlocking {
  for(date in listOf("2026-02-30","2026-11-10")){
   allowed();reply(detail());val item="""{"id":"80000000-0000-4000-8000-000000000001","text":"Text","date":"$date"}""";reply("[$item,$item]")
   try{repo.observations(owner,animal);fail()}catch(e:AuthFailure){assertEquals(AuthProblem.INVALID_RESPONSE,e.problem)}
  }
 }
}
