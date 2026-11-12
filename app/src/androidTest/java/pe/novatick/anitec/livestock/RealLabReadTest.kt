package pe.novatick.anitec.livestock
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.Assume.assumeTrue
import org.junit.Assert.*
import org.json.JSONObject
import java.io.File
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import pe.novatick.anitec.core.session.KeystoreSessionStore
import pe.novatick.anitec.features.auth.infrastructure.*
import pe.novatick.anitec.features.livestock.infrastructure.*
import pe.novatick.anitec.features.livestock.domain.*
class RealLabReadTest {
 @Test fun allStatusSearchReadsPersistedMarker()=runBlocking {
  assumeTrue(InstrumentationRegistry.getArguments().getString("realLabRead")=="true")
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val file=File(context.filesDir,"alpha-test-credentials.json");val data=JSONObject(file.readText());file.delete()
  val retrofit=Retrofit.Builder().baseUrl("http://10.0.2.2:18080/api/v1/").client(OkHttpClient.Builder().retryOnConnectionFailure(false).build()).addConverterFactory(GsonConverterFactory.create()).build()
  val auth=AuthRepositoryImpl(retrofit.create(AuthApi::class.java),KeystoreSessionStore(context))
  auth.login(data.getString("email"),data.getString("password"),false)
  val repo=LivestockRepositoryImpl(retrofit.create(LivestockApi::class.java),auth) {auth.profile.value?.id}
  try {assertTrue(repo.inventory(InventoryFilter(search=data.getString("marker"),status=null)).content.isNotEmpty())}
  finally {auth.logoutLocally().invoke()}
 }
}
