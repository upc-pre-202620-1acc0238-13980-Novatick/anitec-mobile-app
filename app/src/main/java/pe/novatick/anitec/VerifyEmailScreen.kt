package pe.novatick.anitec

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.fillMaxHeight
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
    onLogin: () -> Unit,
    passwordDraft: () -> String = { "" },
    onPasswordChange: (String) -> Unit = {},
    onDismissPassword: () -> Unit = {}
) {
    var otpCode by remember(state.codeVersion,state.email) { mutableStateOf("") }
    var clock by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.waitUntil,state.verifyWaitUntil) {
        while(maxOf(state.waitUntil ?: 0,state.verifyWaitUntil ?: 0)>System.currentTimeMillis()) { clock=System.currentTimeMillis(); delay(1000) }
        clock=System.currentTimeMillis()
    }
    val secondaryColor=NeutralDark.copy(alpha=0.72f)
    val actionColor=androidx.compose.ui.graphics.lerp(PrimaryGreen,NeutralDark,0.3f)
    val waiting=(state.waitUntil ?: 0)>clock
    val verifyWaiting=(state.verifyWaitUntil ?: 0)>clock

    if(state.passwordPrompt) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest=onDismissPassword,
            title={ Text(stringResource(R.string.reg_resend_dialog_title),fontFamily=Poppins,fontSize=18.sp,fontWeight=FontWeight.SemiBold) },
            text={ Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.reg_resend_dialog_copy),fontFamily=Poppins,fontSize=14.sp,color=secondaryColor)
                TextField(value=passwordDraft(),onValueChange=onPasswordChange,singleLine=true,
                    label={ Text(stringResource(R.string.reg_password),fontFamily=Poppins) },
                    visualTransformation=PasswordVisualTransformation(),
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),
                    textStyle=androidx.compose.ui.text.TextStyle(fontFamily=Poppins,fontSize=14.sp),
                    modifier=Modifier.testTag("resend_password_dialog"))
                if(state.issue==pe.novatick.anitec.features.auth.domain.RegistrationIssue.CONTEXT)
                    AuthMessage(stringResource(R.string.reg_context_error),isError=true)
            } },
            confirmButton={ androidx.compose.material3.TextButton(colors=ButtonDefaults.textButtonColors(contentColor=actionColor),onClick={ onResend(passwordDraft()) },enabled=passwordDraft().isNotEmpty() && !state.busy) {
                Text(stringResource(R.string.reg_resend),fontFamily=Poppins) } },
            dismissButton={ androidx.compose.material3.TextButton(colors=ButtonDefaults.textButtonColors(contentColor=actionColor),onClick=onDismissPassword) { Text(stringResource(R.string.reg_cancel),fontFamily=Poppins) } }
        )
    }

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
                color = secondaryColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = state.email,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = actionColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo Input de 6 dígitos
            OtpInputField(
                otpText = otpCode,
                onOtpTextChange = { otpCode = it },
                enabled = !state.busy,
                isError = state.errorCode in listOf("INVALID_CODE","CODE_EXPIRED","CODE_ATTEMPTS_EXCEEDED")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Texto de expiración
            Text(stringResource(R.string.reg_expiry),fontFamily=Poppins,fontSize=12.sp,lineHeight=18.sp,color=secondaryColor,textAlign=TextAlign.Center)
            RegistrationFeedback(state,compactVerification=true,now=clock)
            if(verifyWaiting) Text(stringResource(R.string.reg_wait),fontFamily=Poppins,fontSize=12.sp,color=secondaryColor)

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

            Text(stringResource(R.string.reg_no_code),fontFamily=Poppins,fontSize=13.sp,color=secondaryColor)
            androidx.compose.material3.TextButton(
                colors=ButtonDefaults.textButtonColors(contentColor=actionColor),
                onClick={ onResend(null) },enabled=!state.busy && !waiting,
                modifier=Modifier.testTag("resend_code")
            ) { Text(if(waiting) stringResource(R.string.reg_wait_seconds,(((state.waitUntil ?: 0)-clock+999)/1000).coerceAtLeast(0))
                else stringResource(R.string.reg_resend),fontFamily=Poppins,fontWeight=FontWeight.SemiBold) }

            androidx.compose.material3.TextButton(colors=ButtonDefaults.textButtonColors(contentColor=actionColor),onClick=onLogin) { Text(stringResource(R.string.reg_login)) }
            // Enlace Cambiar correo
            Text(
                text = stringResource(R.string.reg_change),
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = actionColor,
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

// One native editor owns gestures, selection, cursor and clipboard. Boxes are background only.
@Composable
fun OtpInputField(
    otpText: String,
    enabled: Boolean = true,
    isError: Boolean = false,
    onOtpTextChange: (String) -> Unit
) {
    var editor by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(otpText, androidx.compose.ui.text.TextRange(otpText.length))) }
    var focused by remember { mutableStateOf(false) }
    var caretVisible by remember { mutableStateOf(true) }
    LaunchedEffect(focused,enabled,editor.selection,editor.text) {
        caretVisible=true
        if(focused && enabled && editor.selection.collapsed) {
            while(true) { delay(500); caretVisible=!caretVisible }
        }
    }
    LaunchedEffect(otpText) {
        if(editor.text!=otpText) editor=androidx.compose.ui.text.input.TextFieldValue(otpText,androidx.compose.ui.text.TextRange(otpText.length))
    }
    val inputLabel=stringResource(R.string.reg_otp_label)
    val density=androidx.compose.ui.platform.LocalDensity.current
    val measurer=androidx.compose.ui.text.rememberTextMeasurer()
    val style=androidx.compose.ui.text.TextStyle(fontFamily=Poppins,fontWeight=FontWeight.Medium,fontSize=24.sp,
        fontFeatureSettings="tnum",color=NeutralDark)
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gap=8.dp
        val cellWidth=maxWidth/6
        val cellHeight=maxOf(64.dp,with(density) { 24.sp.toDp() }+24.dp)
        val visualTransformation=androidx.compose.ui.text.input.VisualTransformation { text ->
            val distributed=androidx.compose.ui.text.buildAnnotatedString {
                text.forEach { digit ->
                    val glyphPixels=measurer.measure(digit.toString(),style).getLineRight(0)
                    val spacing=with(density) { (cellWidth.toPx()-glyphPixels).toSp() }
                    pushStyle(androidx.compose.ui.text.SpanStyle(letterSpacing=spacing))
                    append(digit)
                    pop()
                }
            }
            androidx.compose.ui.text.input.TransformedText(distributed,androidx.compose.ui.text.input.OffsetMapping.Identity)
        }
        BasicTextField(
            value=editor,
            onValueChange={ value ->
                if(value.text.length<=6 && value.text.all { it in '0'..'9' }) {
                    editor=value
                    onOtpTextChange(value.text)
                }
            },
            enabled=enabled,singleLine=true,
            textStyle=style,
            visualTransformation=visualTransformation,
            // The native caret falls on the boundary between advance lanes. Render its
            // visual at the actual insertion slot; the editor still owns selection and gestures.
            cursorBrush=androidx.compose.ui.graphics.SolidColor(Color.Transparent),
            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),
            modifier=Modifier.fillMaxWidth().testTag("verification_otp")
                .onFocusChanged { focused=it.isFocused }
                .semantics { contentDescription=inputLabel },
            decorationBox={ innerTextField ->
                Box(Modifier.fillMaxWidth().height(cellHeight),contentAlignment=Alignment.CenterStart) {
                    Row(Modifier.fillMaxSize()) {
                        repeat(6) { index ->
                            val active=focused && editor.selection.start.coerceAtMost(5)==index
                            val color=when { isError -> androidx.compose.material3.MaterialTheme.colorScheme.error
                                active || otpText.length==6 -> PrimaryGreen
                                else -> pe.novatick.anitec.ui.theme.NeutralGrayDark }
                            Box(Modifier.weight(1f).fillMaxHeight().padding(horizontal=gap/2).testTag("otp_cell_$index")
                                .background(if(active) PrimaryGreenAccent.copy(alpha=0.05f) else Color.White,RoundedCornerShape(12.dp))
                                .border(if(active || isError) 1.5.dp else 1.dp,color,RoundedCornerShape(12.dp)))
                        }
                    }
                    // Full width native text layout, not an invisible/off-screen input.
                    Box(Modifier.fillMaxWidth()) { innerTextField() }
                    if(focused && enabled && editor.selection.collapsed) {
                        val insertion=editor.selection.start.coerceIn(0,editor.text.length)
                        val lane=insertion.coerceAtMost(5)
                        val glyph=editor.text.getOrNull(lane)
                        val glyphWidth=if(glyph==null) 0.dp else with(density) {
                            measurer.measure(glyph.toString(),style).getLineRight(0).toDp()
                        }
                        val center=cellWidth*(lane+0.5f)
                        val caretX=center+if(insertion==6) glyphWidth/2 else -glyphWidth/2
                        Box(Modifier.align(Alignment.CenterStart).offset(x=caretX-1.dp)
                            .width(2.dp).height(with(density) { 24.sp.toDp() })
                            .testTag("otp_cursor").alpha(if(caretVisible) 1f else 0f).background(PrimaryGreen))
                    }
                }
            }
        )
    }
}
