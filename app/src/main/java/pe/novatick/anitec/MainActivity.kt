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
        if (::authModel.isInitialized && pe.novatick.anitec.core.environment.VariantAccess.isReady()) authModel.restore()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container=AuthContainer(applicationContext)
        val provider=ViewModelProvider(this,object: ViewModelProvider.Factory {
            override fun <T: ViewModel> create(modelClass: Class<T>): T {
                return requireNotNull(modelClass.cast(if(modelClass==RegistrationViewModel::class.java) RegistrationViewModel(container.registration)
                    else AuthViewModel(container.repository)))
            }
        })
        enableEdgeToEdge()
        setContent { AniTecTheme {
            pe.novatick.anitec.core.environment.VariantAccess.Gate(refresh={container.repository.refresh()}) {
                val vm=remember {provider[AuthViewModel::class.java]}
                val registration=remember {provider[RegistrationViewModel::class.java]}
                SideEffect {authModel=vm}
                LaunchedEffect(Unit) {vm.restore()}
                AniTecAppNavigation(vm,registration,container.livestock,container.linking)
            }
        } }
    }
}

@Composable
fun AniTecAppNavigation(vm: AuthViewModel,registration: RegistrationViewModel?=null,livestock: pe.novatick.anitec.features.livestock.domain.LivestockRepository?=null,linking: pe.novatick.anitec.features.linking.domain.LinkingRepository?=null) {
    val profile by vm.profile.collectAsState()
    val state by vm.uiState.collectAsState()
    LaunchedEffect(profile?.id) { if(profile!=null) registration?.abandon() }
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
                if(BuildConfig.DEBUG && BuildConfig.API_ENVIRONMENT=="LAB") Text(stringResource(if(BuildConfig.API_ENVIRONMENT=="LAB") R.string.alpha_lab else R.string.alpha_staging),modifier=Modifier.statusBarsPadding())
                Box(Modifier.weight(1f)) {
                    when(profile!!.role) {
                        Role.GANADERO -> RancherMainContainer(onNavigateToLogin=onLogout,livestock=livestock,profile=profile,linking=linking)
                        Role.VETERINARIO -> if(linking!=null) pe.novatick.anitec.features.linking.presentation.VeterinaryLinkingShell(linking,profile!!,onLogout)
                    }
                }
            }
        } else if(state.restoring) {
            Box(Modifier.fillMaxSize(),contentAlignment=androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
        } else {
            val nav=rememberNavController()
            val regState=registration?.uiState?.collectAsState()?.value
            val language=if(androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language=="en") "en" else "es"
            val backEntry by nav.currentBackStackEntryAsState()
            LaunchedEffect(backEntry?.destination?.route) {
                val route=backEntry?.destination?.route
                if(route!=null && route!="login") vm.clearVerificationContext()
                if(route!=null && route !in listOf("register","verify_email") && regState?.step!=RegistrationStep.COMPLETE) registration?.abandon()
            }
            LaunchedEffect(regState?.step) {
                when(regState?.step) {
                    RegistrationStep.VERIFY -> if(nav.currentDestination?.route!="verify_email") nav.navigate("verify_email")
                    RegistrationStep.COMPLETE -> { registration?.abandon(); vm.notice(Notice.VERIFIED); nav.navigate("login") { popUpTo(nav.graph.id) { inclusive=false } } }
                    else -> Unit
                }
            }
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
                        onContinueVerification={ vm.continueVerification { email,password -> registration?.continueVerification(email,password) } },unverified=state.unverified,
                        onLogin=vm::login,busy=state.busy,problem=state.problem,notice=state.notice,onRetryRestore=vm::restore,onRetryLogout=vm::logout,logoutBlocked=state.logoutBlocked,retryLogout=state.retryLogout)
                }
                // Public registration never creates an authenticated identity.
                composable("register") {
                    if(registration!=null && regState!=null) {
                        androidx.activity.compose.BackHandler { registration.abandon(); if(!nav.popBackStack()) nav.navigate("login") { popUpTo(nav.graph.id) { inclusive=false } } }
                        RegisterScreen(onSubmit={ registration.register(it,language) },state=regState,
                            onNavigateToLogin={ registration.abandon(); nav.navigate("login") { popUpTo(nav.graph.id) { inclusive=false } } })
                    }
                }
                composable("verify_email") {
                    if(registration!=null && regState!=null && regState.step==RegistrationStep.VERIFY) {
                        androidx.activity.compose.BackHandler { registration.abandon(); nav.navigate("login") { popUpTo(nav.graph.id) { inclusive=false } } }
                        VerifyEmailScreen(state=regState,onVerify=registration::verify,
                            onResend={ registration.resend(it,language) },
                            passwordDraft=registration::passwordDraftForEntry,onPasswordChange=registration::updatePasswordDraft,
                            onDismissPassword=registration::dismissPasswordPrompt,
                            onChangeEmailClick={ registration.abandon(true); nav.navigate("register") { popUpTo(nav.graph.id) { inclusive=false } } },
                            onLogin={ registration.abandon(); nav.navigate("login") { popUpTo(nav.graph.id) { inclusive=false } } })
                    }
                }
            }
        }
    }
}
