package pe.novatick.anitec.linking

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.R
import pe.novatick.anitec.debug.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.linking.presentation.*
import pe.novatick.anitec.features.livestock.presentation.*
import pe.novatick.anitec.ui.theme.AniTecTheme
import java.util.*

class FarmerVisitsUiTest {
 @get:Rule val compose=createComposeRule()
 private val previous=TimeZone.getDefault()
 @Before fun zone(){TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"))}
 @After fun restore(){TimeZone.setDefault(previous)}
 private class Fake:LinkingRepository by DebugLinkingRepository(){
  var calls=0;var gate:CompletableDeferred<List<FarmerVisit>>?=null
  var items=(5 downTo 1).map{n->FarmerVisit(HomeVisit(UUID(0,n.toLong()).toString(),"Nina$n","QA Farmer",2064272400000L+n*60000L,"PROGRAMADA","Reason$n",UUID(0,100).toString()),"AN-$n","QA Vet")}
  override suspend fun upcomingVisits():List<FarmerVisit>{calls++;return gate?.await() ?: items}
 }
 private lateinit var context:android.content.Context
 private fun open(language:String,content:@Composable ()->Unit){
  val base=InstrumentationRegistry.getInstrumentation().targetContext
  val config=Configuration(base.resources.configuration).apply{setLocale(Locale.forLanguageTag(language));fontScale=1f};context=base.createConfigurationContext(config)
  compose.setContent{val density=LocalDensity.current;CompositionLocalProvider(LocalContext provides context,LocalResources provides context.resources,LocalConfiguration provides config,LocalDensity provides Density(density.density,1f)){AniTecTheme{content()}}}
 }
 private fun rows(n:Int){for(i in 1..n)compose.onNodeWithTag("farmer_visit_"+UUID(0,i.toLong())).assertExists()}
 private fun capture(name:String){if(InstrumentationRegistry.getArguments().getString("captureFarmerVisits")=="true"){
  compose.waitForIdle()
  val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
  java.io.File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,"farmer-$name.png").outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
 }}
 private fun flows(language:String){val repo=Fake()
  open(language){val vm=remember{LivestockViewModel(DebugLivestockRepository())};DisposableEffect(vm){vm.start();onDispose{vm.close()}};LivestockShell(vm,Profile(UUID(0,1).toString(),"qa@example.invalid","QA","Farmer",Role.GANADERO),{},repo)}
  compose.waitUntil(8000){compose.onAllNodesWithTag("farmer_visit_"+UUID(0,1)).fetchSemanticsNodes().isNotEmpty()}
  rows(3);compose.onNodeWithTag("farmer_visit_"+UUID(0,4)).assertDoesNotExist()
  capture("$language-home")
  compose.onNodeWithTag("farmer_visit_"+UUID(0,1)).performScrollTo().performClick()
  compose.waitUntil(8000){compose.onAllNodesWithText("Nina1 · AN-1").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithText(context.getString(R.string.lk_record_care)).assertDoesNotExist()
  compose.onNodeWithText("QA Vet").assertExists()
  capture("$language-detail")
  compose.onNodeWithContentDescription(context.getString(R.string.animal_back)).performClick()
  compose.onNodeWithText(context.getString(R.string.ls_view_visits)).performScrollTo().performClick()
  compose.waitUntil(8000){compose.onAllNodesWithTag("farmer_visit_"+UUID(0,5)).fetchSemanticsNodes().isNotEmpty()};rows(5)
  capture("$language-list")
  compose.onNodeWithTag("farmer_visit_"+UUID(0,5)).performScrollTo().performClick()
  compose.waitUntil(8000){compose.onAllNodesWithText("Nina5 · AN-5").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithText(context.getString(R.string.lk_record_care)).assertDoesNotExist()
  compose.runOnIdle{assertTrue(repo.calls>=4)}
 }
 @Test fun spanishHomeThreeAllFiveAndBothDetailRoutesReadOnly()=flows("es")
 @Test fun englishHomeThreeAllFiveAndBothDetailRoutesReadOnly()=flows("en")
 @Test fun loadingFailureRetryAndGenuineEmptyAreDistinct(){val repo=Fake();val pending=CompletableDeferred<List<FarmerVisit>>();repo.gate=pending;lateinit var vm:FarmerVisitsViewModel
  open("es"){vm=remember{FarmerVisitsViewModel(repo,Role.GANADERO)};DisposableEffect(vm){onDispose{vm.close()}};Column{FarmerUpcomingHome(vm,{},{})}}
  compose.onNodeWithText(context.getString(R.string.lk_loading)).assertExists()
  compose.onNodeWithText(context.getString(R.string.fv_empty)).assertDoesNotExist()
  compose.runOnIdle{pending.completeExceptionally(AuthFailure(AuthProblem.NETWORK))}
  compose.waitUntil(8000){compose.onAllNodesWithText(context.getString(R.string.vis_read_error)).fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithText(context.getString(R.string.fv_empty)).assertDoesNotExist()
  compose.runOnIdle{repo.gate=null;repo.items=emptyList()};compose.onNodeWithText(context.getString(R.string.lk_reload)).performClick()
  compose.waitUntil(8000){compose.onAllNodesWithText(context.getString(R.string.fv_empty)).fetchSemanticsNodes().isNotEmpty()}
 }
}
