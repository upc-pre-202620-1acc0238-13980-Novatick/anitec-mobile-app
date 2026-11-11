package pe.novatick.anitec.livestock

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.json.JSONObject
import pe.novatick.anitec.BuildConfig
import pe.novatick.anitec.core.session.KeystoreSessionStore
import pe.novatick.anitec.features.auth.infrastructure.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.infrastructure.*
import pe.novatick.anitec.features.livestock.domain.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

/** Explicit opt-in: real writes ONLY to the isolated emulator laboratory, never to Render. */
class RealLabAlphaTest {
    @Test fun realRepositoriesAndPersistedSession()=runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("realLab")=="true")
        check(BuildConfig.DEBUG && BuildConfig.API_BASE_URL=="http://10.0.2.2:18080/api/v1/")
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val credentialsFile=File(context.filesDir,"alpha-test-credentials.json")
        val credentials=JSONObject(credentialsFile.readText())
        credentialsFile.delete()
        val client=OkHttpClient.Builder().retryOnConnectionFailure(false).followRedirects(false)
            .callTimeout(15,TimeUnit.SECONDS).build()
        val retrofit=Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val api=retrofit.create(AuthApi::class.java)
        val wire=retrofit.create(LivestockApi::class.java)
        assertEquals(401,api.me("Bearer invalid-alpha-test").code())
        val store=KeystoreSessionStore(context)
        val auth=AuthRepositoryImpl(api,store)
        auth.login(credentials.getString("email"),credentials.getString("password"),true)
        assertEquals(Role.GANADERO,auth.profile.value!!.role)
        val owner=auth.profile.value!!.id
        val restored=AuthRepositoryImpl(api,store)
        restored.restore();assertEquals(owner,restored.profile.value!!.id)
        restored.refresh()
        val repo=LivestockRepositoryImpl(wire,restored) {restored.profile.value?.id}
        val species=repo.species();assertTrue(species.isNotEmpty())
        repo.farms();repo.inventory(InventoryFilter())
        val capacity=repo.capacity();check(capacity.remaining>0) {"No free laboratory capacity; existing data will not be changed"}
        val marker="TB2-ALPHA-"+UUID.randomUUID().toString().take(8)
        val farm=repo.createFarm(marker,"Controlled local Alpha E2E")
        val created=repo.createAnimal(NewAnimal(farm.id,species.first().id,AnimalSex.HEMBRA,name=marker,birthDate="2024-01-01"))
        assertTrue(created.code.isNotBlank())
        val detail=repo.animal(created.id);assertEquals(farm.id,detail.farmId)
        assertTrue(repo.inventory(InventoryFilter(search=marker,farmId=farm.id)).content.any {it.id==created.id})
        assertEquals(capacity.activeAnimals+1,repo.capacity().activeAnimals)
        val input=AnimalEdit.from(detail).copy(name=marker+"-edited")
        repo.updateAnimal(created.id,input)
        assertEquals(marker+"-edited",repo.animal(created.id).name)
        val conflict=runCatching {repo.updateAnimal(created.id,input)}.exceptionOrNull() as? AuthFailure
        assertEquals(409,conflict?.status)
        assertTrue(repo.observations(created.id).isEmpty())
        val note=repo.addObservation(created.id,"Controlled Alpha E2E observation")
        assertEquals(note.id,repo.observations(created.id).single().id)
        repo.deactivateAnimal(created.id)
        assertEquals(AnimalStatus.INACTIVO,repo.animal(created.id).status)
        assertEquals(capacity.activeAnimals,repo.capacity().activeAnimals)
        val session=store.read()!!
        val logout=restored.logoutLocally().invoke()
        assertTrue(logout.remoteConfirmed)
        assertNull(store.read())
        assertEquals(401,api.refresh(RefreshRequest(session.refresh,UUID.randomUUID().toString())).code())
        val result=JSONObject().put("marker",marker).put("farmId",farm.id).put("animalId",created.id)
            .put("observationId",note.id).put("result","PASS").put("animalStatus","INACTIVO")
        File(context.filesDir,"alpha-e2e-result.json").writeText(result.toString())
    }
}
