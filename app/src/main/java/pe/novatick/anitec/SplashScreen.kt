package pe.novatick.anitec

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit // Callback para navegar a la pantalla principal/login tras el tiempo
) {
    // 1. Lista de recursos con las 5 siluetas de animales
    val animalIcons = remember {
        listOf(
            R.drawable.splash_ic_animal_cow,
            R.drawable.splash_ic_animal_sheep,
            R.drawable.splash_ic_animal_goat,
            R.drawable.splash_ic_animal_pig,
            R.drawable.splash_ic_animal_horse
        )
    }

    // 2. Selección aleatoria cada vez que entra a la pantalla
    val randomIcon = remember { animalIcons.random() }

    // 3. Configuración de la animación flotante suave (sube y baja)
    val infiniteTransition = rememberInfiniteTransition(label = "floating_icon_animation")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -12f, // Desplazamiento superior en dp
        targetValue = 12f,   // Desplazamiento inferior en dp
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800, // Duración del ciclo suave
                easing = LinearOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse // Invierte el movimiento suavemente
        ),
        label = "offsetY"
    )

    // 4. Temporizador de salida (ejemplo: 2.5 segundos)
    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
    }

    // 5. Interfaz de usuario
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Fondo verde idéntico al boceto (#57CF7B o PrimaryGreenAccent)
            .background(PrimaryGreenAccent),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono del animal con animación flotante
            Image(
                painter = painterResource(id = randomIcon),
                contentDescription = "Icono de animal AniTec",
                modifier = Modifier
                    .size(180.dp)
                    .offset(y = offsetY.dp) // Aplica la animación vertical en dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Texto ANITEC totalmente estático
            Text(
                text = "ANITEC",
                color = Color.White,
                fontFamily = Poppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp,
                letterSpacing = 2.sp
            )
        }
    }
}