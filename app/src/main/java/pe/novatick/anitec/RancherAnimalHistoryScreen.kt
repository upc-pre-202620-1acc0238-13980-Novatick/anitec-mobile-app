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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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

// Modelo interno de atención médica
data class HistoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String
)

// Listado mock del historial
val sampleHistoryList = listOf(
    HistoryItem(
        id = "1",
        title = "Control general",
        subtitle = "16 de junio de 2025 · Dra. María López",
        description = "Se realizó revisión preventiva completa, control de peso y..."
    ),
    HistoryItem(
        id = "2",
        title = "Vacunación preventiva",
        subtitle = "3 de marzo de 2025 · Dra. María López",
        description = "Consulta de rutina para aplicación de vacunas del calendar.."
    )
)

@Composable
fun RancherAnimalHistoryScreen(
    onBackClick: () -> Unit,
    onNavigateToHistoryDetail: (String) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(35.dp)) }

            // TOP BAR: BOTÓN ATRÁS Y TÍTULO "Historial de atenciones"
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

                    // Título de la vista
                    Text(
                        text = "Historial de atenciones",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = PrimaryGreenAccent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // TARJETAS DE LA LISTA DE ATENCIONES
            items(sampleHistoryList, key = { it.id }) { item ->
                HistoryCardItem(
                    historyItem = item,
                    onClick = { onNavigateToHistoryDetail(item.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// COMPONENTE: Tarjeta Individual del Historial de Atenciones
@Composable
private fun HistoryCardItem(
    historyItem: HistoryItem,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Tira Verde Vertical Izquierda
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(84.dp)
                    .background(PrimaryGreenAccent)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = historyItem.title,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = historyItem.subtitle,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = NeutralSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = historyItem.description,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = NeutralSlate
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Flecha Derecha (ic_arrow_right)
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "Ver detalle de la atención",
                    tint = NeutralSlate,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}