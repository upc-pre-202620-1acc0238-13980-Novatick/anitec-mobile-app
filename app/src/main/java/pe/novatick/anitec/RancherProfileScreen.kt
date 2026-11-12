package pe.novatick.anitec.ui.screens.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
fun RancherProfileScreen(
    userName: String = "Juan García",
    userEmail: String = "juan.garcia@email.com",
    userRole: String? = null,
    onLogoutClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. ENCABEZADO CURVO VERDE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(CattleHeaderShape)
                .background(PrimaryGreenAccent)
        )

        // 2. CONTENIDO PRINCIPAL SCROLLABLE
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(28.dp)) }

            // TÍTULO Y SUBTÍTULO
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.lk_profile),
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = pe.novatick.anitec.ui.theme.PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.acct_sub),
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = NeutralSlate
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // TARJETA DE PERFIL DE USUARIO
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreenAccent),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Circular con Iniciales "JG" y botón de edición
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pe.novatick.anitec.features.livestock.presentation.livestockInitials(userName, ""),
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = pe.novatick.anitec.ui.theme.PrimaryGreen
                                )
                            }

                            // Botón lapicero para editar foto
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 4.dp, y = 4.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF81E29D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = stringResource(R.string.acct_edit_photo),
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                lineHeight = 20.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userEmail,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Badge de Rol (Ganadero)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.22f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = userRole ?: stringResource(R.string.lk_farmer_role),
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            // SECCIÓN 1: CUENTA
            item {
                Text(
                    text = stringResource(R.string.acct_account),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Opción: Mi suscripción
                ProfileOptionCard(
                    iconRes = R.drawable.link_subscription_official,
                    title = stringResource(R.string.acct_plan),
                    subtitle = stringResource(R.string.acct_plan_sub)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Opción: Editar perfil
                ProfileOptionCard(
                    iconRes = R.drawable.ic_edit,
                    title = stringResource(R.string.acct_edit),
                    subtitle = stringResource(R.string.acct_edit_sub)
                )

                Spacer(modifier = Modifier.height(6.dp))
            }

            // SECCIÓN 2: PREFERENCIAS
            item {
                Text(
                    text = stringResource(R.string.acct_preferences),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Opción: Notificaciones
                ProfileOptionCard(
                    iconRes = R.drawable.ic_bell,
                    title = stringResource(R.string.acct_notifications),
                    subtitle = stringResource(R.string.acct_notifications_sub)
                )

                Spacer(modifier = Modifier.height(6.dp))
            }

            // SECCIÓN 3: AYUDA Y LEGAL
            item {
                Text(
                    text = stringResource(R.string.acct_legal),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Opción: Ayuda y contacto
                ProfileOptionCard(
                    iconRes = R.drawable.ic_headphones,
                    title = stringResource(R.string.acct_help),
                    subtitle = stringResource(R.string.acct_help_sub)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Opción: Términos y condiciones
                ProfileOptionCard(
                    iconRes = R.drawable.ic_document,
                    title = stringResource(R.string.acct_terms),
                    subtitle = stringResource(R.string.acct_terms_sub)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Opción: Política de privacidad
                ProfileOptionCard(
                    iconRes = R.drawable.ic_lock,
                    title = stringResource(R.string.acct_privacy),
                    subtitle = stringResource(R.string.acct_privacy_sub)
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // BOTÓN CERRAR SESIÓN
            item {
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = onLogoutClick,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFEF5350)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = stringResource(R.string.lk_logout),
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.lk_logout),
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// COMPONENTE REUTILIZABLE: Tarjeta de Opción de Perfil
@Composable
private fun ProfileOptionCard(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor verde claro con ícono verde
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F8EE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = if(iconRes==R.drawable.link_subscription_official) Color.Unspecified else PrimaryGreenAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    color = NeutralSlate
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Flecha derecha
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = stringResource(R.string.acct_more),
                tint = NeutralDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
