package pe.novatick.anitec.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
fun RancherVetsScreen(
    onInviteVetClick: () -> Unit,
    onRemoveVetClick: () -> Unit,
    onCancelInviteClick: () -> Unit
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

            // TÍTULO Y SUBTÍTULO
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Veterinarios",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = PrimaryGreenAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Gestiona quién puede atender tu ganado",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = NeutralSlate
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // BOTÓN INVITAR VETERINARIO
            item {
                Button(
                    onClick = onInviteVetClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mail),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Invitar veterinario",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // SECCIÓN: VINCULADOS
            item {
                Text(
                    text = "Vinculados",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NeutralDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // VET 1: Dra. María López
                LinkedVetCardItem(
                    initials = "ML",
                    avatarBgColor = Color(0xFF50C878),
                    name = "Dra. María López",
                    details = "Veterinaria · Desde 04 feb 2024",
                    onRemoveClick = onRemoveVetClick
                )

                Spacer(modifier = Modifier.height(10.dp))

                // VET 2: Dr. Carlos Ruiz
                LinkedVetCardItem(
                    initials = "CR",
                    avatarBgColor = Color(0xFF64B5F6),
                    name = "Dr. Carlos Ruiz",
                    details = "Veterinario · Desde 12 may 2025",
                    onRemoveClick = onRemoveVetClick
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // SECCIÓN: INVITACIONES PENDIENTES
            item {
                Text(
                    text = "Invitaciones pendientes",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NeutralDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // CARD INVITACIÓN PENDIENTE
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFFEF0C7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge con icono de correo
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mail),
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "dra.sofia@email.com",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NeutralDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Enviada hoy · Pendiente",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = NeutralSlate
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Botón Cancelar (Borde e icono Ámbar)
                        OutlinedButton(
                            onClick = onCancelInviteClick,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFD97706)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "Cancelar",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// COMPONENTE: Tarjeta para Veterinario Vinculado
@Composable
private fun LinkedVetCardItem(
    initials: String,
    avatarBgColor: Color,
    name: String,
    details: String,
    onRemoveClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Circular con Iniciales
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(avatarBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = details,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = NeutralSlate
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón Quitar (Borde e icono Rojo)
            OutlinedButton(
                onClick = onRemoveClick,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFEF5350)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Quitar",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}