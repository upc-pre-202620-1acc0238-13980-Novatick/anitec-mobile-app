package pe.novatick.anitec.ui.screens.veterinarian

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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

// Modelo para ganaderos vinculados
data class ActiveRancherItem(
    val id: String,
    val initials: String,
    val name: String,
    val info: String,
    val avatarBgColor: Color = Color(0xFF50C878)
)

// Modelo para invitaciones pendientes
data class PendingRancherInviteItem(
    val id: String,
    val initials: String,
    val name: String,
    val dateText: String,
    val avatarBgColor: Color
)

val sampleActiveRanchers = listOf(
    ActiveRancherItem("1", "JG", "Juan García", "12 animales · Fundo Los Álamos"),
    ActiveRancherItem("2", "AP", "Ana Pérez", "7 animales · El Roble"),
    ActiveRancherItem("3", "LM", "Luis Mendoza", "18 animales · La Esperanza")
)

val samplePendingRancherInvites = listOf(
    PendingRancherInviteItem("1", "ML", "Luis Martínez", "Invitación del 16 jun 2025", Color(0xFF50C878)),
    PendingRancherInviteItem("2", "ML", "Sara Valencia", "Invitación del 15 jun 2025", Color(0xFF64B5F6))
)

@Composable
fun VeterinarianRancherScreen(
    onNavigateToRancherDetail: (String) -> Unit,
    onNavigateToInvites: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(35.dp)) }

            // TÍTULO Y SUBTÍTULO
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Ganaderos",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = PrimaryGreenAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "3 vinculados",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = NeutralSlate
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // BUSCADOR
            item {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Buscar ganadero",
                            fontFamily = Poppins,
                            color = NeutralSlate,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "Buscar",
                            tint = NeutralSlate,
                            modifier = Modifier.size(20.dp)
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
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // SECCIÓN: VINCULACIÓN ACTIVA
            item {
                Text(
                    text = "Vinculación activa",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NeutralDark
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // LISTA DE GANADEROS VINCULADOS (Claves únicas prefijadas)
            items(sampleActiveRanchers, key = { "active_${it.id}" }) { rancher ->
                ActiveRancherCardItem(
                    rancher = rancher,
                    onClick = { onNavigateToRancherDetail(rancher.id) }
                )
            }

            // SECCIÓN: INVITACIONES PENDIENTES + LINK "VER TODAS (2)"
            item {
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2 invitaciones pendientes",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NeutralDark
                    )

                    Text(
                        text = "Ver todas (2)",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryGreenAccent,
                        modifier = Modifier.clickable { onNavigateToInvites() }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // LISTA DE INVITACIONES PENDIENTES (Claves únicas prefijadas)
            items(samplePendingRancherInvites, key = { "pending_${it.id}" }) { invite ->
                PendingRancherInviteCardItem(
                    invite = invite,
                    onAccept = { },
                    onReject = { }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// COMPONENTE: Tarjeta Ganadero Vinculado
@Composable
private fun ActiveRancherCardItem(
    rancher: ActiveRancherItem,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                    .background(rancher.avatarBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rancher.initials,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rancher.name,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = rancher.info,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = NeutralSlate
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "Ver detalle",
                tint = NeutralDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// COMPONENTE: Tarjeta Invitación Pendiente
@Composable
private fun PendingRancherInviteCardItem(
    invite: PendingRancherInviteItem,
    onAccept: () -> Unit,
    onReject: () -> Unit
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
            // Avatar Circular
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(invite.avatarBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = invite.initials,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invite.name,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = invite.dateText,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = NeutralSlate
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Botón Rechazar
            OutlinedButton(
                onClick = onReject,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PrimaryGreenAccent),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreenAccent),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Rechazar",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Botón Aceptar
            Button(
                onClick = onAccept,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Aceptar",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}