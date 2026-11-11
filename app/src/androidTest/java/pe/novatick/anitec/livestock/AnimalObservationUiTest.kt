package pe.novatick.anitec.livestock
import android.content.res.Configuration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import pe.novatick.anitec.R
import java.util.Locale
class AnimalObservationUiTest {
    @get:Rule val compose=createComposeRule()
    private fun text(lang: String,id: Int)=InstrumentationRegistry.getInstrumentation().targetContext.let {it.createConfigurationContext(Configuration(it.resources.configuration).apply {setLocale(Locale.forLanguageTag(lang))}).getString(id)}
    private fun open(lang: String,scenario: String="content") {
        compose.setContent {LivestockLocaleFixture(lang,LivestockReviewRepository(scenario))}
        compose.onNodeWithText(text(lang,R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithText("Luna").performScrollTo().performClick()
        compose.onNodeWithText(text(lang,R.string.obs_add)).performScrollTo().performClick()
    }
    private fun flow(lang: String) {
        open(lang)
        compose.onNodeWithTag("observation_save").performScrollTo().performClick()
        compose.onNodeWithText(text(lang,R.string.obs_invalid)).assertExists()
        compose.onNodeWithTag("observation_text").performTextInput("Observation QA\nSecond line")
        compose.onNodeWithTag("observation_save").performScrollTo().performClick()
        compose.onNodeWithText("Observation QA\nSecond line").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(if(lang=="es") "Ver todas (4)" else "View all (4)").performScrollTo().performClick()
        compose.onNodeWithText("Observation QA\nSecond line").assertExists()
        compose.onNodeWithText("Permanece tranquila durante la tarde.").assertExists()
    }
    @Test fun spanishMultilineValidationSaveAndFullList()=flow("es")
    @Test fun englishMultilineValidationSaveAndFullList()=flow("en")
    @Test fun cancelCreatesNothing() {
        open("es");compose.onNodeWithTag("observation_text").performTextInput("Cancelled QA")
        compose.onNodeWithText(text("es",R.string.ls_cancel)).performScrollTo().performClick()
        compose.onNodeWithText("Cancelled QA").assertDoesNotExist()
        compose.onNodeWithText("Ver todas (3)").performScrollTo().assertExists()
    }
    @Test fun uncertainResultKeepsDraftAndPreventsRetry() {
        open("en","save-uncertain");compose.onNodeWithTag("observation_text").performTextInput("Unconfirmed QA")
        compose.onNodeWithTag("observation_save").performScrollTo().performClick()
        compose.onNodeWithTag("observation_save").assertIsNotEnabled()
        compose.onNodeWithTag("observation_text").assertTextContains("Unconfirmed QA")
    }
    @Test fun savingUsesObservationMessageAndBlocksSecondSubmit() {
        open("en","obs-saving");compose.onNodeWithTag("observation_text").performTextInput("Pending QA")
        compose.onNodeWithTag("observation_save").performScrollTo().performClick()
        compose.onNodeWithText(text("en",R.string.obs_saving)).assertExists()
        compose.onNodeWithTag("observation_save").assertIsNotEnabled()
        compose.onNodeWithText(text("en",R.string.ls_cancel)).assertIsNotEnabled()
    }
}
