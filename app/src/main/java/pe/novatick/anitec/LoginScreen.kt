package pe.novatick.anitec

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.novatick.anitec.ui.components.LoginHeaderShape
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.NeutralSlate
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreen
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent

@Composable
fun LoginScreen(
    onNavigateToHome: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPasswordClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun performLogin() {
        val cleanEmail = email.trim()

        when {
            // Caso Ganadero
            cleanEmail.equals("ganadero123@gmail.com", ignoreCase = true) && password == "ganadero123" -> {
                errorMessage = null
                onNavigateToHome("home_rancher")
            }
            // Caso Veterinario
            cleanEmail.equals("veterinario123@gmail.com", ignoreCase = true) && password == "veterinario123" -> {
                errorMessage = null
                onNavigateToHome("home_veterinarian")
            }
            // Credenciales Incorrectas
            else -> {
                errorMessage = "Correo o contraseña incorrectos"
            }
        }
    }

    val fieldBackgroundColor = Color(0xFFF3F5F8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. HEADER CONTENEDOR (Permite la superposición de la imagen sobre la curva)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp) // Altura suficiente para que el brazo sobresalga abajo a la derecha
        ) {
            // A. Fondo Verde Recortado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .clip(LoginHeaderShape)
                    .background(PrimaryGreenAccent)
            ) {
                Text(
                    text = "Iniciar\nSesión",
                    color = Color.White,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp,
                    lineHeight = 44.sp,
                    modifier = Modifier
                        .statusBarsPadding()
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 180.dp)
                )
            }

            // B. Imagen de la mano con el celular (Superpuesta y pegada a la derecha)
            Image(
                painter = painterResource(id = R.drawable.login_hands_phone),
                contentDescription = "Mano sosteniendo celular",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .height(520.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (15).dp)
                    .statusBarsPadding()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. FORMULARIO DE CREDENCIALES
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // Campo Email
            Text(
                text = "Email",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = NeutralDark,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = "tucorreo@gmail.com",
                        fontFamily = Poppins,
                        color = NeutralSlate,
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBackgroundColor,
                    unfocusedContainerColor = fieldBackgroundColor,
                    disabledContainerColor = fieldBackgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            Text(
                text = "Contraseña",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = NeutralDark,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = "*************",
                        fontFamily = Poppins,
                        color = NeutralSlate,
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val eyeIcon = if (isPasswordVisible) R.drawable.ic_eye_on else R.drawable.ic_eye_off
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            painter = painterResource(id = eyeIcon),
                            contentDescription = "Mostrar u ocultar contraseña",
                            tint = NeutralSlate,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBackgroundColor,
                    unfocusedContainerColor = fieldBackgroundColor,
                    disabledContainerColor = fieldBackgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            // Olvidé mi contraseña
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Olvidé mi contraseña",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = PrimaryGreen,
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Checkbox: Mantener mi sesión iniciada
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { rememberMe = !rememberMe }
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryGreenAccent,
                        uncheckedColor = NeutralSlate,
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    text = "Mantener mi sesión iniciada",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = NeutralDark
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Botón Acceder
            Button(
                onClick = { performLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreenAccent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Acceder",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            // Mensaje de error opcional si las credenciales fallan:
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enlace "¿No tienes una cuenta? Regístrate"
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PrimaryGreenAccent, fontFamily = Poppins, fontSize = 13.sp)) {
                        append("¿No tienes una cuenta? ")
                    }
                    withStyle(style = SpanStyle(color = PrimaryGreen, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp)) {
                        append("Regístrate")
                    }
                }

                Text(
                    text = annotatedString,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}