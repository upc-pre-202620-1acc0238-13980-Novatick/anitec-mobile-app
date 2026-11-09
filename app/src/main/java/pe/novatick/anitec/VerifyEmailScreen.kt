package pe.novatick.anitec

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import pe.novatick.anitec.features.auth.presentation.*
import androidx.compose.material3.TextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.delay
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.components.VerifyHeaderShape
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.NeutralSlate
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreen
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent

@Composable
fun VerifyEmailScreen(
    state: RegistrationUiState,
    onVerify: (String) -> Unit,
    onChangeEmailClick: () -> Unit,
    onResend: (String?) -> Unit,
    onLogin: () -> Unit
) {
    var otpCode by remember(state.codeVersion,state.email) { mutableStateOf("") }
    var resendPassword by remember(state.email) { mutableStateOf("") }
    var clock by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.waitUntil,state.verifyWaitUntil) {
        while(maxOf(state.waitUntil ?: 0,state.verifyWaitUntil ?: 0)>System.currentTimeMillis()) { clock=System.currentTimeMillis(); delay(1000) }
        clock=System.currentTimeMillis()
    }
    val waiting=(state.waitUntil ?: 0)>clock
    val verifyWaiting=(state.verifyWaitUntil ?: 0)>clock

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. ENCABEZADO CURVO VERDE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(VerifyHeaderShape)
                .background(PrimaryGreenAccent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.reg_verify_title),
                color = Color.White,
                fontFamily = Poppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp,
                lineHeight = 42.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(bottom = 12.dp)
            )
        }

        // 2. CONTENIDO PRINCIPAL (Texto, Código OTP y Botones)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.reg_code_prompt),
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = NeutralSlate,
                textAlign = TextAlign.Center
            )

            Text(
                text = state.email,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = PrimaryGreenAccent,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo Input de 6 dígitos
            OtpInputField(
                otpText = otpCode,
                onOtpTextChange = { if (it.length <= 6) otpCode = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Texto de expiración
            Text(stringResource(R.string.reg_expiry),textAlign=TextAlign.Center)
            RegistrationFeedback(state)
            if(verifyWaiting) Text(stringResource(R.string.reg_wait))
            if(waiting) Text(stringResource(R.string.reg_wait_seconds,(((state.waitUntil ?: 0)-clock+999)/1000).coerceAtLeast(0)))

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Verificar correo
            Button(
                onClick = { onVerify(otpCode) },
                enabled = !state.busy && !verifyWaiting && otpCode.length==6,
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
                    text = stringResource(R.string.reg_verify),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(value=resendPassword,onValueChange={resendPassword=it},enabled=!state.busy,
                label={ Text(stringResource(R.string.reg_resend_password)) },singleLine=true,
                visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
            // Enlace Reenviar Código
            val resendString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = NeutralSlate, fontFamily = Poppins, fontSize = 13.sp)) {
                    append(stringResource(R.string.reg_no_code))
                }
                withStyle(style = SpanStyle(color = PrimaryGreenAccent, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp)) {
                    append(stringResource(R.string.reg_resend))
                }
            }
            Text(
                text = resendString,
                modifier = Modifier.clickable(enabled=!state.busy && !waiting) { onResend(resendPassword.takeIf { it.isNotEmpty() }); resendPassword="" },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.TextButton(onClick=onLogin) { Text(stringResource(R.string.reg_login)) }
            // Enlace Cambiar correo
            Text(
                text = stringResource(R.string.reg_change),
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PrimaryGreenAccent,
                modifier = Modifier.clickable { onChangeEmailClick() },
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. ILUSTRACIÓN DE LA MANO CON EL PLANETA
        Image(
            painter = painterResource(id = R.drawable.verify_hands_show),
            contentDescription = "Mano sosteniendo el planeta Tierra",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(320.dp)
        )
    }
}

// COMPONENTE: 6 Casillas de OTP con manejo de focos y retroceso
@Composable
fun OtpInputField(
    otpText: String,
    onOtpTextChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = otpText,
        onValueChange = { newValue ->
            if (newValue.length <= 6 && newValue.all { it in '0'..'9' }) {
                onOtpTextChange(newValue)
            }
        },
        modifier=Modifier.testTag("verification_otp").focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = { inner ->
            Box {
            Box(Modifier.size(1.dp).alpha(0f)) { inner() }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in 0 until 6) {
                    val char = otpText.getOrNull(index)?.toString() ?: ""
                    val isFocused = otpText.length == index || (index == 5 && otpText.length == 6)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(82.dp)
                            .clickable { focusRequester.requestFocus() }
                            .border(
                                width = 1.5.dp,
                                color = if (isFocused) PrimaryGreenAccent else Color(0xFF62D28E),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = NeutralDark,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            }
        }
    )
}