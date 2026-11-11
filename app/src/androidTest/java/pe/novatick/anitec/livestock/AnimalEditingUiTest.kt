package pe.novatick.anitec.livestock

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.R
import java.io.File
import java.util.Locale

class AnimalEditingUiTest {
    @get:Rule val compose=createComposeRule()
    private fun text(lang: String,id: Int)=InstrumentationRegistry.getInstrumentation().targetContext.let {
        it.createConfigurationContext(Configuration(it.resources.configuration).apply {setLocale(Locale.forLanguageTag(lang));fontScale=1f}).getString(id)
    }
    private fun open(lang: String,repo: LivestockReviewRepository) {
        compose.setContent {LivestockLocaleFixture(lang,repo)}
        compose.onNodeWithText(text(lang,R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithText("Luna").performScrollTo().performClick()
    }
    private fun menu(lang: String) {compose.onNodeWithContentDescription(text(lang,R.string.animal_more)).performClick()}
    private fun shot(lang: String,name: String) {
        compose.waitForIdle();SystemClock.sleep(250)
        val i=InstrumentationRegistry.getInstrumentation()
        val file=File(i.targetContext.getExternalFilesDir(null),"phase3c-shots/$lang/$name.png").apply {parentFile!!.mkdirs()}
        val bitmap=i.uiAutomation.takeScreenshot() ?: error("Screenshot unavailable")
        assertEquals(1080,bitmap.width);assertEquals(2340,bitmap.height)
        file.outputStream().use {assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))};bitmap.recycle()
    }
    private fun journey(lang: String) {
        val repo=LivestockReviewRepository();open(lang,repo)
        menu(lang);shot(lang,"01-menu")
        compose.onNodeWithTag("animal_edit").assertIsEnabled().performClick()
        compose.onNodeWithTag("animal_edit_code").assertTextContains("ANI-000001")
        compose.onNodeWithTag("animal_edit_name").assertTextContains("Luna");shot(lang,"02-edit-loaded")
        compose.onNodeWithTag("animal_edit_code").performTextClearance();androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithTag("animal_edit_submit").performScrollTo().performClick()
        compose.onNodeWithTag("animal_edit_code").performScrollTo();shot(lang,"03-edit-validation")
        compose.onNodeWithTag("animal_edit_code").performTextReplacement("ANI-000001")
        compose.onNodeWithTag("animal_edit_name").performScrollTo().performTextReplacement("Luna QA")
        compose.onNodeWithTag("animal_edit_breed").performScrollTo().performTextReplacement("Holstein");androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithTag("animal_edit_submit").performScrollTo().performClick()
        compose.onNodeWithTag("animal_header_title").assertTextEquals("Luna QA");shot(lang,"04-saved-detail")
        menu(lang);compose.onNodeWithTag("animal_deactivate").performClick()
        compose.onNodeWithTag("animal_deactivation_dialog").assertIsDisplayed();shot(lang,"05-deactivation-modal")
        compose.onNodeWithTag("animal_cancel_deactivate").performClick()
        compose.onNodeWithTag("animal_deactivation_dialog").assertDoesNotExist()
        compose.runOnIdle {assertEquals(1,repo.mutationWrites)};shot(lang,"06-cancelled-detail")
        menu(lang);compose.onNodeWithTag("animal_deactivate").performClick();compose.onNodeWithTag("animal_confirm_deactivate").performClick()
        compose.onNodeWithText(text(lang,R.string.animal_deactivated)).assertExists();shot(lang,"07-deactivated-detail")
        compose.onNodeWithContentDescription(text(lang,R.string.animal_back)).performClick()
        compose.onNodeWithText("Luna QA").assertDoesNotExist();shot(lang,"08-updated-inventory")
        compose.runOnIdle {assertEquals(2,repo.mutationWrites)}
    }
    @Test fun editCancelDeactivateAndCaptureSpanish()=journey("es")
    @Test fun editCancelDeactivateAndCaptureEnglish()=journey("en")
    private fun error(lang: String,scenario: String,expected: Int) {
        val repo=LivestockReviewRepository(scenario);open(lang,repo);menu(lang);compose.onNodeWithTag("animal_edit").performClick()
        compose.onNodeWithTag("animal_edit_name").performScrollTo().performTextReplacement("Draft kept");androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithTag("animal_edit_submit").performScrollTo().performClick()
        compose.onNodeWithText(text(lang,expected)).assertExists()
        compose.onNodeWithTag("animal_edit_submit").assertIsNotEnabled()
        compose.onNodeWithTag("animal_edit_name").performScrollTo().assertTextContains("Draft kept")
        compose.onNodeWithTag("animal_reload").performScrollTo().assertIsDisplayed();shot(lang,if(scenario=="edit-conflict") "09-conflict" else "10-uncertain")
        compose.runOnIdle {assertEquals(1,repo.mutationWrites)}
    }
    @Test fun conflictKeepsDraftAndBlocksResubmitSpanish()=error("es","edit-conflict",R.string.animal_conflict)
    @Test fun conflictKeepsDraftAndBlocksResubmitEnglish()=error("en","edit-conflict",R.string.animal_conflict)
    @Test fun uncertainKeepsDraftAndBlocksResubmitSpanish()=error("es","save-uncertain",R.string.animal_mutation_uncertain)
    @Test fun uncertainKeepsDraftAndBlocksResubmitEnglish()=error("en","save-uncertain",R.string.animal_mutation_uncertain)
    @Test fun cancellingDeactivationNeverCallsRepository() {
        val repo=LivestockReviewRepository();open("es",repo);menu("es");compose.onNodeWithTag("animal_deactivate").performClick()
        compose.onNodeWithTag("animal_cancel_deactivate").performClick()
        compose.runOnIdle {assertEquals(0,repo.mutationWrites)}
        compose.onNodeWithTag("animal_header_title").assertTextEquals("Luna")
    }
}
