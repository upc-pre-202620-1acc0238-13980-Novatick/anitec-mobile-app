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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.res.stringResource
import pe.novatick.anitec.features.auth.domain.AuthProblem
import pe.novatick.anitec.features.auth.presentation.Notice
import pe.novatick.anitec.features.auth.presentation.AuthMessage
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
    onNavigateToRegister: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    onLogin: (String,String,Boolean) -> Unit,
    busy: Boolean,
    problem: AuthProblem?,
    notice: Notice?,
    onContinueVerification: () -> Unit = {}, unverified: Boolean=false,
    onRetryRestore: () -> Unit,
    onRetryLogout: () -> Unit, logoutBlocked: Boolean, retryLogout: Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    val errorMessage = problem?.let { stringResource(when(it) {
        AuthProblem.CREDENTIALS -> R.string.auth_credentials
        AuthProblem.UNAUTHORIZED, AuthProblem.RELOGIN -> R.string.auth_relogin
        AuthProblem.FORBIDDEN -> R.string.auth_forbidden
        AuthProblem.NETWORK -> R.string.auth_network
        AuthProblem.STORAGE -> R.string.auth_storage
        AuthProblem.INVALID_RESPONSE -> R.string.auth_invalid_response
        AuthProblem.SERVER -> R.string.auth_server
    }) }
    val signupPrompt=stringResource(R.string.auth_signup_prompt)
    val signupAction=stringResource(R.string.auth_signup)
    val noticeText = notice?.takeUnless { it==Notice.REMOTE_LOGOUT_OK }?.let { stringResource(when(it) {
        Notice.DEMO -> R.string.auth_demo_register
        Notice.RECOVERY -> R.string.auth_recovery
        Notice.REMOTE_LOGOUT_OK -> R.string.auth_logout_ok
        Notice.REMOTE_LOGOUT_FAILED -> R.string.auth_logout_failed
        Notice.LOGOUT_INCOMPLETE -> R.string.auth_logout_incomplete
        Notice.REMOTE_ONLY -> R.string.auth_logout_remote_only
        Notice.VERIFIED -> R.string.reg_verified
    }) }

    val fieldBackgroundColor = Color(0xFFF3F5F8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
            .imePadding()
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
                    text = stringResource(R.string.auth_title),
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
                contentDescription = stringResource(R.string.auth_illustration),
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
                text = stringResource(R.string.auth_email),
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = NeutralDark,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            TextField(
                value = email,
                onValueChange = { email = it },
                enabled = !busy,
                placeholder = {
                    Text(
                        text = stringResource(R.string.auth_email_hint),
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
                text = stringResource(R.string.auth_password),
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = NeutralDark,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                enabled = !busy,
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
                            contentDescription = stringResource(R.string.auth_toggle_password),
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
                    text = stringResource(R.string.auth_forgot),
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
                    text = stringResource(R.string.auth_remember),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = NeutralDark
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Botón Acceder
            Button(
                onClick = { onLogin(email,password,rememberMe); password = "" },
                enabled = !busy && !logoutBlocked && email.isNotBlank() && password.isNotBlank(),
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
                    text = stringResource(if(busy) R.string.auth_loading else R.string.auth_access),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            if(unverified) androidx.compose.material3.TextButton(onClick=onContinueVerification,enabled=!busy) {
                Text(stringResource(R.string.reg_continue))
            }
            if(retryLogout) androidx.compose.material3.TextButton(onClick=onRetryLogout,enabled=!busy) {
                Text(stringResource(R.string.auth_retry_logout))
            }
            noticeText?.let { AuthMessage(it) }
            if(problem == AuthProblem.NETWORK) {
                androidx.compose.material3.TextButton(onClick=onRetryRestore,enabled=!busy) {
                    Text(stringResource(R.string.auth_restore))
                }
            }
            // Mensaje de error opcional si las credenciales fallan:
            errorMessage?.let { AuthMessage(it,isError=true) }

            Spacer(modifier = Modifier.height(16.dp))

            // Enlace "¿No tienes una cuenta? Regístrate"
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PrimaryGreenAccent, fontFamily = Poppins, fontSize = 13.sp)) {
                        append(signupPrompt)
                    }
                    withStyle(style = SpanStyle(color = PrimaryGreen, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp)) {
                        append(signupAction)
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
