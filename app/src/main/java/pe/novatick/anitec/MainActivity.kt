package pe.novatick.anitec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.*
import pe.novatick.anitec.core.di.AuthContainer
import pe.novatick.anitec.features.auth.domain.Role
import pe.novatick.anitec.features.auth.presentation.*
import pe.novatick.anitec.ui.screens.home.RancherMainContainer
import pe.novatick.anitec.ui.screens.veterinarian.VeterinarianMainContainer
import pe.novatick.anitec.ui.theme.AniTecTheme

class MainActivity : ComponentActivity() {
    private lateinit var authModel: AuthViewModel
    override fun onStart() {
        super.onStart()
        if (::authModel.isInitialized) authModel.restore()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm=ViewModelProvider(this,object: ViewModelProvider.Factory {
            override fun <T: ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(AuthContainer(applicationContext).repository) as T
            }
        })[AuthViewModel::class.java]
        authModel=vm
        enableEdgeToEdge()
        setContent { AniTecTheme { AniTecAppNavigation(vm) } }
    }
}

@Composable
fun AniTecAppNavigation(vm: AuthViewModel) {
    val profile by vm.profile.collectAsState()
    val state by vm.uiState.collectAsState()
    var firstPublicEntry by rememberSaveable { mutableStateOf(true) }
    val onLogout: () -> Unit = {
        // Restored identities may never have visited the public splash in this process.
        // Select login before clearing identity, so the newly composed public graph is correct.
        firstPublicEntry = false
        vm.logout()
    }
    // Separate composition and back stack for authenticated identities. Logout disposes all private routes.
    key(profile?.id,profile?.role) {
        if(profile != null) {
            Column(Modifier.fillMaxSize()) {
                Text(stringResource(R.string.auth_demo),modifier=Modifier.statusBarsPadding())
                Box(Modifier.weight(1f)) {
                    when(profile!!.role) {
                        Role.GANADERO -> RancherMainContainer(onNavigateToLogin=onLogout)
                        Role.VETERINARIO -> VeterinarianMainContainer(onNavigateToLogin=onLogout)
                    }
                }
            }
        } else if(state.restoring) {
            Box(Modifier.fillMaxSize(),contentAlignment=androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
        } else {
            val nav=rememberNavController()
            val publicStart=remember { if(firstPublicEntry) "splash" else "login" }
            NavHost(navController=nav,startDestination=publicStart) {
                composable("splash") {
                    SplashScreen(onTimeout={ firstPublicEntry=false; nav.navigate("welcome") { popUpTo("splash") { inclusive=true } } })
                }
                composable("welcome") {
                    WelcomeScreen(onNavigateToLogin={ nav.navigate("login") },onNavigateToRegister={ nav.navigate("register") })
                }
                composable("login") {
                    LoginScreen(onNavigateToRegister={ nav.navigate("register") },onForgotPasswordClick={ vm.notice(Notice.RECOVERY) },
                        onLogin=vm::login,busy=state.busy,problem=state.problem,notice=state.notice,onRetryRestore=vm::restore,onRetryLogout=vm::logout,logoutBlocked=state.logoutBlocked,retryLogout=state.retryLogout)
                }
                // Existing registration remains a demo and cannot create an authenticated identity.
                composable("register") {
                    RegisterScreen(onNavigateToVerifyEmail={ nav.navigate("verify_email") },onNavigateToLogin={ nav.popBackStack() })
                }
                composable("verify_email") {
                    VerifyEmailScreen(onVerifySuccess={ vm.notice(Notice.DEMO); nav.navigate("login") { popUpTo("login") { inclusive=true } } },
                        onChangeEmailClick={ nav.popBackStack() })
                }
            }
        }
    }
}
