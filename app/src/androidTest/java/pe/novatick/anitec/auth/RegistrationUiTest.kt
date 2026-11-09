package pe.novatick.anitec.auth

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.presentation.*
import pe.novatick.anitec.ui.theme.AniTecTheme

/** UI contract tests with a controlled repository; not evidence of email delivery. */
class RegistrationUiTest {
    @get:Rule val compose=createComposeRule()
    @Test fun otpPasteKeepsLeadingZeroesAndRejectsNonAscii() {
        var value=""
        compose.setContent { var code by remember { mutableStateOf("") }
            OtpInputField(code) { code=it;value=it }
        }
        compose.onNodeWithTag("verification_otp").performTextInput("001234")
        compose.runOnIdle { assertEquals("001234",value) }
        compose.onNodeWithTag("verification_otp").performTextReplacement("１２３４５６")
        compose.runOnIdle { assertEquals("001234",value) }
        compose.onNodeWithTag("verification_otp").performTextReplacement("000001")
        compose.runOnIdle { assertEquals("000001",value) }
    }
    private class Fake : RegistrationRepository {
        var createdRole: Role?=null; var verifies=0; var resendCount=0
        override suspend fun register(form: RegistrationForm,language: String): RegistrationResult {
            createdRole=form.role
            return RegistrationResult("11111111-1111-4111-8111-111111111111",form.email)
        }
        override suspend fun verify(email: String,code: String,password: String?) {
            assertEquals("001234",code);verifies++
        }
        override suspend fun resend(email: String,password: String?,language: String): ResendResult {
            resendCount++;return ResendResult(false)
        }
    }
    private fun navigation(role: Role) {
        val repo=Fake()
        val auth=object: AuthRepository {
            override val profile=MutableStateFlow<Profile?>(null)
            override suspend fun restore() {}
            override suspend fun login(email: String,password: String,remember: Boolean) { error("No login should be fabricated") }
            override suspend fun refresh() { error("No refresh expected") }
            override fun logoutLocally(): suspend ()->LogoutResult = error("No logout expected")
        }
        lateinit var registration: RegistrationViewModel
        compose.runOnUiThread { registration=RegistrationViewModel(repo) }
        lateinit var vm: AuthViewModel
        compose.runOnUiThread { vm=AuthViewModel(auth) }
        compose.setContent { AniTecTheme { AniTecAppNavigation(vm,registration) } }
        compose.waitUntil(8000) { compose.onAllNodesWithText("Regístrate").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Regístrate").performScrollTo().performClick()
        val fields=compose.onAllNodes(hasSetTextAction())
        for((index,value) in listOf("Test","Person","ui@example.invalid","Aa1!test","Aa1!test").withIndex()) {
            fields[index].performScrollTo().performTextInput(value)
        }
        if(role==Role.VETERINARIO) compose.onNodeWithText("Veterinario").performScrollTo().performClick()
        compose.onNodeWithText("Términos y condiciones").performScrollTo().performClick()
        compose.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithTag("verification_otp").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("ui@example.invalid").assertExists()
        compose.onNodeWithTag("verification_otp").performScrollTo().performTextInput("001234")
        compose.onNodeWithText("Verificar correo").performScrollTo().performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithText("Acceder").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Acceder").assertExists()
        compose.runOnIdle { assertEquals(role,repo.createdRole);assertEquals(1,repo.verifies);assertNull(vm.profile.value) }
        compose.onNodeWithText("Ganado").assertDoesNotExist();compose.onNodeWithText("Agenda").assertDoesNotExist()
    }
    @Test fun rancherRegistrationVerifyReturnsToLoginWithoutSession() = navigation(Role.GANADERO)
    @Test fun veterinarianRegistrationVerifyReturnsToLoginWithoutSession() = navigation(Role.VETERINARIO)
    @Test fun codeSurvivesErrorAndResendWaitDoesNotDisableVerification() {
        var state by mutableStateOf(RegistrationUiState(step=RegistrationStep.VERIFY,email="test@example.invalid"))
        compose.setContent { AniTecTheme {
            VerifyEmailScreen(state,onVerify={},onChangeEmailClick={},onResend={},onLogin={})
        } }
        compose.onNodeWithTag("verification_otp").performScrollTo().performTextInput("001234")
        compose.runOnIdle { state=state.copy(errorCode="CODE_EXPIRED",waitUntil=System.currentTimeMillis()+60000) }
        compose.onNodeWithText("Verificar correo").performScrollTo().assertIsEnabled()
        compose.onNodeWithTag("verification_otp").assertTextContains("001234")
    }
    @Test fun compositionRecreationKeepsEmailContextAndDoesNotResend() {
        val repo=Fake()
        lateinit var vm: RegistrationViewModel
        compose.runOnUiThread { vm=RegistrationViewModel(repo); vm.continueVerification("retained@example.invalid",null) }
        val restoration=androidx.compose.ui.test.junit4.StateRestorationTester(compose)
        restoration.setContent {
            val state by vm.uiState.collectAsState()
            AniTecTheme { VerifyEmailScreen(state,onVerify=vm::verify,onChangeEmailClick={vm.abandon(true)},
                onResend={vm.resend(it,"es")},onLogin={vm.abandon()}) }
        }
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("retained@example.invalid").assertExists()
        compose.runOnIdle { assertEquals(0,repo.resendCount);assertEquals(RegistrationStep.VERIFY,vm.uiState.value.step) }
    }
    @Test fun unverifiedLoginOffersExplicitContinuationAndKeepsActualEmail() {
        val repo=Fake()
        val auth=object: AuthRepository {
            override val profile=MutableStateFlow<Profile?>(null)
            override suspend fun restore() {}
            override suspend fun login(email: String,password: String,remember: Boolean) { throw AuthFailure(AuthProblem.CREDENTIALS,401,"EMAIL_NOT_VERIFIED") }
            override suspend fun refresh() { error("No refresh") }
            override fun logoutLocally(): suspend ()->LogoutResult = error("No logout")
        }
        lateinit var vm: AuthViewModel
        lateinit var registration: RegistrationViewModel
        compose.runOnUiThread { vm=AuthViewModel(auth);registration=RegistrationViewModel(repo) }
        compose.setContent { AniTecTheme { AniTecAppNavigation(vm,registration) } }
        compose.waitUntil(8000) { compose.onAllNodesWithText("Iniciar Sesión").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Iniciar Sesión").performScrollTo().performClick()
        compose.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextInput("pending@example.invalid")
        compose.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextInput("Aa1!test")
        compose.onNodeWithText("Acceder").performScrollTo().performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithText("Continuar verificación").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Continuar verificación").performScrollTo().performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithTag("verification_otp").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("pending@example.invalid").assertExists()
        compose.runOnIdle { assertNull(vm.profile.value);assertEquals(0,repo.resendCount) }
    }
}
