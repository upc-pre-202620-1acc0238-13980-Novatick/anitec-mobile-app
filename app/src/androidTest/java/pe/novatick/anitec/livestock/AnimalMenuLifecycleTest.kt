package pe.novatick.anitec.livestock

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.KeyCharacterMap
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import pe.novatick.anitec.R
import java.util.Locale

/** Real frame clock/input, not Compose's virtual test clock. Only the debug, in-memory repository. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion=33)
class AnimalMenuLifecycleTest {
    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    private val automation=instrumentation.uiAutomation
    private val measurements=JSONArray()
    private var anr=false
    private val app="pe.novatick.anitec"

    private fun label(language: String,id: Int): String {
        val context=instrumentation.targetContext
        return context.createConfigurationContext(Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(language))
        }).getString(id)
    }
    private fun shell(command: String): String = automation.executeShellCommand(command).use {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().use {reader->reader.readText()}
    }
    private fun find(root: AccessibilityNodeInfo?,text: String): AccessibilityNodeInfo? {
        if(root==null) return null
        if(root.isVisibleToUser && (root.text?.toString()==text || root.contentDescription?.toString()==text)) return root
        for(index in 0 until root.childCount) find(root.getChild(index),text)?.let {return it}
        return null
    }
    private fun node(text: String): AccessibilityNodeInfo? {
        val active=automation.rootInActiveWindow
        val title=active?.let {root ->
            listOf("AniTec no responde","ANITEC no responde","AniTec isn't responding","AniTec is not responding")
                .any {find(root,it)!=null}
        } ?: false
        if(title) {anr=true;fail("ANR observed while exercising the animal menu")}
        return automation.windows.asSequence().mapNotNull {it.root}.filter {it.packageName?.toString()==app}
            .mapNotNull {find(it,text)}.firstOrNull()
    }
    private fun waitFor(message: String,predicate: ()->Boolean) {
        val end=SystemClock.uptimeMillis()+6000
        while(SystemClock.uptimeMillis()<end) {
            if(predicate()) return
            SystemClock.sleep(16)
        }
        fail("Timed out: $message; no ANR dialog detected")
    }
    private fun tap(text: String) {
        waitFor("visible $text") {node(text)!=null}
        val bounds=Rect();node(text)!!.getBoundsInScreen(bounds)
        touch(bounds.exactCenterX(),bounds.exactCenterY())
    }
    private fun touch(x: Float,y: Float) {
        val start=SystemClock.uptimeMillis()
        for(action in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)) {
            val event=MotionEvent.obtain(start,SystemClock.uptimeMillis(),action,x,y,0).apply {source=InputDevice.SOURCE_TOUCHSCREEN}
            try {assertTrue(automation.injectInputEvent(event,true))} finally {event.recycle()}
        }
    }
    private fun back() {
        val start=SystemClock.uptimeMillis()
        for(action in listOf(KeyEvent.ACTION_DOWN,KeyEvent.ACTION_UP)) {
            val event=KeyEvent(start,SystemClock.uptimeMillis(),action,KeyEvent.KEYCODE_BACK,0,0,
                KeyCharacterMap.VIRTUAL_KEYBOARD,0,0,InputDevice.SOURCE_KEYBOARD)
            automation.injectInputEvent(event,true)
        }
        // The observable popup/navigation state is asserted by the caller, not the injection acknowledgement.
    }
    private fun locale(language: String) {shell("cmd locale set-app-locales $app --user 0 --locales $language")}
    private fun openExistingAnimal(language: String) {
        val more=label(language,R.string.animal_more)
        waitFor("localized home or detail") {node(label(language,R.string.ls_tab_cattle))!=null}
        if(node(more)==null) {
            tap(label(language,R.string.ls_tab_cattle))
            tap("Luna")
        }
        waitFor("existing UUID detail") {node(more)!=null && node("ANI-000001 · Bovino")!=null}
    }
    private fun disabled(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo?=node
        while(current!=null) {if(!current.isEnabled) return true;current=current.parent}
        return false
    }
    private inline fun debugSession(block: ()->Unit) {
        val previousFlags=automation.serviceInfo.flags
        automation.serviceInfo=automation.serviceInfo.apply {flags=previousFlags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS}
        var scenario: ActivityScenario<Activity>?=null
        try {
            locale("es")
            scenario=ActivityScenario.launch(Intent().setClassName(app,"$app.debug.LivestockReviewActivity"))
            block()
        } finally {
            Log.i("ANITEC_MENU_QA",JSONObject().put("test",javaClass.simpleName).put("anr",anr).put("measurements",measurements).toString())
            // If ANR is detected, preserve it for diagnosis instead of waiting for the blocked main thread.
            if(!anr) {scenario?.close();locale("es")}
            automation.serviceInfo=automation.serviceInfo.apply {flags=previousFlags}
        }
    }

    @Test fun realClockMenuOpenAndDismissMeasurements()=debugSession {
        for(language in listOf("es","en")) {
            if(language=="en") locale(language)
            openExistingAnimal(language)
            val more=label(language,R.string.animal_more)
            val edit=label(language,R.string.animal_edit)
            val deactivate=label(language,R.string.animal_deactivate)
            repeat(5) {cycle ->
                waitFor("menu anchor") {node(more)!=null}
                val anchor=Rect();node(more)!!.getBoundsInScreen(anchor)
                val openedAt=System.nanoTime()
                touch(anchor.exactCenterX(),anchor.exactCenterY())
                waitFor("menu accessible") {node(edit)!=null && node(deactivate)!=null}
                val accessibleMs=(System.nanoTime()-openedAt)/1e6
                assertTrue(disabled(node(edit)!!));assertTrue(disabled(node(deactivate)!!))
                // Let the framework's spring animation finish; no virtual clock is used.
                SystemClock.sleep(500)
                val windowId=node(edit)!!.windowId
                val frames=automation.getWindowContentFrameStats(windowId)
                val firstPresentedMs=frames?.let {stats ->
                    (0 until stats.frameCount).map {stats.getFramePresentedTimeNano(it)}
                        .firstOrNull {it>=openedAt}?.let {(it-openedAt)/1e6}
                }
                val closedAt=System.nanoTime()
                if(cycle%2==0) back() else {
                    // A focusable popup intentionally hides the underlying anchor from accessibility.
                    touch(anchor.exactCenterX(),anchor.exactCenterY())
                }
                waitFor("popup removed") {node(edit)==null}
                val dismissMs=(System.nanoTime()-closedAt)/1e6
                waitFor("detail unchanged") {node("ANI-000001 · Bovino")!=null}
                measurements.put(JSONObject().put("language",language).put("cycle",cycle).put("openAccessibleMs",accessibleMs)
                    .put("firstPresentedFrameMs",firstPresentedMs ?: JSONObject.NULL).put("dismissRemovedMs",dismissMs)
                    .put("dismiss",if(cycle%2==0) "back" else "outside"))
            }
            back()
            waitFor("return to inventory") {node(label(language,R.string.ls_inventory))!=null}
        }
    }

    @Test fun changingLocaleWithOpenMenuRecreatesAUsableScreen()=debugSession {
        var language="es"
        openExistingAnimal(language)
        for(next in listOf("en","es","en")) {
            tap(label(language,R.string.animal_more))
            waitFor("open before locale change") {node(label(language,R.string.animal_edit))!=null}
            val started=System.nanoTime()
            locale(next)
            waitFor("responsive translated screen") {node(label(next,R.string.ls_tab_cattle))!=null && node(label(next,R.string.animal_edit))==null}
            measurements.put(JSONObject().put("from",language).put("to",next).put("localeReadyMs",(System.nanoTime()-started)/1e6))
            language=next
            openExistingAnimal(language)
        }
        tap(label(language,R.string.animal_more))
        waitFor("usable final menu") {node(label(language,R.string.animal_edit))!=null}
        back()
        waitFor("final popup dismissed") {node(label(language,R.string.animal_edit))==null}
    }
}
