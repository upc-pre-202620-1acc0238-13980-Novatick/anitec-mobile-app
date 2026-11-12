package pe.novatick.anitec.linking
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.BuildConfig
import pe.novatick.anitec.core.session.KeystoreSessionStore
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.infrastructure.*
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.linking.infrastructure.*
import pe.novatick.anitec.features.linking.presentation.*
import pe.novatick.anitec.features.livestock.infrastructure.LivestockApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
/** Explicit opt-in: exactly two new marked appointments in the existing isolated lab; never Render. */
class RealLabVisitsTest {
 @Test fun twoEntryContextsPersistThroughRealAndroidSessionAndRefreshAgenda()=runBlocking {
  Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("realVisitsLab")=="true")
  check(BuildConfig.DEBUG && BuildConfig.API_BASE_URL=="http://10.0.2.2:18080/api/v1/")
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val input=java.io.File(context.filesDir,"visits-lab-credentials.json")
  val credentials=try{org.json.JSONObject(input.readText())}finally{input.delete()}
  val retrofit=Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL).client(OkHttpClient.Builder().retryOnConnectionFailure(false).followRedirects(false).readTimeout(15,java.util.concurrent.TimeUnit.SECONDS).build()).addConverterFactory(GsonConverterFactory.create()).build()
  val auth=AuthRepositoryImpl(retrofit.create(AuthApi::class.java),KeystoreSessionStore(context))
  auth.login(credentials.getString("email"),credentials.getString("password"),false)
  assertEquals(Role.VETERINARIO,auth.profile.value!!.role)
  val repo=LinkingRepositoryImpl(retrofit.create(LinkingApi::class.java),auth,retrofit.create(LivestockApi::class.java)){auth.profile.value}
  val vm=VisitsViewModel(repo,Role.VETERINARIO)
  try{
   val owner=repo.farmers().firstOrNull{it.animals>0} ?: error("No existing linked QA farmer with animals")
   val animal=repo.animals(owner.id).content.first()
   val today=VisitDay.current();val before=repo.todayVisits(today).size
   val marker="QA-F5-"+System.currentTimeMillis();val created=mutableListOf<HomeVisit>()
   for(index in 0..1){
    if(index==0){vm.begin();withTimeout(20000){vm.state.first{it.form.farmers.value!=null || it.form.farmers.error!=null}};vm.chooseOwner(owner.id)}else vm.begin(owner.id,animal.id)
    withTimeout(20000){vm.state.first{it.form.animals.value!=null || it.form.animals.error!=null}}
    if(index==0)vm.chooseAnimal(animal.id) else assertTrue(vm.state.value.form.fixed)
    val at=Calendar.getInstance().apply{add(Calendar.MINUTE,15+index*10);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}.time
    vm.date(SimpleDateFormat("yyyy-MM-dd",Locale.US).format(at));vm.time(SimpleDateFormat("HH:mm",Locale.US).format(at));vm.reason("$marker-${if(index==0)"HOME" else "ANIMAL"}")
    assertTrue(vm.canSave());vm.save();vm.save()
    val saved=withTimeout(25000){vm.state.first{it.form.confirmed!=null || it.form.error!=null}}
    assertNull("Real lab schedule must confirm once",saved.form.error);val id=saved.form.confirmed!!
    val day=VisitDay.current(at.time);val list=repo.todayVisits(day);val visit=list.single{it.id==id};assertEquals(animal.id,visit.animalId);assertEquals(owner.id,visit.farmerId);assertEquals("PROGRAMADA",visit.status);assertEquals(at.time,visit.time)
    assertEquals(1,list.count{it.reason=="$marker-${if(index==0)"HOME" else "ANIMAL"}"})
    created+=visit;vm.loadDetail(id,day);withTimeout(15000){vm.state.first{!it.detailLoading}};assertEquals(id,vm.state.value.detail!!.id)
    assertTrue(vm.dismiss())
   }
   val after=repo.todayVisits(today);assertEquals(before+created.count{today.contains(it.time)},after.size)
   vm.load(VisitDay.current(created.first().time));withTimeout(15000){vm.state.first{!it.agenda.loading}};assertTrue(vm.state.value.agenda.value!!.any{it.id==created.first().id})
   assertEquals(Role.VETERINARIO,auth.profile.value!!.role)
   InstrumentationRegistry.getInstrumentation().sendStatus(0,android.os.Bundle().apply{putString("qa_result","2 real schedules, UUID/UTC/detail/day filtering and session verified; existing records unchanged")})
  }finally{vm.close();auth.logoutLocally()()}
 }
}
