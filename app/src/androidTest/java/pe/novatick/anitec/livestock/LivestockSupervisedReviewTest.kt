package pe.novatick.anitec.livestock

import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.R
import java.io.File
import java.util.Locale

/** No timer changes pages. Only a host command after the Team Leader's confirmation does. */
class LivestockSupervisedReviewTest {
    @get:Rule val compose=createComposeRule()
    @Test fun supervisedSingleStateReview() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val arguments=InstrumentationRegistry.getArguments()
        val manual=arguments.getString("manualLivestockReview")=="true"
        val pattern=Regex("(es|en):([1-9]|1[0-8])")
        val start=arguments.getString("reviewStart")?.takeIf {pattern.matches(it)} ?: "es:1"
        var current by mutableStateOf(start)
        fun scenario(command: String)=when(command.substringAfter(':').toInt()) {8->"empty";13->"loading";14->"error";15->"saving";16->"save-error";else->"content"}
        var repository by mutableStateOf(LivestockReviewRepository(scenario(start)))
        compose.setContent {key(current) {LivestockLocaleFixture(current.substringBefore(':'),repository)}}
        fun text(language: String,id: Int,vararg args: Any): String {
            val config=Configuration(instrumentation.targetContext.resources.configuration).apply {setLocale(Locale.forLanguageTag(language));fontScale=1f}
            val resources=instrumentation.targetContext.createConfigurationContext(config).resources
            return if(resources.getResourceTypeName(id)=="plurals") resources.getQuantityString(id,if((args[0] as Number).toLong()==1L) 1 else 2,*args) else resources.getString(id,*args)
        }
        fun prepare(command: String) {
            val language=command.substringBefore(':');val stage=command.substringAfter(':').toInt()
            compose.waitForIdle()
            if(stage==1) return
            compose.onNodeWithText(text(language,R.string.ls_tab_cattle)).performClick()
            when(stage) {
                3 -> {compose.onNodeWithTag("livestock_search").performTextInput("ANI-000001")
                    compose.waitUntil(5000) {compose.onAllNodesWithText(text(language,R.plurals.ls_result_count,1)).fetchSemanticsNodes().isNotEmpty()}}
                4 -> compose.onNodeWithTag("farm_filter").performScrollTo().performClick()
                5 -> compose.onNodeWithTag("species_${repository.catalog.first().id}").performScrollTo().performClick()
                6 -> compose.onNodeWithTag("status_filter").performScrollTo().performClick()
                7 -> compose.onNodeWithTag("animal_${java.util.UUID(0L,100L)}").performScrollTo()
                17 -> compose.onNodeWithTag("animal_${java.util.UUID(0L,104L)}").performScrollTo()
                8 -> compose.onNodeWithText(text(language,R.string.ls_no_animals)).performScrollTo()
                9 -> {compose.onNodeWithTag("livestock_search").performTextInput("NO-MATCH")
                    compose.waitUntil(5000) {compose.onAllNodesWithText(text(language,R.string.ls_no_results)).fetchSemanticsNodes().isNotEmpty()}
                    compose.onNodeWithText(text(language,R.string.ls_no_results)).performScrollTo()}
                10,11,12,15,16,18 -> {
                    compose.onNodeWithTag("my_farms").performScrollTo().performClick()
                    if(stage!=10) compose.onNodeWithText(text(language,R.string.ls_add_farm)).performClick()
                    if(stage==12) compose.onNodeWithText(text(language,R.string.ls_save)).performClick()
                    if(stage==18) {
                        compose.onNodeWithTag("farm_location").performScrollTo().performTextInput("x".repeat(256))
                        compose.onNodeWithText(text(language,R.string.ls_save)).performClick()
                    }
                    if(stage==15 || stage==16) {
                        compose.onNodeWithTag("farm_name").performTextInput("Finca de revisión")
                        compose.onNodeWithText(text(language,R.string.ls_save)).performClick()
                    }
                }
            }
            compose.waitForIdle()
        }
        prepare(start)
        if(!manual) {compose.onNodeWithText(text("es",R.string.ls_greeting,"Alex")).assertExists();return}
        val command=File(instrumentation.targetContext.cacheDir,"anitec-livestock-review-command.txt")
        val ready=File(instrumentation.targetContext.cacheDir,"anitec-livestock-review-ready.txt")
        command.writeText(start);ready.writeText(start)
        var shown=start
        while(true) {
            val next=command.readText().trim()
            if(next=="finish") break
            if(next!=shown && pattern.matches(next)) {
                ready.writeText("preparing")
                compose.runOnIdle {repository=LivestockReviewRepository(scenario(next));current=next}
                prepare(next);shown=next;ready.writeText(next)
            }
            Thread.sleep(250)
        }
    }
}
