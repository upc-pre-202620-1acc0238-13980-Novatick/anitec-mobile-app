package pe.novatick.anitec.auth

import java.io.IOException
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import pe.novatick.anitec.core.session.SessionRecord
import pe.novatick.anitec.core.session.SessionStore
import pe.novatick.anitec.core.session.Absence
import pe.novatick.anitec.core.session.InvalidationResult
import okhttp3.ResponseBody.Companion.toResponseBody
import pe.novatick.anitec.features.auth.domain.AuthProblem
import pe.novatick.anitec.features.auth.infrastructure.*
import pe.novatick.anitec.features.auth.presentation.AuthViewModel
import retrofit2.Response

/** O-01 reproduction with synthetic storage/transport; not Android filesystem or backend E2E. */
@OptIn(ExperimentalCoroutinesApi::class)
class LogoutStorageFailureTest {
    private val account = "11111111-1111-4111-8111-111111111111"

    private inner class UndeletableStore(var keyFails: Boolean=true, var fileFails: Boolean=true) : SessionStore {
        var keyPresent = true
        val original = SessionRecord(account, "test@example.invalid", "GANADERO",
            "synthetic-access", "synthetic-refresh", 130000, true)
        var record: SessionRecord? = original
        var clearAttempts = 0
        override fun read() = if(keyPresent) record else null
        override fun write(record: SessionRecord) { this.record = record; keyPresent=true }
        override fun clear() { clearAttempts++; if(fileFails) throw IOException("Synthetic deletion failure"); record=null }
        override fun invalidate(): InvalidationResult {
            if(!keyFails) keyPresent=false
            val files = try { clear(); Absence.VERIFIED } catch (_: Exception) { Absence.FAILED }
            return InvalidationResult(if(keyPresent) Absence.FAILED else Absence.VERIFIED,files)
        }
    }

    private inner class ControlledApi(private val remoteAvailable: Boolean) : AuthApi {
        @Volatile var logoutCalls = 0
        var gate: kotlinx.coroutines.CompletableDeferred<Unit>? = null
        var revoked = false
        override suspend fun login(body: LoginRequest): Response<Envelope<LoginDto>> =
            Response.success(Envelope(true,"ok","2035-01-01T00:00:00Z",
                LoginDto(account,"test@example.invalid","GANADERO","synthetic-access","synthetic-refresh",30)))
        override suspend fun refresh(body: RefreshRequest): Response<Envelope<TokenDto>> =
            error("The synthetic access token is not expired")
        override suspend fun me(bearer: String): Response<Envelope<ProfileDto>> {
            if(revoked) return Response.error(401,"{}".toResponseBody())
            assertEquals("Bearer synthetic-access", bearer)
            return Response.success(Envelope(true, "ok", "2035-01-01T00:00:00Z",
                ProfileDto(account, "test@example.invalid", "Test", "User", "GANADERO",
                    true, "", "2035-01-01T00:00:00Z", null, null, null, null)))
        }
        override suspend fun logout(body: LogoutRequest): Response<Envelope<Unit>> {
            logoutCalls++
            gate?.await()
            assertEquals("synthetic-refresh", body.refreshToken)
            if (!remoteAvailable) throw IOException("Synthetic remote failure")
            revoked=true
            return Response.success(Envelope(true, "ok", "2035-01-01T00:00:00Z", null))
        }
    }

