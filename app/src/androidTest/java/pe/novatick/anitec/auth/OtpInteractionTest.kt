package pe.novatick.anitec.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.presentation.*
import pe.novatick.anitec.ui.theme.AniTecTheme
import java.util.concurrent.atomic.AtomicBoolean

/** All operations use synthetic clipboard content and a repository double: no network/email. */
@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
class OtpInteractionTest {
    @get:Rule val compose=createComposeRule()
    private fun clipboard(value: String) { compose.runOnUiThread {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("OTP QA",value))
    } }
    @Test fun nativeClipboardPastePartialOverflowAndBackspace() {
        var observed=""
        compose.setContent { var code by remember { mutableStateOf("") }; AniTecTheme {
            OtpInputField(code) { code=it;observed=it }
        } }
        val editor=compose.onNodeWithTag("verification_otp")
        editor.performClick()
        clipboard("001234")
        editor.performSemanticsAction(SemanticsActions.PasteText) { it() }
        compose.runOnIdle { assertEquals("001234",observed) }
        val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        editor.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val bounds=editor.fetchSemanticsNode().boundsInRoot
        repeat(6) { index ->
            val cell=compose.onNodeWithTag("otp_cell_$index",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot
            val center=bounds.left+layouts.single().getBoundingBox(index).center.x
            assertTrue("Digit $index outside its cell: center=$center cell=$cell",center>=cell.left && center<=cell.right)
            assertTrue("Digit $index is not centered: center=$center cell=$cell glyph=${layouts.single().getBoundingBox(index)} layout=${layouts.single().size}",kotlin.math.abs(center-cell.center.x)<6f)
        }
        editor.performKeyInput { pressKey(Key.Backspace) }
        compose.runOnIdle { assertEquals("00123",observed) }
        editor.performTextClearance();clipboard("00")
        editor.performSemanticsAction(SemanticsActions.PasteText) { it() }
        editor.performTextInput("1234")
        compose.runOnIdle { assertEquals("001234",observed) }
        editor.performTextClearance()
        for(value in listOf("1234567","12x456","１２３４５６")) {
            clipboard(value);editor.performSemanticsAction(SemanticsActions.PasteText) { it() }
            compose.runOnIdle { assertEquals("",observed) }
        }
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(1)
        repeat(6) { compose.onNodeWithTag("otp_cell_$it",useUnmergedTree=true).assertExists().assertHasNoClickAction() }
    }
    @Test fun cursorStaysInsideActiveCellDuringManualTypingAndDeletion() {
        var observed=""
        compose.setContent { var code by remember { mutableStateOf("") }; AniTecTheme {
            OtpInputField(code) { code=it;observed=it }
        } }
        val editor=compose.onNodeWithTag("verification_otp")
        fun checkCursor(index: Int) {
            val cell=compose.onNodeWithTag("otp_cell_$index",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot
            val caret=compose.onNodeWithTag("otp_cursor",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot
            assertTrue("Cursor must be inset inside cell $index: $caret / $cell",caret.left>cell.left+4 && caret.right<cell.right-4)
            assertTrue("Cursor must be vertically centered",kotlin.math.abs(caret.center.y-cell.center.y)<2f)
        }
        editor.performClick();checkCursor(0)
        editor.performTextInput("00");checkCursor(2)
        editor.performKeyInput { pressKey(Key.DirectionLeft) };checkCursor(1)
        editor.performKeyInput { pressKey(Key.Backspace) };checkCursor(0)
        compose.runOnIdle { assertEquals("0",observed) }
        editor.performTextClearance();checkCursor(0)
        editor.performTextInput("001234");checkCursor(5)
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(1)
        compose.onNodeWithTag("otp_cursor",useUnmergedTree=true).assertHasNoClickAction()
    }
    @Test fun passwordIsRequestedOnlyOnDemandAndCancelDoesNotResend() {
        val vm=vm()
        screen(vm)
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(1)
        compose.onNodeWithTag("resend_code").performScrollTo().performClick()
        compose.onNodeWithTag("resend_password_dialog").assertExists().performTextInput("private-draft")
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithTag("resend_password_dialog").assertDoesNotExist()
        compose.runOnIdle { assertEquals("",vm.passwordDraftForEntry());assertFalse(vm.uiState.value.passwordPrompt) }
    }
    @Test fun passwordAlreadyAvailableDoesNotOpenDialog() {
        val vm=vm("known-password")
        screen(vm)
        compose.onNodeWithTag("resend_code").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("resend_password_dialog").assertDoesNotExist()
        compose.runOnIdle { assertEquals(RegistrationNotice.GENERIC_RESEND,vm.uiState.value.notice) }
    }
    private fun vm(password: String?=null): RegistrationViewModel {
        val fake=object: RegistrationRepository {
            override suspend fun register(form: RegistrationForm,language: String): RegistrationResult = error("Not used")
            override suspend fun verify(email: String,code: String,password: String?) { throw RegistrationFailure(status=422,code="INVALID_CODE") }
            override suspend fun resend(email: String,password: String?,language: String) = ResendResult(false)
        }
        lateinit var vm: RegistrationViewModel
        compose.runOnUiThread { vm=RegistrationViewModel(fake);vm.continueVerification("qa@example.invalid",password) }
        return vm
    }
    private fun screen(vm: RegistrationViewModel,manual: Boolean=false,finished: AtomicBoolean=AtomicBoolean(false)) {
        compose.setContent {
            val state by vm.uiState.collectAsState()
            AniTecTheme { Box(Modifier.fillMaxSize()) {
                VerifyEmailScreen(state,onVerify=vm::verify,onChangeEmailClick={vm.abandon(true)},onResend={vm.resend(it,"es")},onLogin={vm.abandon()},
                    passwordDraft=vm::passwordDraftForEntry,onPasswordChange=vm::updatePasswordDraft,onDismissPassword=vm::dismissPasswordPrompt)
                if(manual) Row(Modifier.align(Alignment.BottomCenter)) {
                    Button(onClick={
                        val c=InstrumentationRegistry.getInstrumentation().targetContext
                        (c.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Synthetic OTP","001234"))
                    }) { Text("Copiar 001234") }
                    Button(onClick={finished.set(true)}) { Text("Finalizar revisión") }
                }
            } }
        }
    }
    @Test fun sixCellsReviewSurface() {
        val manual=InstrumentationRegistry.getArguments().getString("manualOtpReview")=="true"
        val finished=AtomicBoolean(false);val vm=vm()
        screen(vm,manual,finished)
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(1)
        repeat(6) { compose.onNodeWithTag("otp_cell_$it",useUnmergedTree=true).assertExists() }
        if(manual) compose.waitUntil(600000) { finished.get() }
        compose.runOnIdle { vm.abandon() }
    }
    @Test fun logoutFeedbackNeverAttributesRemoteSuccessToLocalOnlyNotice() {
        var notice by mutableStateOf(Notice.REMOTE_LOGOUT_FAILED)
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent { AniTecTheme {
            LoginScreen(onNavigateToRegister={},onLogin={_,_,_->},busy=false,problem=null,notice=notice,
                onRetryRestore={},onRetryLogout={},logoutBlocked=false,retryLogout=false)
        } }
        compose.onNodeWithText(context.getString(R.string.auth_logout_failed)).assertExists()
        compose.onNodeWithText(context.getString(R.string.auth_logout_ok)).assertDoesNotExist()
        compose.runOnIdle { notice=Notice.REMOTE_LOGOUT_OK }
        compose.onNodeWithText(context.getString(R.string.auth_logout_ok)).assertExists()
        compose.onNodeWithText(context.getString(R.string.auth_logout_failed)).assertDoesNotExist()
    }
    @Test fun dialogUsesEnglishResourcesAndFontScaleKeepsSixCells() {
        val vm=vm()
        val original=InstrumentationRegistry.getInstrumentation().targetContext
        val config=android.content.res.Configuration(original.resources.configuration).apply { setLocale(java.util.Locale.ENGLISH) }
        val english=original.createConfigurationContext(config)
        compose.setContent {
            val state by vm.uiState.collectAsState()
            val density=androidx.compose.ui.platform.LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides english,
                androidx.compose.ui.platform.LocalResources provides english.resources,
                androidx.compose.ui.platform.LocalConfiguration provides config,
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density,2f)
            ) { AniTecTheme {
                VerifyEmailScreen(state,onVerify={},onResend={vm.resend(it,"en")},onLogin={},onChangeEmailClick={},
                    passwordDraft=vm::passwordDraftForEntry,onPasswordChange=vm::updatePasswordDraft,onDismissPassword=vm::dismissPasswordPrompt)
            } }
        }
        repeat(6) { compose.onNodeWithTag("otp_cell_$it",useUnmergedTree=true).performScrollTo().assertIsDisplayed() }
        val editor=compose.onNodeWithTag("verification_otp")
        editor.performClick();clipboard("001234")
        editor.performSemanticsAction(SemanticsActions.PasteText) { it() }
        editor.assertTextEquals("001234")
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(1)
        val lastCell=compose.onNodeWithTag("otp_cell_5",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot
        val caret=compose.onNodeWithTag("otp_cursor",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot
        assertTrue("English cursor must stay in the sixth cell",caret.left>lastCell.left && caret.right<lastCell.right)
        compose.onNodeWithTag("resend_code").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(vm.uiState.value.passwordPrompt) }
        compose.onNodeWithText(english.getString(R.string.reg_resend_dialog_title)).assertExists()
        compose.onNodeWithText("Cancel").performClick()
    }
}
