package pe.novatick.anitec.livestock

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.livestock.presentation.*
import pe.novatick.anitec.features.livestock.domain.*
import java.io.File
import java.util.Locale

class AnimalDeactivationScrimTest {
    @get:Rule val compose=createComposeRule()
    private val instrumentation get()=InstrumentationRegistry.getInstrumentation()
    private fun text(lang: String,id: Int)=instrumentation.targetContext.let {
        it.createConfigurationContext(Configuration(it.resources.configuration).apply {setLocale(Locale.forLanguageTag(lang));fontScale=1f}).getString(id)
    }
    private fun image(): Bitmap {compose.waitForIdle();SystemClock.sleep(180);return instrumentation.uiAutomation.takeScreenshot() ?: error("No screenshot")}
    private fun save(bitmap: Bitmap,lang: String,name: String) {
        assertEquals(1080,bitmap.width);assertEquals(2340,bitmap.height)
        val file=File(instrumentation.targetContext.getExternalFilesDir(null),"phase3c-scrim/$lang/$name.png").apply {parentFile!!.mkdirs()}
        file.outputStream().use {assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))}
    }
    private fun open(lang: String) {
        compose.onNodeWithContentDescription(text(lang,R.string.animal_more)).performClick()
        compose.onNodeWithTag("animal_deactivate").performClick()
        compose.onNodeWithTag("animal_deactivation_dialog").assertIsDisplayed()
    }
    private fun visual(lang: String) {
        val repo=LivestockReviewRepository();compose.setContent {LivestockLocaleFixture(lang,repo)}
        compose.onNodeWithText(text(lang,R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithText("Nina").performScrollTo().performClick()
        val before=image();save(before,lang,"01-before")
        open(lang);val after=image();save(after,lang,"02-modal")
        // White margins of header, detail and ANITEC bottom navigation, plus green header.
        val samples=listOf(.02f to .14f,.02f to .30f,.02f to .92f,.50f to .065f)
        for((xf,yf) in samples) {
            val x=(before.width*xf).toInt();val y=(before.height*yf).toInt()
            val a=before.getPixel(x,y);val b=after.getPixel(x,y)
            for(shift in listOf(16,8,0)) {
                val expected=((a shr shift) and 255)*.56
                assertEquals("Scrim channel at $xf,$yf",expected,((b shr shift) and 255).toDouble(),7.0)
            }
        }
        val card=compose.onNodeWithTag("animal_deactivation_dialog").fetchSemanticsNode().boundsInRoot
        // Sample the clear inner left margin, away from text/buttons: card must remain white.
        val c=after.getPixel((card.left+12).toInt(),card.center.y.toInt())
        assertTrue("White card must not be dimmed",listOf(16,8,0).all {((c shr it) and 255)>245})
        before.recycle();after.recycle()
        compose.onNodeWithTag("animal_cancel_deactivate").assertIsDisplayed().performClick()
        compose.onNodeWithTag("animal_deactivation_dialog").assertDoesNotExist()
        // Open/back and open/outside are safe and do not navigate the underlying screen.
        repeat(3) {open(lang);Espresso.pressBack();compose.onNodeWithTag("animal_deactivation_dialog").assertDoesNotExist()}
        open(lang);compose.onNodeWithTag("animal_deactivation_scrim").performTouchInput {click(Offset(10f,10f))}
        compose.onNodeWithTag("animal_deactivation_dialog").assertDoesNotExist()
        compose.onNodeWithTag("animal_header_title").assertTextEquals("Nina")
        compose.runOnIdle {assertEquals(0,repo.mutationWrites)}
    }
    @Test fun renderedScrimCoversHeaderDetailAndBottomBarSpanish()=visual("es")
    @Test fun renderedScrimCoversHeaderDetailAndBottomBarEnglish()=visual("en")
    @Test fun busyModalBlocksBackOutsideCancelAndDuplicateConfirmOverScrolledContent() {
        val animal=AnimalDetail("id","CODE","Nina","Bovino","species",AnimalSex.HEMBRA,null,null,AnimalStatus.ACTIVO,"farm","Farm",0,null)
        var show by mutableStateOf(false);var mutation by mutableStateOf(AnimalMutation());var confirms=0;var cancels=0
        compose.setContent {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                repeat(40) {Text("QA row $it")}
                Button({show=true}) {Text("Open modal")}
            }
            if(show) AnimalDeactivationDialog(animal,mutation,{cancels++;show=false},{confirms++;mutation=AnimalMutation(busy=true)})
        }
        compose.onNodeWithText("Open modal").performScrollTo().performClick()
        compose.onNodeWithTag("animal_confirm_deactivate").performClick()
        compose.onNodeWithTag("animal_confirm_deactivate").assertIsNotEnabled().performTouchInput {click()}
        compose.onNodeWithTag("animal_cancel_deactivate").assertIsNotEnabled()
        Espresso.pressBack()
        compose.onNodeWithTag("animal_deactivation_scrim").performTouchInput {click(Offset(10f,10f))}
        compose.onNodeWithTag("animal_deactivation_dialog").assertIsDisplayed()
        compose.runOnIdle {assertEquals(1,confirms);assertEquals(0,cancels);mutation=AnimalMutation()}
        compose.onNodeWithTag("animal_cancel_deactivate").performClick()
        compose.onNodeWithTag("animal_deactivation_dialog").assertDoesNotExist()
        compose.runOnIdle {assertEquals(1,cancels)}
    }
}

/** Exercise the same edge-to-edge Activity the Team Leader uses, not only the test host. */
class AnimalDeactivationReviewScrimTest {
    @get:Rule val compose=androidx.compose.ui.test.junit4.createAndroidComposeRule<pe.novatick.anitec.debug.LivestockReviewActivity>()
    @Test fun realDebugReviewActivityDimsItsBottomNavigationAndKeepsCardWhite() {
        val activity=compose.activity
        compose.onNodeWithText(activity.getString(R.string.ls_tab_cattle)).performClick()
        compose.onNodeWithText("Nina").performScrollTo().performClick()
        compose.waitForIdle()
        val i=InstrumentationRegistry.getInstrumentation();val before=i.uiAutomation.takeScreenshot()!!
        compose.onNodeWithContentDescription(activity.getString(R.string.animal_more)).performClick()
        compose.onNodeWithTag("animal_deactivate").performClick()
        compose.onNodeWithTag("animal_deactivation_dialog").assertIsDisplayed()
        compose.waitForIdle();SystemClock.sleep(180)
        val after=i.uiAutomation.takeScreenshot()!!
        for((xf,yf) in listOf(.02f to .14f,.02f to .30f,.02f to .92f,.5f to .065f)) {
            val x=(after.width*xf).toInt();val y=(after.height*yf).toInt()
            val a=before.getPixel(x,y);val b=after.getPixel(x,y)
            for(shift in listOf(16,8,0)) assertEquals("Actual QA Activity scrim",((a shr shift) and 255)*.56,((b shr shift) and 255).toDouble(),7.0)
        }
        val file=File(activity.getExternalFilesDir(null),"phase3c-scrim/es/03-real-review-modal.png").apply {parentFile!!.mkdirs()}
        file.outputStream().use {assertTrue(after.compress(Bitmap.CompressFormat.PNG,100,it))}
        before.recycle();after.recycle()
        compose.onNodeWithTag("animal_cancel_deactivate").performClick()
        compose.onNodeWithTag("animal_header_title").assertTextEquals("Nina")
    }
}
