package pe.novatick.anitec.linking
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.debug.DebugLinkingRepository
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.presentation.*
import pe.novatick.anitec.ui.theme.AniTecTheme
import java.util.*
import java.text.SimpleDateFormat

class VisitsUiTest {
 @get:Rule val compose=createComposeRule()
 private lateinit var repo:DebugLinkingRepository
 private fun open(language:String="es",scenario:String="content"){
  repo=DebugLinkingRepository(scenario)
  compose.setContent{
   val context=InstrumentationRegistry.getInstrumentation().targetContext
   val config=remember{Configuration(context.resources.configuration).apply{setLocale(Locale.forLanguageTag(language));fontScale=1f}}
   val localized=remember{context.createConfigurationContext(config)}
   CompositionLocalProvider(LocalContext provides localized,LocalResources provides localized.resources,LocalConfiguration provides config){AniTecTheme{VeterinaryLinkingShell(repo,Profile(DebugLinkingRepository.VET,"qa@example.invalid","María","López",Role.VETERINARIO)){}}}
  }
 }
 private fun dateAndTime(selected:Calendar?=null){
  compose.onNodeWithTag("vis_date").performScrollTo().performClick()
  val target=selected ?: Calendar.getInstance().apply{add(Calendar.DAY_OF_MONTH,1);set(Calendar.HOUR_OF_DAY,10);set(Calendar.MINUTE,30)}
  val tag="vis_pick_"+SimpleDateFormat("yyyy-MM-dd",Locale.US).format(target.time)
  if(compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty())compose.onNodeWithTag("vis_month_next").performClick()
  compose.onNodeWithTag(tag).performClick()
  compose.onNodeWithTag("vis_time").performScrollTo().performClick()
  compose.onNodeWithTag("vis_hour").performTextReplacement(target.get(Calendar.HOUR_OF_DAY).toString());compose.onNodeWithTag("vis_minute").performTextReplacement(target.get(Calendar.MINUTE).toString())
  compose.onNodeWithTag("vis_time_choose").performClick()
 }
 private fun fullForm(language:String){
  open(language);compose.onNodeWithTag("home_schedule_unavailable").performScrollTo().assertIsEnabled().performClick()
  compose.onNodeWithTag("vis_save").performScrollTo().performClick();assertEquals(0,repo.schedules)
  compose.onNodeWithTag("vis_farmer").performScrollTo().performClick();compose.onNodeWithText("Luis Martínez").performClick()
  compose.onNodeWithTag("vis_animal").performScrollTo().performClick();compose.onNodeWithText("Luna").performClick()
  compose.onNodeWithTag("vis_reason").performScrollTo().performTextInput("Control QA")
  val target=if(language=="es")Calendar.getInstance().apply{add(Calendar.MINUTE,30)} else null
  dateAndTime(target);compose.onNodeWithTag("vis_save").performScrollTo().performClick()
  compose.onNodeWithText(if(language=="es")"Mi agenda" else "My agenda").assertExists()
  compose.onNodeWithText("Control QA").performScrollTo().performClick()
  compose.onNodeWithText(if(language=="es")"Detalle de visita" else "Visit details").assertExists()
  compose.onNodeWithText("Luna").assertExists();compose.onNodeWithText("María López").assertExists();assertEquals(1,repo.schedules)
  if(target!=null){compose.onNodeWithText("Inicio",substring=false).performClick();compose.onNodeWithTag("home_visit_count",useUnmergedTree=true).assertTextContains(if(pe.novatick.anitec.features.linking.domain.VisitDay.current().contains(target.timeInMillis))"1" else "0")}
 }
 @Test fun spanishHomeFormPersistsIntoAgendaAndDetail()=fullForm("es")
 @Test fun englishUsesSameCompleteFlow()=fullForm("en")
 @Test fun contextualModalHasNoSelectorsAndCancelNeverPosts(){
  open();compose.onNodeWithText("Ganaderos",substring=false).performClick();compose.onNodeWithText("Luis Martínez").performScrollTo().performClick()
  compose.onNodeWithTag("linked_view_animals").performScrollTo().performClick();compose.onNodeWithText("Luna").performScrollTo().performClick()
  compose.onNodeWithTag("vet_schedule_unavailable").performScrollTo().performClick()
  compose.onNodeWithTag("vis_farmer").assertDoesNotExist();compose.onNodeWithTag("vis_animal").assertDoesNotExist()
  compose.onNodeWithTag("vis_cancel").performClick();assertEquals(0,repo.schedules)
  compose.onNodeWithTag("vet_schedule_unavailable").performScrollTo().performClick();compose.onNodeWithTag("vis_reason").performTextInput("Contextual QA")
  dateAndTime();compose.onNodeWithTag("vis_save").performClick();compose.onNodeWithTag("visit_modal").assertDoesNotExist();assertEquals(1,repo.schedules)
  compose.onNodeWithTag("vet_care_unavailable").assertIsNotEnabled()
 }
 @Test fun agendaNavigatesBeyondExampleDatesAndFailedReadIsNotEmpty(){
  open(scenario="visits");compose.onNodeWithText("Ver agenda").performScrollTo().performClick()
  compose.onNodeWithText("Control general").assertExists();compose.onNodeWithTag("vis_next_days").performScrollTo().performClick();compose.onNodeWithText("Control general").assertDoesNotExist();compose.onNodeWithText("Inicio",substring=false).performClick();compose.onNodeWithText("Ver agenda").performScrollTo().performClick();compose.onNodeWithText("Control general").assertExists();compose.onNodeWithTag("vis_next_days").performScrollTo().performClick();compose.onNodeWithTag("vis_previous_days").performClick();compose.onNodeWithText("Control general").assertExists()
 }
 @Test fun homeVisitOpensDetailAndCareRemainsDisabled(){open(scenario="visits");compose.onNodeWithText("Control general").performScrollTo().performClick();compose.onNodeWithText("Detalle de visita").assertExists();compose.onNodeWithText("Registrar atención").performScrollTo().assertIsNotEnabled()}
}
