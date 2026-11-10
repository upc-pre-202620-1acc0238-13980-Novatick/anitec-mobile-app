package pe.novatick.anitec.livestock

import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.R
import java.util.Locale

class LivestockFinalVisualTest {
    @get:Rule val compose=createComposeRule()
    private fun text(language: String,id: Int,vararg args: Any): String {
        val original=InstrumentationRegistry.getInstrumentation().targetContext
        val config=Configuration(original.resources.configuration).apply {setLocale(Locale.forLanguageTag(language));fontScale=1f}
        return original.createConfigurationContext(config).resources.getString(id,*args)
    }
    @Test fun homeUsesCurrentUserInitialsAndNoRepeatedCapacityOrTechnicalMessagesInEitherLanguage() {
        var language by mutableStateOf("es");val repo=LivestockReviewRepository()
        compose.setContent {key(language) {LivestockLocaleFixture(language,repo)}}
        for(lang in listOf("es","en")) {
            compose.runOnIdle {language=lang}
            compose.onNodeWithText("AR").assertIsDisplayed()
            compose.onNodeWithText(text(lang,R.string.ls_register_short)).assertIsEnabled().performClick()
            compose.onNodeWithText(text(lang,R.string.ls_invite_vet)).assertIsEnabled().performClick()
            compose.onNodeWithText(text(lang,R.string.ls_open_inventory)).assertDoesNotExist()
            compose.onNodeWithText(text(lang,R.string.ls_visits_neutral)).performScrollTo().assertIsDisplayed()
            compose.onNodeWithText(text(lang,R.string.ls_view_visits)).performClick()
            compose.onNodeWithText(text(lang,R.string.ls_indications_neutral)).performScrollTo().assertIsDisplayed()
            compose.onNodeWithText(text(lang,R.string.ls_future)).assertDoesNotExist()
            compose.onNodeWithTag("livestock_capacity").assertDoesNotExist()
            compose.onNodeWithTag("home_inventory_active").performScrollTo().performClick()
            compose.onNodeWithTag("livestock_title").assertIsDisplayed()
            compose.onNodeWithText(text(lang,R.string.ls_animals_pending)).assertDoesNotExist()
            compose.onNodeWithText(text(lang,R.string.ls_register_animal)).assertIsEnabled().performClick()
            compose.onNodeWithTag("livestock_title").assertIsDisplayed()
            compose.onNodeWithTag("animal_${java.util.UUID(0L,100L)}").assertHasNoClickAction()
            compose.runOnIdle {assertEquals(0,repo.writes)}
        }
    }
    @Test fun simultaneousErrorsHaveOneGeneralNoticeAndInventoryRetryLeavesOtherReadsIndependent() {
        val repo=LivestockReviewRepository("error")
        compose.setContent {LivestockLocaleFixture("es",repo)}
        compose.onNodeWithText(text("es",R.string.ls_tab_cattle)).performClick()
        compose.onAllNodesWithText(text("es",R.string.ls_partial_error)).assertCountEquals(1)
        listOf(R.string.ls_capacity_error,R.string.ls_farms_error,R.string.ls_species_error,R.string.ls_inventory_error).forEach {
            compose.onNodeWithText(text("es",it)).assertExists()
        }
        compose.onNodeWithText(text("es",R.string.ls_error)).assertDoesNotExist()
        var before=emptyMap<String,Int>()
        compose.runOnIdle {before=repo.reads.toMap()}
        compose.onAllNodesWithText(text("es",R.string.ls_retry)).onLast().performScrollTo().performClick()
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(before.getValue("inventory")+1,repo.reads["inventory"])
            listOf("capacity","farms","species").forEach {assertEquals(before[it],repo.reads[it])}
        }
        compose.onNodeWithText(text("es",R.string.ls_inventory_error)).assertDoesNotExist()
        compose.onNodeWithText(text("es",R.string.ls_farms_error)).assertExists()
    }
    @Test fun bothModalActionsRemainReachableWithKeyboardAndLongValidationInBothLanguages() {
        var language by mutableStateOf("es");val repo=LivestockReviewRepository()
        compose.setContent {key(language) {LivestockLocaleFixture(language,repo)}}
        for(lang in listOf("es","en")) {
            compose.runOnIdle {language=lang}
            compose.onNodeWithText(text(lang,R.string.ls_tab_cattle)).performClick()
            compose.onNodeWithTag("my_farms").performScrollTo().performClick()
            compose.onNodeWithText(text(lang,R.string.ls_add_farm)).performClick()
            compose.onNodeWithTag("farm_location").performScrollTo().performTextInput("x".repeat(256))
            compose.onNodeWithText(text(lang,R.string.ls_save)).performClick()
            compose.onNodeWithText(text(lang,R.string.ls_location_error)).assertExists()
            compose.onNodeWithText(text(lang,R.string.ls_name_error)).assertExists()
            compose.onNodeWithText(text(lang,R.string.ls_save)).assertIsDisplayed().assertIsEnabled()
            assertFooterFullyInsideModal()
            compose.onNodeWithText(text(lang,R.string.ls_cancel)).assertIsDisplayed().assertIsEnabled().performClick()
            compose.runOnIdle {assertEquals(0,repo.writes)}
        }
    }
    @Test fun longUncertainSaveMessageWithKeyboardKeepsCancelReachableAndSaveSafelyDisabledInBothLanguages() {
        var language by mutableStateOf("es")
        var repo by mutableStateOf(LivestockReviewRepository("save-uncertain"))
        compose.setContent {key(language,repo) {LivestockLocaleFixture(language,repo)}}
        for(lang in listOf("es","en")) {
            compose.runOnIdle {language=lang;repo=LivestockReviewRepository("save-uncertain")}
            compose.onNodeWithText(text(lang,R.string.ls_tab_cattle)).performClick()
            compose.onNodeWithTag("my_farms").performScrollTo().performClick()
            compose.onNodeWithText(text(lang,R.string.ls_add_farm)).performClick()
            compose.onNodeWithTag("farm_name").performTextInput("Review farm")
            compose.onNodeWithText(text(lang,R.string.ls_save)).performClick()
            compose.onNodeWithText(text(lang,R.string.ls_uncertain)).assertExists()
            compose.onNodeWithText(text(lang,R.string.ls_save)).assertIsDisplayed().assertIsNotEnabled()
            assertFooterFullyInsideModal()
            compose.onNodeWithText(text(lang,R.string.ls_cancel)).assertIsDisplayed().assertIsEnabled().performClick()
            compose.runOnIdle {assertEquals(1,repo.writes)}
        }
    }
    private fun assertFooterFullyInsideModal() {
        val surface=compose.onNodeWithTag("livestock_modal_surface").fetchSemanticsNode().boundsInRoot
        val actions=compose.onNodeWithTag("livestock_modal_actions").fetchSemanticsNode().boundsInRoot
        assertTrue("Footer must be completely above the scrim and inside the white surface",actions.top>=surface.top && actions.bottom<surface.bottom && actions.left>=surface.left && actions.right<=surface.right)
    }
    @Test fun saveErrorKeepsFullFooterVisibleWithKeyboardInBothLanguages() {
        var language by mutableStateOf("es")
        var repo by mutableStateOf(LivestockReviewRepository("save-error"))
        compose.setContent {key(language,repo) {LivestockLocaleFixture(language,repo)}}
        for(lang in listOf("es","en")) {
            compose.runOnIdle {language=lang;repo=LivestockReviewRepository("save-error")}
            compose.onNodeWithText(text(lang,R.string.ls_tab_cattle)).performClick()
            compose.onNodeWithTag("my_farms").performScrollTo().performClick()
            compose.onNodeWithText(text(lang,R.string.ls_add_farm)).performClick()
            compose.onNodeWithTag("farm_name").performTextInput("Review farm")
            compose.onNodeWithText(text(lang,R.string.ls_save)).assertIsDisplayed().performClick()
            compose.onNodeWithText(text(lang,R.string.ls_form_error)).assertExists()
            assertFooterFullyInsideModal()
            compose.onNodeWithText(text(lang,R.string.ls_save)).assertIsDisplayed().assertIsEnabled()
            compose.onNodeWithText(text(lang,R.string.ls_cancel)).assertIsDisplayed().assertIsEnabled().performClick()
            compose.runOnIdle {assertEquals(1,repo.writes)}
        }
    }
}
