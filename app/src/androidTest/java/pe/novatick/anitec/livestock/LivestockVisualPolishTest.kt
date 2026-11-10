package pe.novatick.anitec.livestock

import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.R
import java.util.Locale

class LivestockVisualPolishTest {
    @get:Rule val compose=createComposeRule()
    private fun text(language: String,id: Int,vararg args: Any): String {
        val original=InstrumentationRegistry.getInstrumentation().targetContext
        val config=Configuration(original.resources.configuration).apply {setLocale(Locale.forLanguageTag(language));fontScale=1f}
        val resources=original.createConfigurationContext(config).resources
        return if(resources.getResourceTypeName(id)=="plurals") resources.getQuantityString(id,if((args[0] as Number).toLong()==1L) 1 else 2,*args) else resources.getString(id,*args)
    }
    @Test fun localeParityUsesSameTypographyAndGeometryAtOneHundredPercent() {
        var language by mutableStateOf("es");val repo=LivestockReviewRepository()
        compose.setContent {LivestockLocaleFixture(language,repo)}
        compose.onNodeWithText(text("es",R.string.ls_tab_cattle)).performClick()
        fun style(): List<Any?> {
            val result=mutableListOf<TextLayoutResult>()
            compose.onNodeWithTag("livestock_title").performSemanticsAction(SemanticsActions.GetTextLayoutResult) {it(result)}
            val s=result.single().layoutInput.style
            return listOf(s.fontFamily,s.fontSize,s.fontWeight,s.lineHeight,s.color,result.single().layoutInput.density.fontScale,result.single().layoutInput.density.density)
        }
        val before=style();val title=compose.onNodeWithTag("livestock_title").fetchSemanticsNode().boundsInRoot
        val search=compose.onNodeWithTag("livestock_search").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle {language="en"}
        compose.waitForIdle()
        assertEquals(before,style())
        val translatedTitle=compose.onNodeWithTag("livestock_title").fetchSemanticsNode().boundsInRoot
        assertEquals(title.height,translatedTitle.height,0.5f)
        assertEquals(title.left,translatedTitle.left,0.5f)
        // Translated text may naturally be wider; compare fixed input geometry, not glyph width.
        val translatedSearch=compose.onNodeWithTag("livestock_search").fetchSemanticsNode().boundsInRoot
        assertEquals(search.width,translatedSearch.width,0.5f);assertEquals(search.height,translatedSearch.height,0.5f)
        assertEquals(1f,before[5])
        compose.onNodeWithText(text("en",R.string.ls_tab_cattle)).assertExists()
        compose.onNodeWithText(text("en",R.string.ls_register_animal)).assertIsEnabled()
    }
    @Test fun farmValidationIsInlineCancelKeepsNoWritesAndKeyboardFieldsAreReachable() {
        val repo=LivestockReviewRepository();compose.setContent {LivestockLocaleFixture("es",repo)}
        compose.onNodeWithText(text("es",R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithTag("my_farms").performScrollTo().performClick()
        compose.onNodeWithText(text("es",R.string.ls_add_farm)).performClick()
        compose.onNodeWithText(text("es",R.string.ls_save)).performClick()
        compose.onNodeWithText(text("es",R.string.ls_name_error)).assertExists()
        compose.onNodeWithTag("farm_name").performScrollTo().performTextInput("Review farm")
        compose.onNodeWithTag("farm_location").performScrollTo().performTextInput("x".repeat(256))
        compose.onNodeWithText(text("es",R.string.ls_save)).performClick()
        compose.onNodeWithText(text("es",R.string.ls_location_error)).assertExists()
        compose.runOnIdle {assertEquals(0,repo.writes)}
        compose.onNodeWithText(text("es",R.string.ls_cancel)).performClick()
        compose.runOnIdle {assertEquals(0,repo.writes)}
    }
    @Test fun consistentCountersKeepGlobalCapacityDistinctFromFilteredResults() {
        val repo=LivestockReviewRepository();compose.setContent {LivestockLocaleFixture("es",repo)}
        compose.onNodeWithText(text("es",R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithContentDescription(text("es",R.string.ls_capacity,12L,15L,3L)).assertExists()
        compose.onNodeWithTag("species_${repo.catalog.first().id}").performScrollTo().performClick()
        compose.waitUntil(5000) {compose.onAllNodesWithText(text("es",R.plurals.ls_result_count,3)).fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithText(text("es",R.plurals.ls_active_global_count,12L)).assertExists()
        compose.onNodeWithContentDescription(text("es",R.string.ls_capacity,12L,15L,3L)).assertExists()
    }
    @Test fun emptyFarmListDoesNotInventData() {
        val repo=LivestockReviewRepository("no-farms");compose.setContent {LivestockLocaleFixture("es",repo)}
        compose.onNodeWithText(text("es",R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithTag("my_farms").performScrollTo().performClick()
        compose.onAllNodesWithText(text("es",R.string.ls_no_farms)).assertCountEquals(2)
        compose.onNodeWithText(text("es",R.string.ls_add_farm)).assertIsEnabled()
        compose.runOnIdle {assertEquals(0,repo.writes)}
    }
}
