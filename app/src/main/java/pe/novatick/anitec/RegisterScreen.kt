package pe.novatick.anitec

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import pe.novatick.anitec.features.auth.presentation.*
import pe.novatick.anitec.features.auth.domain.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.novatick.anitec.ui.components.RegisterHeaderShape
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.NeutralSlate
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreen
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent

enum class UserRole {
    GANADERO, VETERINARIO
}

@Composable
fun RegisterScreen(
    onSubmit: (RegistrationForm) -> Unit,
    state: RegistrationUiState,
    onNavigateToLogin: () -> Unit
) {
    // Estados de los campos
    var names by remember { mutableStateOf("") }
    var lastNames by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var selectedRole by remember { mutableStateOf(UserRole.GANADERO) }
    var acceptTerms by remember { mutableStateOf(false) }

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
        // 1. ENCABEZADO CON CURVA E ILUSTRACIÓN DE LA LLAVE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            // Fondo Verde Curvo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RegisterHeaderShape)
                    .background(PrimaryGreenAccent)
            ) {
                Text(
                    text = stringResource(R.string.reg_title),
                    color = Color.White,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    lineHeight = 42.sp,
                    modifier = Modifier
                        .statusBarsPadding()
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 62.dp)
                )
            }

            // Ilustración de la mano sosteniendo la llave
            Image(
                painter = painterResource(id = R.drawable.register_hands_give),
                contentDescription = "Mano sosteniendo llave",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .height(260.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp)
                    .statusBarsPadding()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. FORMULARIO DE REGISTRO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Campo Nombres
            CustomTextFieldLabel(stringResource(R.string.reg_names))
            CustomTextField(
                value = names,
                onValueChange = { names = it },
                placeholder = "Juan Carlos",
                enabled = !state.busy,
                backgroundColor = fieldBackgroundColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Apellidos
            CustomTextFieldLabel(stringResource(R.string.reg_last_names))
            CustomTextField(
                value = lastNames,
                onValueChange = { lastNames = it },
                placeholder = "García Mendoza",
                enabled = !state.busy,
                backgroundColor = fieldBackgroundColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Correo electrónico
            CustomTextFieldLabel(stringResource(R.string.reg_email))
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "tucorreo@gmail.com",
                keyboardType = KeyboardType.Email,
                enabled = !state.busy,
                backgroundColor = fieldBackgroundColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Contraseña
            CustomTextFieldLabel(stringResource(R.string.reg_password))
            CustomTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "*************",
                isPassword = true,
                isPasswordVisible = isPasswordVisible,
                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                enabled = !state.busy,
                backgroundColor = fieldBackgroundColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Confirmar contraseña
            CustomTextFieldLabel(stringResource(R.string.reg_confirmation))
            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "*************",
                isPassword = true,
                isPasswordVisible = isConfirmPasswordVisible,
                onTogglePasswordVisibility = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                enabled = !state.busy,
                backgroundColor = fieldBackgroundColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. SELECTOR ANIMADO: GANADERO vs VETERINARIO
            AnimatedRoleSelector(
                selectedRole = selectedRole,
                onRoleSelected = { if(!state.busy) selectedRole = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox: Términos y condiciones
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled=!state.busy) { acceptTerms = !acceptTerms }
            ) {
                Checkbox(
                    checked = acceptTerms,
                    onCheckedChange = { if(!state.busy) acceptTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryGreenAccent,
                        uncheckedColor = NeutralSlate,
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = stringResource(R.string.reg_terms),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NeutralDark
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            RegistrationFeedback(state)

            // Botón stringResource(R.string.reg_create)
            Button(
                onClick = {
                    val form=RegistrationForm(names,lastNames,email,password,confirmPassword,Role.valueOf(selectedRole.name),acceptTerms)
                    onSubmit(form)
                    if(runCatching { RegistrationValidation.validate(form) }.isSuccess) { password=""; confirmPassword="" }
                },
                enabled = !state.busy,
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
                    text = stringResource(R.string.reg_create),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enlace "¿Ya tienes una cuenta? Inicia sesión"
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PrimaryGreenAccent, fontFamily = Poppins, fontSize = 13.sp)) {
                        append(stringResource(R.string.reg_login_prompt))
                    }
                    withStyle(style = SpanStyle(color = PrimaryGreen, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp)) {
                        append(stringResource(R.string.reg_login))
                    }
                }

                Text(
                    text = annotatedString,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// COMPONENTE: Label superior de los campos
@Composable
private fun CustomTextFieldLabel(text: String) {
    Text(
        text = text,
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = NeutralDark,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

// COMPONENTE: Campo de Texto Reutilizable
@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
    enabled: Boolean=true,
    backgroundColor: Color
) {
    TextField(
        value = value,
        enabled = enabled,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = Poppins,
                color = NeutralSlate,
                fontSize = 14.sp
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                val eyeIcon = if (isPasswordVisible) R.drawable.ic_eye_on else R.drawable.ic_eye_off
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        painter = painterResource(id = eyeIcon),
                        contentDescription = "Mostrar u ocultar contraseña",
                        tint = NeutralSlate,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else null,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    )
}

// COMPONENTE: Selector Animado de Segmentos (Ganadero / Veterinario)
@Composable
private fun AnimatedRoleSelector(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(Color(0xFFF3F5F8), RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        val itemWidth = maxWidth / 2

        // Posición x animada para el cuadro verde que se desliza
        val animatedOffset by animateDpAsState(
            targetValue = if (selectedRole == UserRole.GANADERO) 0.dp else itemWidth,
            animationSpec = tween(durationMillis = 280),
            label = "RoleSelectorAnimation"
        )

        // Cuadro Verde Deslizante
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(itemWidth)
                .fillMaxHeight()
                .background(PrimaryGreenAccent, RoundedCornerShape(12.dp))
        )

        // Opciones del Selector
        Row(modifier = Modifier.fillMaxSize()) {
            // Opción Ganadero
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onRoleSelected(UserRole.GANADERO) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.reg_rancher),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (selectedRole == UserRole.GANADERO) Color.White else NeutralSlate
                )
            }

            // Opción Veterinario
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onRoleSelected(UserRole.VETERINARIO) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.reg_vet),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (selectedRole == UserRole.VETERINARIO) Color.White else NeutralSlate
                )
            }
        }
    }
}