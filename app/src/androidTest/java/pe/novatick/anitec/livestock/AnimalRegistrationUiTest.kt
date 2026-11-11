package pe.novatick.anitec.livestock
import android.content.res.Configuration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import pe.novatick.anitec.R
import java.util.Locale
import java.util.UUID
import android.view.View
import android.widget.DatePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.ViewAction
import androidx.test.espresso.UiController
import org.hamcrest.Matcher
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
        compose.onNodeWithText("QA-16 · Bovino").assertExists()
        compose.onNodeWithContentDescription(text(lang,R.string.animal_back)).performClick()
        compose.onNodeWithText(text(lang,R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithText("Animal QA").performScrollTo().performClick()
        compose.onNodeWithText("QA-16 · Bovino").assertExists()
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
    private fun correctedValidation(lang: String) {
        compose.setContent {LivestockLocaleFixture(lang,LivestockReviewRepository())}
        compose.onNodeWithText(text(lang,R.string.ls_register_short)).performClick()
        compose.onNodeWithTag("animal_submit").performScrollTo().performClick()
        compose.onNodeWithText(text(lang,R.string.animal_validation)).assertExists()
        compose.onNodeWithTag("animal_farm").performScrollTo().performClick()
        compose.onNodeWithText("Finca Esperanza").performClick()
        compose.onNodeWithText(text(lang,R.string.animal_validation)).assertExists()
        compose.onAllNodesWithText(text(lang,R.string.animal_invalid)).assertCountEquals(1)
        compose.onNodeWithTag("animal_species").performScrollTo().performClick()
        compose.onNodeWithText("Bovino").performClick()
        compose.onNodeWithText(text(lang,R.string.animal_validation)).assertDoesNotExist()
        compose.onNodeWithText(text(lang,R.string.animal_invalid)).assertDoesNotExist()
    }
    @Test fun correctedValidationSpanish()=correctedValidation("es")
    @Test fun correctedValidationEnglish()=correctedValidation("en")
    private fun detailOptions(lang: String) {
        openAndFill(lang)
        val header=compose.onNodeWithTag("animal_header").fetchSemanticsNode().boundsInRoot
        val title=compose.onNodeWithTag("animal_header_title").fetchSemanticsNode().boundsInRoot
        Assert.assertEquals(header.center.x,title.center.x,1f)
        compose.onNodeWithText(text(lang,R.string.animal_breed_label)).assertExists()
        compose.onNodeWithContentDescription(text(lang,R.string.animal_more)).performClick()
        compose.onNodeWithTag("animal_edit").assertIsEnabled()
        compose.onNodeWithTag("animal_deactivate").assertIsEnabled()
        compose.onNodeWithTag("animal_options").assertExists()
        compose.onAllNodesWithText(text(lang,R.string.animal_not_available)).assertCountEquals(0)
        compose.onNodeWithText("QA-16 · Bovino").assertExists()
    }
    @Test fun centeredHeaderAndEnabledOptionsSpanish()=detailOptions("es")
    @Test fun centeredHeaderAndEnabledOptionsEnglish()=detailOptions("en")
    private fun inventoryContext(lang: String) {
        val repo=LivestockReviewRepository()
        compose.setContent {LivestockLocaleFixture(lang,repo)}
        compose.onNodeWithText(text(lang,R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithTag("farm_filter").performScrollTo().performClick()
        compose.onNodeWithText("Finca Esperanza").performClick()
        val species=UUID(0,10).toString();val animal=UUID(0,100).toString()
        compose.onNodeWithTag("species_$species").performScrollTo().performClick()
        compose.onNodeWithTag("livestock_search").performScrollTo().performTextInput("Luna")
        compose.waitUntil(5000) {compose.onAllNodesWithTag("animal_$animal").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithTag("animal_$animal").performScrollTo().performClick()
        compose.onNodeWithText("ANI-000001 · Bovino").assertExists()
        Assert.assertEquals(listOf(animal),repo.animalReads)
        compose.onNodeWithContentDescription(text(lang,R.string.animal_back)).performClick()
        compose.onNodeWithTag("livestock_search").assertTextContains("Luna")
        compose.onNodeWithTag("species_$species").assertIsSelected()
        compose.onNodeWithText("Finca Esperanza").assertExists()
        compose.onNodeWithTag("animal_$animal").assertExists()
    }
    @Test fun selectedUuidAndBackKeepSearchFarmAndSpeciesSpanish()=inventoryContext("es")
    @Test fun selectedUuidAndBackKeepSearchFarmAndSpeciesEnglish()=inventoryContext("en")
    @Test fun nativeDatePickerReturnsCivilIsoAndCancelPreservesDraft() {
        compose.setContent {LivestockLocaleFixture("en",LivestockReviewRepository())}
        compose.onNodeWithText(text("en",R.string.ls_register_short)).performClick()
        compose.onNodeWithTag("animal_birth").performScrollTo().performTextInput("2023-05-10")
        compose.onNodeWithTag("animal_birth_picker").performScrollTo().performClick()
        onView(withId(android.R.id.button2)).perform(click())
        compose.onNodeWithTag("animal_birth").assertTextContains("2023-05-10")
        compose.onNodeWithTag("animal_birth_picker").performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(object: ViewAction {
            override fun getConstraints(): Matcher<View> = isAssignableFrom(DatePicker::class.java)
            override fun getDescription()="Select a known civil date using the native DatePicker"
            override fun perform(controller: UiController,view: View) {
                val picker=view as DatePicker
                Assert.assertTrue(picker.maxDate<=System.currentTimeMillis())
                picker.updateDate(2024,0,15)
            }
        })
        onView(withId(android.R.id.button1)).perform(click())
        compose.onNodeWithTag("animal_birth").assertTextContains("2024-01-15")
    }
}
