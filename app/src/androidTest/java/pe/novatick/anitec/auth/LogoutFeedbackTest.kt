package pe.novatick.anitec.auth

import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import pe.novatick.anitec.LoginScreen
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.AuthProblem
import pe.novatick.anitec.features.auth.presentation.Notice
import pe.novatick.anitec.features.livestock.presentation.LivestockHomeSections
import pe.novatick.anitec.ui.theme.AniTecTheme
import java.util.Locale

/** Presentation-only regressions: no real sessions, network calls or writes. */
class LogoutFeedbackTest {
    @get:Rule val compose=createComposeRule()
    private fun check(language:String,home:Boolean=false) {
        val base=InstrumentationRegistry.getInstrumentation().targetContext
        val config=Configuration(base.resources.configuration).apply {setLocale(Locale.forLanguageTag(language))}
        val context=base.createConfigurationContext(config)
        var notice by mutableStateOf<Notice?>(Notice.REMOTE_LOGOUT_OK)
        var problem by mutableStateOf<AuthProblem?>(null)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides context,LocalResources provides context.resources,LocalConfiguration provides config) {
                AniTecTheme {
                    if(home) LivestockHomeSections() else LoginScreen(onNavigateToRegister={},onLogin={_,_,_->},
                        busy=false,problem=problem,notice=notice,onRetryRestore={},onRetryLogout={},logoutBlocked=false,retryLogout=false)
                }
            }
        }
        if(home) {
            compose.onNodeWithText(context.getString(R.string.ls_visits_neutral)).assertExists()
            compose.onNodeWithText(context.getString(R.string.ls_view_visits)).assertIsNotEnabled()
            return
        }
        compose.onNodeWithText(context.getString(R.string.auth_logout_ok)).assertDoesNotExist()
        for((value,resource) in listOf(Notice.REMOTE_LOGOUT_FAILED to R.string.auth_logout_failed,
            Notice.LOGOUT_INCOMPLETE to R.string.auth_logout_incomplete,Notice.REMOTE_ONLY to R.string.auth_logout_remote_only)) {
            compose.runOnIdle {notice=value}
            compose.onNodeWithText(context.getString(resource)).assertExists()
        }
        compose.runOnIdle {notice=null;problem=AuthProblem.STORAGE}
        compose.onNodeWithText(context.getString(R.string.auth_storage)).assertExists()
    }
    @Test fun spanishSuccessIsSilentButSecurityErrorsRemain()=check("es")
    @Test fun englishSuccessIsSilentButSecurityErrorsRemain()=check("en")
    @Test fun spanishRancherVisitsAreExplicitlyUnavailable()=check("es",true)
    @Test fun englishRancherVisitsAreExplicitlyUnavailable()=check("en",true)
}