    // Retained reproduction cases now enforce safe behavior, including the approved total-failure bound.
    private fun characterize(remoteAvailable: Boolean) = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val store = UndeletableStore()
            val api = ControlledApi(remoteAvailable)
            val repository = AuthRepositoryImpl(api, store) { 100000 }
            val vm = AuthViewModel(repository)
            withTimeout(5000) { vm.uiState.first { !it.busy && !it.restoring } }
            assertNotNull(repository.profile.value)
            vm.logout()
            assertNull(repository.profile.value)
            withTimeout(5000) { vm.uiState.first { !it.busy } }
            assertEquals(1, store.clearAttempts)
            assertSame(store.original, store.record)
            assertEquals(1, api.logoutCalls)
            assertTrue(vm.uiState.value.logoutBlocked)
            assertTrue(vm.uiState.value.retryLogout)
            assertEquals(if(remoteAvailable) pe.novatick.anitec.features.auth.presentation.Notice.REMOTE_ONLY
                else pe.novatick.anitec.features.auth.presentation.Notice.LOGOUT_INCOMPLETE, vm.uiState.value.notice)
            vm.restore()
            withTimeout(5000) { vm.uiState.first { !it.busy && !it.restoring } }
            assertNull(repository.profile.value)
            assertTrue(runCatching { repository.login("unused","unused",true) }.isFailure)
            if(remoteAvailable) {
                val next=AuthRepositoryImpl(api,store) { 100000 }
                assertTrue(runCatching { next.restore() }.isFailure)
                assertNull(next.profile.value)
            }
            // Total failure has no cross-process guarantee. Do not assert an invented one.
            store.fileFails=false
            vm.logout()
            withTimeout(5000) { vm.uiState.first { !it.busy } }
            assertFalse(vm.uiState.value.logoutBlocked)
            assertNull(store.record)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun reproducesSkippedRevocationAndRestorationWithAvailableRemote() = characterize(true)

    @Test fun reproducesRestorationWithDeletionFailureAndUnavailableRemote() = characterize(false)

    /** Original red regression: the required remote attempt must now succeed independently. */
    @Test fun keyDeletionProtectsResidualFileWhenRemoteFails() = runBlocking {
        val store=UndeletableStore(keyFails=false)
        val api=ControlledApi(false)
        val repo=AuthRepositoryImpl(api,store) { 100000 }; repo.restore()
        val result=repo.logoutLocally()()
        assertTrue(result.local.protected); assertFalse(result.remoteConfirmed)
        assertSame(store.original,store.record); assertFalse(store.keyPresent)
        val next=AuthRepositoryImpl(api,store) { 100000 }; next.restore(); assertNull(next.profile.value)
        assertEquals(1,api.logoutCalls)
    }
    @Test fun fileDeletionProtectsSessionWhenKeyAndRemoteFail() = runBlocking {
        val store=UndeletableStore(fileFails=false)
        val api=ControlledApi(false)
        val repo=AuthRepositoryImpl(api,store) { 100000 }; repo.restore()
        val result=repo.logoutLocally()()
        assertTrue(result.local.protected); assertFalse(result.remoteConfirmed)
        assertTrue(store.keyPresent); assertNull(store.record)
        val next=AuthRepositoryImpl(api,store) { 100000 }; next.restore(); assertNull(next.profile.value)
        assertEquals(1,api.logoutCalls)
    }
    @Test fun deletionFailureMustNotPreventRemoteRevocationAttempt() = runBlocking {
        val store = UndeletableStore()
        val api = ControlledApi(true)
        val repository = AuthRepositoryImpl(api, store) { 100000 }
        repository.restore()
        val failure = runCatching { repository.logoutLocally()() }.exceptionOrNull()
        assertNull(repository.profile.value)
        assertSame(store.original, store.record)
        assertEquals("O-01: deletion failure must not skip revocation (exception=${failure?.javaClass?.simpleName})",
            1, api.logoutCalls)
    }
    @Test fun lateLogoutCannotInvalidateNewLoginGeneration() = runBlocking {
        val store=UndeletableStore(fileFails=false)
        val api=ControlledApi(true)
        val repo=AuthRepositoryImpl(api,store) { 100000 }; repo.restore()
        val revoke=repo.logoutLocally()
        val gate=kotlinx.coroutines.CompletableDeferred<Unit>(); api.gate=gate
        val job=kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).async { revoke() }
        withTimeout(5000) { while(api.logoutCalls==0) kotlinx.coroutines.delay(5) }
        repo.login("test@example.invalid","synthetic-password",true)
        val next=store.record!!
        assertNotEquals(store.original.generation,next.generation)
        gate.complete(Unit); assertTrue(job.await().remoteConfirmed)
        assertSame(next,store.record); assertNotNull(repo.profile.value)
        assertTrue(revoke().remoteConfirmed)
        assertEquals(1,api.logoutCalls)
        assertSame(next,store.record)
    }
}
