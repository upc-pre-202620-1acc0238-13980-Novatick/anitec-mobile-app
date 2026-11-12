package pe.novatick.anitec.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import pe.novatick.anitec.AniTecAppNavigation
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.auth.presentation.AuthViewModel
import pe.novatick.anitec.ui.theme.AniTecTheme

/** Navigation regression only: session repository is a controlled double, not backend E2E. */
class LogoutNavigationTest {
    @get:Rule val compose = createComposeRule()
    private fun verify(role: Role, restored: Boolean) {
        val person = Profile("11111111-1111-4111-8111-111111111111", "test@example.invalid", "Test", "User", role)
        val repository = object : AuthRepository {
            override val profile = MutableStateFlow<Profile?>(null)
            override suspend fun restore() { if (restored) profile.value = person }
            override suspend fun login(email: String, password: String, remember: Boolean) { profile.value = person }
            override suspend fun refresh() {}
            override fun logoutLocally(): suspend () -> LogoutResult { profile.value = null; return { LogoutResult(pe.novatick.anitec.core.session.InvalidationResult(pe.novatick.anitec.core.session.Absence.VERIFIED,pe.novatick.anitec.core.session.Absence.VERIFIED),true) } }
        }
        val vm = AuthViewModel(repository)
        compose.setContent { AniTecTheme { AniTecAppNavigation(vm,linking=pe.novatick.anitec.debug.DebugLinkingRepository()) } }
        if (!restored) {
            // Exercise normal unauthenticated startup before simulating a successful login result.
            compose.waitUntil(8000) { compose.onAllNodesWithText("ANITEC").fetchSemanticsNodes().isNotEmpty() }
            compose.waitUntil(8000) { compose.onAllNodesWithText("Iniciar Sesión").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Iniciar Sesión").performScrollTo().performClick()
            compose.onNodeWithText("Acceder").assertExists()
            runBlocking { repository.login("unused", "unused", true) }
        }
        val profileTab="Mi cuenta"
        compose.waitUntil(8000) { compose.onAllNodesWithText(profileTab).fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodesWithText(profileTab).onLast().performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Cerrar sesión"))
        compose.onNodeWithText("Cerrar sesión").performClick()
        compose.waitUntil(8000) { compose.onAllNodesWithText("Acceder").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Acceder").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Sesión cerrada y revocada en el servidor.").assertDoesNotExist()
        compose.onNodeWithText("Gestiona el bienestar de\ntus animales").assertDoesNotExist()
        compose.onNodeWithText("Ganado").assertDoesNotExist()
        compose.onNodeWithText("Agenda").assertDoesNotExist()
    }
    @Test fun restoredRancherLogoutGoesDirectlyToLogin() = verify(Role.GANADERO, true)
    @Test fun restoredVeterinarianLogoutGoesDirectlyToLogin() = verify(Role.VETERINARIO, true)
    @Test fun freshRancherLogoutGoesDirectlyToLogin() = verify(Role.GANADERO, false)
    @Test fun freshVeterinarianLogoutGoesDirectlyToLogin() = verify(Role.VETERINARIO, false)
}
