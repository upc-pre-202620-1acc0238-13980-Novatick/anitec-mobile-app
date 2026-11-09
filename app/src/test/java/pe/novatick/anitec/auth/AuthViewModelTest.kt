package pe.novatick.anitec.auth

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.presentation.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @Test fun duplicateLoginIsSuppressedAndLateCompletionCannotReplaceLogoutNotice()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val gate=CompletableDeferred<Unit>();var calls=0
            val repository=object: AuthRepository {
                override val profile=MutableStateFlow<Profile?>(null)
                override suspend fun restore() {}
                override suspend fun refresh() {}
                override suspend fun login(email: String,password: String,remember: Boolean) { calls++;gate.await() }
                override fun logoutLocally(): suspend ()->LogoutResult = { LogoutResult(pe.novatick.anitec.core.session.InvalidationResult(pe.novatick.anitec.core.session.Absence.UNKNOWN,pe.novatick.anitec.core.session.Absence.VERIFIED),false) }
            }
            val vm=AuthViewModel(repository);runCurrent()
            vm.login("x","x",false);vm.login("x","x",false);runCurrent();assertEquals(1,calls)
            vm.logout();runCurrent();gate.complete(Unit);runCurrent()
            assertEquals(Notice.REMOTE_LOGOUT_FAILED,vm.uiState.value.notice);assertFalse(vm.uiState.value.busy)
        } finally { Dispatchers.resetMain() }
    }
}
