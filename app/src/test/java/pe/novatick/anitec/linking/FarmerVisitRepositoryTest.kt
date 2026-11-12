package pe.novatick.anitec.linking

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.core.network.AuthorizedRequests
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.infrastructure.Envelope
import pe.novatick.anitec.features.linking.infrastructure.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class FarmerVisitRepositoryTest {
 private val server=MockWebServer();private var role=Role.GANADERO;private var owner="10000000-0000-4000-8000-000000000001"
 private lateinit var repo:LinkingRepositoryImpl
 @Before fun start(){server.start();val retrofit=Retrofit.Builder().baseUrl(server.url("/api/v1/")).addConverterFactory(GsonConverterFactory.create()).build()
  val requests=object:AuthorizedRequests{override suspend fun <T> execute(request:suspend(String)->Response<Envelope<T>>):T{val r=request("Bearer synthetic-farmer");if(!r.isSuccessful)throw AuthFailure(AuthProblem.SERVER,r.code());return r.body()!!.data!!}}
  repo=LinkingRepositoryImpl(retrofit.create(LinkingApi::class.java),requests){Profile(owner,"qa@example.invalid","QA","Farmer",role)}
 }
 @After fun stop(){server.shutdown()}
 private fun row(n:Int=1,date:String="2035-06-01T01:00:00.123456Z",status:String="PROGRAMADA")="""{"id":"90000000-0000-4000-8000-${n.toString().padStart(12,'0')}","animalId":"60000000-0000-4000-8000-000000000001","animalName":"Nina","animalCode":"AN-001","farmerName":"Farmer","vetName":"Vet","type":"VISITA","scheduledAt":"$date","status":"$status","reason":null}"""
 private fun reply(rows:String){server.enqueue(MockResponse().setHeader("Content-Type","application/json").setBody("""{"success":true,"data":$rows}"""))}
 @Test fun usesOnlyOwnerEndpointJwtAndFullListWithStableUtcOrdering()=runBlocking{
  reply("["+(5 downTo 1).joinToString(","){row(it)}+"]");val items=repo.upcomingVisits()
  assertEquals(5,items.size);assertTrue(items.first().visit.id.endsWith("001"));assertEquals(123L,items.first().visit.time%1000)
  val r=server.takeRequest();assertEquals("GET",r.method);assertEquals("/api/v1/visits/upcoming",r.path);assertEquals("Bearer synthetic-farmer",r.getHeader("Authorization"));assertEquals(owner,items.first().visit.farmerId)
  assertEquals("2035-05-31 20:00",java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",java.util.Locale.US).apply{timeZone=java.util.TimeZone.getTimeZone("America/Lima")}.format(java.util.Date(items.first().visit.time)))
 }
 @Test fun otherRoleNeverDispatches()=runBlocking{role=Role.VETERINARIO;try{repo.upcomingVisits();fail()}catch(e:AuthFailure){assertEquals(403,e.status)};assertEquals(0,server.requestCount)}
 @Test fun utcParsingDoesNotRejectAnInstantAtDeviceDstGap()=runBlocking{val old=java.util.TimeZone.getDefault();try{java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/New_York"));reply("[${row(date="2026-03-08T02:30:00Z")}]");val item=repo.upcomingVisits().single();assertEquals("2026-03-08T02:30:00Z",java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",java.util.Locale.US).apply{timeZone=java.util.TimeZone.getTimeZone("UTC")}.format(java.util.Date(item.visit.time)))}finally{java.util.TimeZone.setDefault(old)}}
 @Test fun emptyIsDistinctFromHttpError()=runBlocking{reply("[]");assertTrue(repo.upcomingVisits().isEmpty());for(code in listOf(404,503)){server.enqueue(MockResponse().setResponseCode(code));try{repo.upcomingVisits();fail()}catch(e:AuthFailure){assertEquals(code,e.status)}};assertEquals(3,server.requestCount)}
 @Test fun malformedStateDateAndDuplicatesCannotPretendValidData()=runBlocking{
  for(rows in listOf("[${row(status="CANCELADA")}]","[${row(date="2035-02-30T01:00:00Z")}]","[${row()},${row()}]")){reply(rows);try{repo.upcomingVisits();fail()}catch(e:AuthFailure){assertEquals(AuthProblem.INVALID_RESPONSE,e.problem)}}
 }
}
