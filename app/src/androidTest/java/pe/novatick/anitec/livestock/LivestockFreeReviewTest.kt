package pe.novatick.anitec.livestock

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.R
import pe.novatick.anitec.debug.LivestockReviewActivity

class LivestockFreeReviewTest {
    @get:Rule val compose=createAndroidComposeRule<LivestockReviewActivity>()
    private fun text(id: Int)=compose.activity.getString(id)
    @Test fun standaloneDebugReviewAllowsManualNavigationAndControlledSaveError() {
        compose.onNodeWithText("AR").assertIsDisplayed()
        compose.onNodeWithText(text(R.string.qa_local)).performClick()
        compose.onNodeWithText(text(R.string.qa_save_error)).performClick()
        compose.onNodeWithText(text(R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithTag("farm_filter").performScrollTo().performClick()
        compose.onNodeWithText("Finca Esperanza").performClick()
        compose.onNodeWithTag("my_farms").performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.ls_add_farm)).performClick()
        compose.onNodeWithTag("farm_name").performTextInput("Controlled manual review")
        compose.onNodeWithText(text(R.string.ls_save)).assertIsDisplayed().performClick()
        compose.onNodeWithText(text(R.string.ls_form_error)).assertExists()
        val surface=compose.onNodeWithTag("livestock_modal_surface").fetchSemanticsNode().boundsInRoot
        val footer=compose.onNodeWithTag("livestock_modal_actions").fetchSemanticsNode().boundsInRoot
        assertTrue(footer.bottom<surface.bottom && footer.top>=surface.top)
        compose.onNodeWithText(text(R.string.ls_cancel)).assertIsDisplayed().performClick()
        compose.onNodeWithText(text(R.string.ls_close)).performClick()
        compose.onNodeWithText(text(R.string.ls_tab_home)).performClick()
        compose.onNodeWithText("AR").assertIsDisplayed()
    }
    @Test fun debugPendingSaveResolvesAndDoesNotTrapReviewerInBusyModal() {
        compose.onNodeWithText(text(R.string.qa_local)).performClick()
        compose.onNodeWithText(text(R.string.qa_saving)).performClick()
        compose.onNodeWithText(text(R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithTag("my_farms").performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.ls_add_farm)).performClick()
        compose.onNodeWithTag("farm_name").performTextInput("Controlled pending review")
        compose.onNodeWithText(text(R.string.ls_save)).performClick()
        compose.waitUntil(6000) {compose.onAllNodesWithText(text(R.string.ls_form_error)).fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithText(text(R.string.ls_cancel)).assertIsDisplayed().assertIsEnabled().performClick()
        compose.onNodeWithText(text(R.string.ls_close)).performClick()
        compose.onNodeWithText(text(R.string.qa_local)).assertIsDisplayed().performClick()
        compose.onNodeWithText(text(R.string.qa_scenarios)).assertIsDisplayed()
    }
}
