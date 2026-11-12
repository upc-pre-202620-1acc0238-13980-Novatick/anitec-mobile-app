package pe.novatick.anitec.linking
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.R
import pe.novatick.anitec.debug.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.linking.presentation.*
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.features.livestock.presentation.*
import pe.novatick.anitec.ui.theme.AniTecTheme
import java.util.Locale

class BasicCareUiTest {
 @get:Rule val compose=createComposeRule()
 private class Fake:LinkingRepository by DebugLinkingRepository(){
  private val base=DebugLinkingRepository();var posts=0;var record:BasicCare?=null
  override suspend fun animal(farmerId:String,animalId:String)=base.animal(farmerId,animalId).copy(recentCare=record?.let{listOf(CareSummary(it.id,it.title,it.vetName,it.date,it.description))} ?: emptyList())
  override suspend fun createCare(farmerId:String,input:NewBasicCare):BasicCare{posts++;return BasicCare("90000000-0000-4000-8000-000000000001",input.animalId,input.title,input.description,input.date,DebugLinkingRepository.VET,"QA Vet").also{record=it}}
  override suspend fun care(id:String,animalId:String)=record!!.also{assertEquals(id,it.id);assertEquals(animalId,it.animalId)}
 }
 private lateinit var context:android.content.Context
 private fun open(language:String,content:@Composable ()->Unit){val base=InstrumentationRegistry.getInstrumentation().targetContext;val config=Configuration(base.resources.configuration).apply{setLocale(Locale.forLanguageTag(language));fontScale=1f};context=base.createConfigurationContext(config)
  compose.setContent{val density=LocalDensity.current;CompositionLocalProvider(LocalContext provides context,LocalResources provides context.resources,LocalConfiguration provides config,LocalDensity provides Density(density.density,1f)){AniTecTheme{content()}}}
 }
 private fun text(id:Int)=context.getString(id)
 private fun capture(name:String){if(InstrumentationRegistry.getArguments().getString("captureBasicCare")=="true"){compose.waitForIdle();val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot();java.io.File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,"care-$name.png").outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}}}
 private fun vet(language:String){val repo=Fake()
  open(language){VeterinaryLinkingShell(repo,Profile(DebugLinkingRepository.VET,"qa@example.invalid","QA","Vet",Role.VETERINARIO),{})}
  compose.onNodeWithText(text(R.string.lk_farmers)).performClick();compose.onNodeWithText("Luis Martínez").performScrollTo().performClick()
  compose.onNodeWithTag("linked_view_animals").performScrollTo().performClick();compose.onNodeWithText("Luna").performScrollTo().performClick()
  compose.onNodeWithTag("vet_care_unavailable").performScrollTo().performClick()
  compose.onNodeWithTag("care_save").performScrollTo().performClick();compose.runOnIdle{assertEquals(0,repo.posts)}
  compose.onNodeWithTag("care_title").performScrollTo().performTextInput("Consulta básica")
  compose.onNodeWithTag("care_description").performScrollTo().performTextInput("Descripción clínica controlada.")
  androidx.test.espresso.Espresso.closeSoftKeyboard();capture("$language-form")
  compose.onNodeWithTag("care_save").performScrollTo().performClick()
  compose.waitUntil(8000){compose.onAllNodesWithTag("care_latest").fetchSemanticsNodes().isNotEmpty()}
  compose.runOnIdle{assertEquals(1,repo.posts)}
  compose.onNodeWithTag("care_latest").performScrollTo().performClick()
  compose.waitUntil(8000){compose.onAllNodesWithText("Descripción clínica controlada.").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithTag("care_save").assertDoesNotExist();compose.onNodeWithText(text(R.string.care_detail)).assertExists();capture("$language-detail")
 }
 private fun owner(language:String){val repo=Fake();val base=DebugLivestockRepository();val animal=base.animalsForTest()
  repo.record=BasicCare("90000000-0000-4000-8000-000000000001",animal,"Consulta QA","Descripción visible al propietario.","2020-02-29",DebugLinkingRepository.VET,"QA Vet")
  val livestock=object:LivestockRepository by base{override suspend fun animal(id:String)=base.animal(id).copy(recentCare=listOf(CareSummary(repo.record!!.id,repo.record!!.title,"QA Vet","2020-02-29","Summary")))}
  open(language){val vm=remember{LivestockViewModel(livestock)};DisposableEffect(vm){vm.start();onDispose{vm.close()}};LivestockShell(vm,Profile(base.owner,"qa@example.invalid","QA","Owner",Role.GANADERO),{},repo)}
  compose.onNodeWithText(text(R.string.ls_tab_cattle)).performClick();compose.onNodeWithText("Luna").performScrollTo().performClick();compose.onNodeWithTag("care_latest").performScrollTo().performClick()
  compose.waitUntil(8000){compose.onAllNodesWithText("Descripción visible al propietario.").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithTag("care_save").assertDoesNotExist();compose.onNodeWithTag("vet_care_unavailable").assertDoesNotExist();compose.runOnIdle{assertEquals(0,repo.posts)}
 }
 private fun DebugLivestockRepository.animalsForTest()=java.util.UUID(0L,100L).toString()
 @Test fun spanishVetCreatesAndOpensLatest()=vet("es")
 @Test fun englishVetCreatesAndOpensLatest()=vet("en")
 @Test fun spanishOwnerReadsLatestWithoutWriteActions()=owner("es")
 @Test fun englishOwnerReadsLatestWithoutWriteActions()=owner("en")
}
