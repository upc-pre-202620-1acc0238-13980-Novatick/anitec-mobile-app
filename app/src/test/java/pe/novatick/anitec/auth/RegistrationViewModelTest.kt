package pe.novatick.anitec.auth

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.presentation.*

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest {
    private open class Fake : RegistrationRepository {
        var registrations=0; var verifies=0; var resends=0
        var gate: CompletableDeferred<Unit>?=null
        var verifyError: RegistrationFailure?=null
        var resendError: RegistrationFailure?=null
        var result=ResendResult(true,4,61000)
        var seenPassword: String?=null
        override suspend fun register(form: RegistrationForm,language: String): RegistrationResult {
            registrations++; gate?.await(); return RegistrationResult("11111111-1111-4111-8111-111111111111",form.email)
        }
        override suspend fun verify(email: String,code: String,password: String?) {
            verifies++; seenPassword=password; gate?.await(); verifyError?.let { throw it }
        }
        override suspend fun resend(email: String,password: String?,language: String): ResendResult {
            resends++; seenPassword=password; gate?.await(); resendError?.let { throw it }; return result
        }
    }
    private fun form()=RegistrationForm("Test","Person","test@example.invalid","Aa1!test","Aa1!test",Role.GANADERO,true)
    @Test fun duplicateRegistrationIsSuppressedAndStateNeverContainsPassword()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fake=Fake();fake.gate=CompletableDeferred();val vm=RegistrationViewModel(fake)
            vm.register(form(),"es");vm.register(form(),"es");runCurrent();assertEquals(1,fake.registrations)
            assertFalse(vm.uiState.value.toString().contains("Aa1!test"))
            fake.gate!!.complete(Unit);runCurrent();assertEquals(RegistrationStep.VERIFY,vm.uiState.value.step)
            assertFalse(vm.uiState.value.toString().contains("Aa1!test"))
        } finally { Dispatchers.resetMain() }
    }
    @Test fun lateRegistrationDoesNotReplaceChangedContext()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fake=Fake();fake.gate=CompletableDeferred();val vm=RegistrationViewModel(fake)
            vm.register(form(),"es");runCurrent();vm.continueVerification("other@example.invalid",null)
            fake.gate!!.complete(Unit);runCurrent()
            assertEquals("other@example.invalid",vm.uiState.value.email)
            vm.resend(null,"es");assertEquals(RegistrationIssue.CONTEXT,vm.uiState.value.issue);assertEquals(0,fake.resends)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun verifyAndResendAreSerializedAndSuccessReturnsToLoginStage()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fake=Fake();fake.gate=CompletableDeferred();val vm=RegistrationViewModel(fake)
            vm.continueVerification("test@example.invalid","secret")
            vm.verify("001234");vm.resend(null,"es");runCurrent()
            assertEquals(1,fake.verifies);assertEquals(0,fake.resends)
            fake.gate!!.complete(Unit);runCurrent();assertEquals(RegistrationStep.COMPLETE,vm.uiState.value.step)
            vm.abandon();vm.continueVerification("test@example.invalid",null);vm.resend(null,"es")
            assertEquals(RegistrationIssue.CONTEXT,vm.uiState.value.issue)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun failedVerificationKeepsContextAndCooldownNeverEmitsAutomatically()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var now=1000L;val fake=Fake();val vm=RegistrationViewModel(fake) { now }
            vm.continueVerification("test@example.invalid","secret");fake.verifyError=RegistrationFailure(status=422,code="CODE_EXPIRED")
            vm.verify("001234");runCurrent();assertEquals(RegistrationStep.VERIFY,vm.uiState.value.step)
            assertEquals("CODE_EXPIRED",vm.uiState.value.errorCode)
            fake.resendError=RegistrationFailure(status=429,code="RESEND_COOLDOWN",retryAt=61000)
            vm.resend(null,"es");runCurrent();vm.resend(null,"es");runCurrent();assertEquals(1,fake.resends)
            now=62000;advanceUntilIdle();assertEquals(1,fake.resends)
            fake.resendError=null;vm.resend(null,"es");runCurrent();assertEquals(2,fake.resends)
            assertEquals(1L,vm.uiState.value.codeVersion)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun genericResendDoesNotClearOtpOrInventQuotaAndAbandonDiscardsCredential()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fake=Fake();fake.result=ResendResult(false);val vm=RegistrationViewModel(fake)
            vm.continueVerification("test@example.invalid","secret");vm.resend(null,"en");runCurrent()
            assertEquals(RegistrationNotice.GENERIC_RESEND,vm.uiState.value.notice)
            assertEquals(0L,vm.uiState.value.codeVersion);assertNull(vm.uiState.value.remaining)
            vm.abandon(true);vm.continueVerification("test@example.invalid",null);vm.resend(null,"es")
            assertEquals(RegistrationIssue.CONTEXT,vm.uiState.value.issue);assertEquals(1,fake.resends)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun contextIsCapturedBeforeDispatchAndLateVerifyCannotCompleteNewFlow()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fake=Fake();fake.gate=CompletableDeferred();val vm=RegistrationViewModel(fake)
            vm.continueVerification("test@example.invalid","old-secret");vm.verify("001234")
            vm.continueVerification("other@example.invalid","new-secret");runCurrent()
            assertEquals("old-secret",fake.seenPassword)
            fake.gate!!.complete(Unit);runCurrent();assertEquals("other@example.invalid",vm.uiState.value.email)
            assertEquals(RegistrationStep.VERIFY,vm.uiState.value.step)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun pendingLoginContinuationIsExplicitAndDoesNotCreateProfile()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo=object: AuthRepository {
                override val profile=kotlinx.coroutines.flow.MutableStateFlow<Profile?>(null)
                override suspend fun restore() {}
                override suspend fun login(email: String,password: String,remember: Boolean) {
                    throw AuthFailure(AuthProblem.CREDENTIALS,401,"EMAIL_NOT_VERIFIED")
                }
                override suspend fun refresh() { error("No refresh expected") }
                override fun logoutLocally(): suspend ()->LogoutResult = error("No logout expected")
            }
            val vm=AuthViewModel(repo);runCurrent();vm.login("test@example.invalid","existing-long-"+"x".repeat(100),true);runCurrent()
            assertTrue(vm.uiState.value.unverified);assertNull(vm.profile.value)
            var calls=0
            vm.continueVerification { email,password -> assertEquals("test@example.invalid",email);assertTrue(password!!.length>72);calls++ }
            assertEquals(1,calls);assertFalse(vm.uiState.value.unverified)
            vm.continueVerification { _,_ -> calls++ };assertEquals(1,calls)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun resendCooldownDoesNotBlockUsableCodeButVerifyRetryAfterDoes()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fake=Fake();val vm=RegistrationViewModel(fake) { 1000L }
            vm.continueVerification("test@example.invalid","secret")
            fake.resendError=RegistrationFailure(status=429,code="RESEND_COOLDOWN",retryAt=61000)
            vm.resend(null,"es");runCurrent()
            fake.verifyError=RegistrationFailure(status=429,code="VERIFICATION_ORIGIN_LIMIT",retryAt=5000)
            vm.verify("001234");runCurrent();assertEquals(1,fake.verifies)
            vm.verify("001234");runCurrent();assertEquals(1,fake.verifies)
            assertEquals(5000L,vm.uiState.value.verifyWaitUntil)
            assertEquals(61000L,vm.uiState.value.waitUntil)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun lateUnverifiedLoginDoesNotRetainCredentialAfterLeavingLogin()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val gate=CompletableDeferred<Unit>()
            val repo=object: AuthRepository {
                override val profile=kotlinx.coroutines.flow.MutableStateFlow<Profile?>(null)
                override suspend fun restore() {}
                override suspend fun login(email: String,password: String,remember: Boolean) {
                    gate.await(); throw AuthFailure(AuthProblem.CREDENTIALS,401,"EMAIL_NOT_VERIFIED")
                }
                override suspend fun refresh() {}
                override fun logoutLocally(): suspend ()->LogoutResult = error("unused")
            }
            val vm=AuthViewModel(repo);runCurrent();vm.login("test@example.invalid","private",false);runCurrent()
            vm.clearVerificationContext();gate.complete(Unit);runCurrent()
            assertFalse(vm.uiState.value.unverified)
            vm.continueVerification { _,_ -> fail("Abandoned credential was retained") }
        } finally { Dispatchers.resetMain() }
    }
}
