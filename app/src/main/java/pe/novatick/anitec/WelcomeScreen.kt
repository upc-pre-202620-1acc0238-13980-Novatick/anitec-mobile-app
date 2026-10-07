package pe.novatick.anitec

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.components.CurvedHeaderShape
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreen
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Encabezado Curvo Verde (Edge-to-Edge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(CurvedHeaderShape)
                .background(PrimaryGreenAccent),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "ANITEC",
                color = Color.White,
                fontFamily = Poppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 52.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 24.dp)
            )
        }

        // 2. Contenido Central (Títulos e Imagen)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título en verde oscuro para mayor legibilidad
            Text(
                text = "Gestiona el bienestar de\ntus animales",
                color = PrimaryGreen, // #10A363
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Imagen ampliada de carpetas
            Image(
                painter = painterResource(id = R.drawable.welcome_all_animal_folders),
                contentDescription = "Carpetas de gestión de animales",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(320.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtítulo
            Text(
                text = "Organiza tu ganado, vinculaciones y\natenciones veterinarias desde un\nsolo lugar.",
                color = NeutralDark,
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }

        // 3. Botones y barra decorativa inferior
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreenAccent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Iniciar Sesión",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
            }

            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryGreenAccent
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(PrimaryGreenAccent),
                    width = 1.5.dp
                )
            ) {
                Text(
                    text = "Regístrate",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Línea verde decorativa en la base
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(3.dp)
                    .background(
                        color = PrimaryGreenAccent,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}