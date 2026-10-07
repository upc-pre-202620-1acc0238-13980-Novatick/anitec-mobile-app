package pe.novatick.anitec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.novatick.anitec.ui.theme.AniTecTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AniTecTheme {
                AniTecAppNavigation()
            }
        }
    }
}

@Composable
fun AniTecAppNavigation() {
    val navController = rememberNavController()

    // Estado global simple para mantener el rol seleccionado en el registro
    var selectedRoleByRegister by remember { mutableStateOf(UserRole.GANADERO) }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // 1. Splash Screen
        composable("splash") {
            SplashScreen(
                onTimeout = {
                    navController.navigate("welcome") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 2. Pantalla de Bienvenida (Welcome / Pre-Login)
        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // 3. Pantalla de Login
        composable("login") {
            LoginScreen(
                onNavigateToHome = { destinationRoute ->
                    // Navega a home_rancher o home_veterinarian borrando la pila
                    navController.navigate(destinationRoute) {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // 4. Pantalla de Registro
        composable("register") {
            RegisterScreen(
                onNavigateToVerifyEmail = { role ->
                    selectedRoleByRegister = role
                    navController.navigate("verify_email")
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // 5. Pantalla de Verificación de Correo
        composable("verify_email") {
            VerifyEmailScreen(
                userRole = selectedRoleByRegister,
                onVerifySuccess = { role ->
                    val destination = if (role == UserRole.GANADERO) "home_rancher" else "home_veterinarian"
                    navController.navigate(destination) {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onChangeEmailClick = {
                    navController.popBackStack()
                }
            )
        }

        // 6.1. Home para Ganadero (Contenedor con Bottom Navigation integrada)
        composable("home_rancher") {
            RancherMainContainer(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 6.2 Home para Veterinario
        composable("home_veterinarian") {
            // Reemplazar con VeterinarianMainContainer() cuando esté creado
        }
    }
}