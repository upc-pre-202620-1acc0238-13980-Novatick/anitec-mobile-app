package pe.novatick.anitec.linking
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.BuildConfig
import pe.novatick.anitec.core.session.*
import pe.novatick.anitec.features.auth.infrastructure.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.infrastructure.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
/** Explicit opt-in; existing laboratory sessions only. No Render or business writes. */
class RealLabLinkingReadTest {
 @Test fun bothRolesUseActualRepositoryAndAuthorizedSession()=runBlocking {
  Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("realLinkingLab")=="true")
  check(BuildConfig.DEBUG && BuildConfig.API_BASE_URL=="http://10.0.2.2:18080/api/v1/")
  val retrofit=Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL).client(OkHttpClient.Builder().retryOnConnectionFailure(false).followRedirects(false).build()).addConverterFactory(GsonConverterFactory.create()).build()
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  // Private input supplied only for this run; never part of the APK or evidence.
  val file=java.io.File(context.filesDir,"linking-lab-credentials.json")
  val inputs=org.json.JSONObject(file.readText());file.delete()
  for(role in listOf(Role.GANADERO,Role.VETERINARIO)){
   val auth=AuthRepositoryImpl(retrofit.create(AuthApi::class.java),KeystoreSessionStore(context))
   val credential=inputs.getJSONObject(role.name)
   auth.login(credential.getString("email"),credential.getString("password"),false)
   try {
    assertEquals(role,auth.profile.value!!.role)
    val repo=LinkingRepositoryImpl(retrofit.create(LinkingApi::class.java),auth){auth.profile.value}
    val links=repo.links();assertTrue(links.isNotEmpty())
    if(role==Role.VETERINARIO){assertNotNull(repo.pending());assertTrue(repo.farmers().isNotEmpty());assertEquals(auth.profile.value!!.id,repo.capacity().vetId)}
    else {try{repo.capacity();fail("Wrong role accepted")}catch(e:AuthFailure){assertEquals(403,e.status)}}
   }finally{assertTrue(auth.logoutLocally()().remoteConfirmed)}
  }
 }
}
