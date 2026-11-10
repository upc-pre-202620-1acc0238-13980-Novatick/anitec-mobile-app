package pe.novatick.anitec.livestock
import android.content.res.Configuration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import pe.novatick.anitec.R
import java.util.Locale
class AnimalRegistrationUiTest {
    @get:Rule val compose=createComposeRule()
    private fun text(lang: String,id: Int): String {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        return context.createConfigurationContext(Configuration(context.resources.configuration).apply {setLocale(Locale.forLanguageTag(lang));fontScale=1f}).getString(id)
    }
    private fun openAndFill(lang: String,scenario: String="content") {
        val repo=LivestockReviewRepository(scenario)
        compose.setContent {LivestockLocaleFixture(lang,repo)}
        compose.onNodeWithText(text(lang,R.string.ls_register_short)).performClick()
        compose.onNodeWithText(text(lang,R.string.animal_save)).performScrollTo().performClick()
        compose.onAllNodesWithText(text(lang,R.string.animal_invalid)).assertCountEquals(2)
        compose.onAllNodesWithText(text(lang,R.string.animal_select)).onFirst().performScrollTo().performClick()
        compose.onNodeWithText("Finca Esperanza").performClick()
        compose.onNodeWithText(text(lang,R.string.animal_select)).performScrollTo().performClick()
        compose.onNodeWithText("Bovino").performClick()
        compose.onNodeWithTag("animal_name").performScrollTo().performTextInput("Animal QA")
        compose.onNodeWithText(text(lang,R.string.animal_save)).performScrollTo().performClick()
    }
    private fun success(lang: String) {
        openAndFill(lang)
        compose.onNodeWithText("Animal QA").assertExists()
        compose.onNodeWithText("QA-16 - Bovino").assertExists()
        compose.onNodeWithContentDescription(text(lang,R.string.animal_back)).performClick()
        compose.onNodeWithText(text(lang,R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithText("Animal QA").performScrollTo().performClick()
        compose.onNodeWithText("QA-16 - Bovino").assertExists()
    }
    @Test fun registrationAndUuidDetailSpanish()=success("es")
    @Test fun registrationAndUuidDetailEnglish()=success("en")
    @Test fun timeoutKeepsDraftAndBlocksAutomaticResubmit() {
        openAndFill("es","save-uncertain")
        compose.onNodeWithText(text("es",R.string.animal_uncertain)).assertExists()
        compose.onNodeWithTag("animal_submit").assertIsNotEnabled()
        compose.onNodeWithTag("animal_name").assertTextContains("Animal QA")
    }
    @Test fun duplicateKeepsEditableDraftInEnglish() {
        openAndFill("en","save-error")
        compose.onNodeWithText(text("en",R.string.animal_duplicate)).assertExists()
        compose.onNodeWithTag("animal_submit").assertIsEnabled()
        compose.onNodeWithTag("animal_name").assertTextContains("Animal QA")
    }
    @Test fun creatingFarmInsideRegistrationPreservesAnimalDraftAndUsesReturnedIdentity() {
        compose.setContent {LivestockLocaleFixture("es",LivestockReviewRepository("no-farms"))}
        compose.onNodeWithText("Registrar animal").performClick()
        compose.onNodeWithTag("animal_name").performScrollTo().performTextInput("Draft retained")
        compose.onNodeWithText("Agregar finca").performScrollTo().performClick()
        compose.onNodeWithTag("farm_name").performTextInput("Finca nueva 3B")
        compose.onNodeWithText("Guardar").performClick()
        compose.onNodeWithText("Finca nueva 3B").assertExists()
        compose.onNodeWithTag("animal_name").assertTextContains("Draft retained")
    }
}
