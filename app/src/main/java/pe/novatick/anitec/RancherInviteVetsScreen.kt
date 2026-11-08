package pe.novatick.anitec.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.components.CattleHeaderShape
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.NeutralSlate
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent

@Composable
fun RancherInviteVetsScreen(
    onBackClick: () -> Unit,
    onSendSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. ENCABEZADO CURVO VERDE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(CattleHeaderShape)
                .background(PrimaryGreenAccent)
        )

        // 2. CONTENIDO PRINCIPAL SCROLLABLE
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(35.dp)) }

            // TOP BAR: BOTÓN ATRÁS + TÍTULO Y SUBTÍTULO
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Atrás (ic_arrow_left)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F8EE))
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_left),
                            contentDescription = "Regresar",
                            tint = PrimaryGreenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Invitar veterinario",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            color = PrimaryGreenAccent
                        )
                        Text(
                            text = "La vinculación se activará cuando sea aceptada",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = NeutralSlate
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // ILUSTRACIÓN CENTRAL (CORREO ROTADO + ESTETOSCOPIO REPOSICIONADO)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Círculo base verde claro
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F8EE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(120.dp)
                        ) {
                            // Tarjeta verde rotada hacia la izquierda (-12°) con ícono de correo
                            Box(
                                modifier = Modifier
                                    .size(76.dp, 66.dp)
                                    .offset(x = (-8).dp, y = (-4).dp)
                                    .graphicsLayer { rotationZ = -12f }
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(PrimaryGreenAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_mail),
                                    contentDescription = "Correo",
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            // Badge blanco superpuesto en la esquina inferior derecha con ícono de estetoscopio
                            Box(
                                modifier = Modifier
                                    .offset(x = 24.dp, y = 16.dp)
                                    .size(54.dp)
                                    .shadow(8.dp, RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_stethoscope),
                                    contentDescription = "Veterinario",
                                    tint = PrimaryGreenAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // CAMPO: CORREO ELECTRÓNICO *
            item {
                Text(
                    text = "Correo electrónico *",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            text = "veterinario@email.com",
                            fontFamily = Poppins,
                            color = NeutralSlate,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F5F8),
                        unfocusedContainerColor = Color(0xFFF3F5F8),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                )
            }

            // BANNER DE SEGURIDAD (ic_shield)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1FBF5))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shield),
                            contentDescription = "Seguridad",
                            tint = PrimaryGreenAccent,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Enviar una invitación no concede acceso. Podrás revocarlo en cualquier momento después de su aceptación.",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = NeutralDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // BOTÓN ENVIAR INVITACIÓN
            item {
                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { onSendSuccess() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Enviar invitación",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}