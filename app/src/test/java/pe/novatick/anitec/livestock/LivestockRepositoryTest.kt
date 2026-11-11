package pe.novatick.anitec.livestock
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.infrastructure.Envelope
import pe.novatick.anitec.core.network.AuthorizedRequests
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.features.livestock.infrastructure.*
class LivestockRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: LivestockRepository
    private val owner="11111111-1111-4111-8111-111111111111"
    private val farm="22222222-2222-4222-8222-222222222222"
    private val species="33333333-3333-4333-8333-333333333333"
    private fun response(data: String,status: Int=200)=MockResponse().setResponseCode(status).setBody("""{"success":true,"message":"ok","timestamp":"2035-01-01T00:00:00Z","data":$data}""")
    private fun farmJson()="""{"id":"$farm","name":"Farm","ownerId":"$owner","createdAt":"2035-01-01T00:00:00.123456Z","location":null}"""
    @Before fun setup() {
        server=MockWebServer();server.start()
        val api=Retrofit.Builder().baseUrl(server.url("api/v1/")).addConverterFactory(GsonConverterFactory.create()).build().create(LivestockApi::class.java)
        val access=object: AuthorizedRequests {
            override suspend fun <T> execute(request: suspend (String)->Response<Envelope<T>>): T {
                val r=request("Bearer synthetic");if(!r.isSuccessful) throw AuthFailure(AuthProblem.SERVER,r.code())
                return r.body()?.data ?: throw AuthFailure(AuthProblem.INVALID_RESPONSE)
            }
        }
        repository=LivestockRepositoryImpl(api,access) { owner }
    }
    @After fun cleanup() { server.shutdown() }
    @Test fun farmCreationUsesOnlyContractFieldsAndNoOwnerInput()=runBlocking {
        server.enqueue(response(farmJson(),201));val created=repository.createFarm(" Farm ","")
        assertEquals(farm,created.id);assertNull(created.location)
        val req=server.takeRequest();assertEquals("POST",req.method);assertEquals("Bearer synthetic",req.getHeader("Authorization"))
        val body=com.google.gson.JsonParser.parseString(req.body.readUtf8()).asJsonObject
        assertEquals(setOf("name"),body.keySet());assertEquals("Farm",body.get("name").asString)
    }
    @Test fun farmsValidateOwnerDateAndUuidAndAcceptOptionalLocation()=runBlocking {
        server.enqueue(response("[${farmJson()}]"));assertEquals(farm,repository.farms().single().id)
        for(data in listOf(farmJson().replace(owner,farm),farmJson().replace("2035-01-01T00:00:00.123456Z","invalid"),farmJson().replace(farm,"bad"),"null")) {
            server.enqueue(response("[$data]"));assertTrue(runCatching { repository.farms() }.exceptionOrNull() is AuthFailure)
        }
    }
    @Test fun filtersUseRealUuidAndFarmEndpointWithIndependentTotal()=runBlocking {
        server.enqueue(response("""{"totalActive":12,"content":[]}"""))
        val result=repository.inventory(InventoryFilter("A & B",species,AnimalStatus.INACTIVO,farm))
        assertEquals(12L,result.totalActive);assertEquals(0,result.content.size)
        val url=server.takeRequest().requestUrl!!;assertEquals("/api/v1/farms/$farm/animals",url.encodedPath)
        assertEquals(species,url.queryParameter("species"));assertEquals("A & B",url.queryParameter("search"));assertEquals("INACTIVO",url.queryParameter("status"))
        server.enqueue(response("""{"totalActive":14,"content":[]}"""));repository.inventory(InventoryFilter(status=null))
        val all=server.takeRequest().requestUrl!!;assertEquals("/api/v1/animals",all.encodedPath);assertNull(all.queryParameter("status"));assertNull(all.queryParameter("farmId"))
    }
    @Test fun malformedInventoryNeverDefaultsUnknownStatusOrMissingNumbers()=runBlocking {
        for(data in listOf("{}","""{"totalActive":-1,"content":[]}""","""{"totalActive":1,"content":[null]}""",
            """{"totalActive":1,"content":[{"id":"$farm","code":"A","species":"Test","speciesId":"$species","status":"UNKNOWN","farmId":"$farm","ownerId":"$owner","version":0}]}""")) {
            server.enqueue(response(data));assertTrue(runCatching { repository.inventory(InventoryFilter()) }.exceptionOrNull() is AuthFailure)
        }
    }
    @Test fun capacityIsGlobalAndExcessNeverHidesAnimals()=runBlocking {
        server.enqueue(response("""{"ownerId":"$owner","allowedAnimals":15,"activeAnimals":16,"lastPlanRevision":9223372036854775806}"""))
        val v=repository.capacity();assertEquals(0L,v.remaining);assertEquals(16L,v.activeAnimals);assertEquals(9223372036854775806L,v.lastPlanRevision)
    }
    @Test fun speciesEmptyIsNotStaticFallbackAndFarmValidationDoesNotSend()=runBlocking {
        server.enqueue(response("[]"));assertTrue(repository.species().isEmpty());assertEquals(1,server.requestCount)
        for(name in listOf(" ","x".repeat(151))) assertTrue(runCatching {repository.createFarm(name,null)}.exceptionOrNull() is AuthFailure)
        assertTrue(runCatching {repository.createFarm("ok","x".repeat(256))}.exceptionOrNull() is AuthFailure);assertEquals(1,server.requestCount)
    }
    @Test fun populatedInventoryMapsEachStatusAndMissingOptionalNameWithoutFabrication()=runBlocking {
        for(status in AnimalStatus.entries) {
            server.enqueue(response("""{"totalActive":8,"content":[{"id":"$farm","code":"ANI-000001","species":"Actual species","speciesId":"$species","status":"${status.name}","farmId":"$farm","ownerId":"$owner","version":0}]}"""))
            val animal=repository.inventory(InventoryFilter()).content.single()
            assertEquals(status,animal.status);assertNull(animal.name);assertEquals(0L,animal.version);assertEquals("ANI-000001",animal.code)
        }
    }
    @Test fun farmCreationDoesNotAcceptWrongSuccessStatus()=runBlocking {
        server.enqueue(response(farmJson(),200))
        assertTrue(runCatching {repository.createFarm("Farm",null)}.exceptionOrNull() is AuthFailure)
        assertEquals(1,server.requestCount)
    }
    @Test fun animalPostOmitsGeneratedCodeAndChecksCreatedIdentity()=runBlocking {
        val data=com.google.gson.Gson().toJson(mapOf("id" to owner,"code" to "SERVER-1","species" to "Bovino","speciesId" to species,"status" to "ACTIVO","farmId" to farm,"ownerId" to owner,"version" to 0))
        server.enqueue(response(data,201))
        val created=repository.createAnimal(NewAnimal(farm,species,AnimalSex.MACHO))
        assertEquals("SERVER-1",created.code)
        val request=server.takeRequest()
        assertEquals("/api/v1/animals",request.path);assertEquals("POST",request.method)
        val body=com.google.gson.JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals(setOf("farmId","speciesId","sex"),body.keySet())
        server.enqueue(response(data,200))
        assertEquals(AuthProblem.INVALID_RESPONSE,(runCatching {repository.createAnimal(NewAnimal(farm,species,AnimalSex.HEMBRA))}.exceptionOrNull() as AuthFailure).problem)
    }
    @Test fun detailChecksUuidSexVersionAndOwnedFarm()=runBlocking {
        val data=com.google.gson.Gson().toJson(mapOf("id" to owner,"code" to "SERVER-1","species" to "Bovino","speciesId" to species,
            "sex" to "HEMBRA","status" to "ACTIVO","farmId" to farm,"farmName" to "Farm","version" to 0))
        server.enqueue(response(data));server.enqueue(response(farmJson()))
        assertEquals(AnimalSex.HEMBRA,repository.animal(owner).sex)
        assertEquals("/api/v1/animals/"+owner,server.takeRequest().path);assertEquals("/api/v1/farms/"+farm,server.takeRequest().path)
        server.enqueue(response(data));server.enqueue(response(farmJson().replace(owner,species)))
        assertTrue(runCatching {repository.animal(owner)}.exceptionOrNull() is AuthFailure)
    }

    @Test fun updateUsesPutSnapshotVersionAndPreservesOptionalPhotoWithoutFarmMutation()=runBlocking {
        val id="44444444-4444-4444-8444-444444444444"
        val input=AnimalEdit(" BOV-024 ",species,AnimalSex.HEMBRA,"Luna","Holstein","2021-03-12","https://example.invalid/photo.png",7)
        val json="""{"id":"$id","code":"BOV-024","name":"Luna","species":"Bovino","speciesId":"$species","status":"ACTIVO","farmId":"$farm","ownerId":"$owner","version":8}"""
        server.enqueue(response(json));val result=repository.updateAnimal(id,input)
        assertEquals(8L,result.version);val req=server.takeRequest();assertEquals("PUT",req.method);assertEquals("/api/v1/animals/$id",req.path)
        val body=com.google.gson.JsonParser.parseString(req.body.readUtf8()).asJsonObject
        assertEquals(setOf("code","speciesId","sex","name","breed","birthDate","photoUrl","version"),body.keySet())
        assertEquals(7L,body.get("version").asLong);assertEquals(input.photoUrl,body.get("photoUrl").asString);assertEquals("BOV-024",body.get("code").asString)
        assertEquals("Holstein",body.get("breed").asString);assertFalse(body.has("farmId"));assertFalse(body.has("ownerId"))
    }
    @Test fun softDeactivationUsesExistingDeleteWithoutInventedVersionQueryOrBody()=runBlocking {
        val id="44444444-4444-4444-8444-444444444444"
        server.enqueue(response("""{"id":"$id","status":"INACTIVO"}"""))
        assertEquals(AnimalStatus.INACTIVO,repository.deactivateAnimal(id).status)
        val req=server.takeRequest();assertEquals("DELETE",req.method);assertEquals("/api/v1/animals/$id",req.path);assertEquals(0L,req.bodySize)
    }
    @Test fun deactivationRejectsWrongIdentityOrNonInactiveSuccess()=runBlocking {
        val id="44444444-4444-4444-8444-444444444444"
        for(json in listOf("""{"id":"$farm","status":"INACTIVO"}""","""{"id":"$id","status":"ACTIVO"}""")) {
            server.enqueue(response(json));val e=runCatching {repository.deactivateAnimal(id)}.exceptionOrNull();assertTrue(e is AuthFailure)
        }
    }
}
