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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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

// Modelo para los días del selector de calendario
data class CalendarDayItem(
    val dayName: String,
    val dayNumber: String
)

// Modelo para ítems de visitas
data class VetAgendaVisitItem(
    val id: String,
    val time: String,
    val title: String,
    val subtitle: String
)

val sampleCalendarDays = listOf(
    CalendarDayItem("LUN", "16"),
    CalendarDayItem("MAR", "17"),
    CalendarDayItem("MIÉ", "18"),
    CalendarDayItem("JUE", "19"),
    CalendarDayItem("VIE", "20")
)

val sampleVetAgendaVisits = listOf(
    VetAgendaVisitItem("1", "10:30", "Control general", "Luna · Juan García"),
    VetAgendaVisitItem("2", "14:00", "Vacunación", "Mora · Juan García"),
    VetAgendaVisitItem("3", "15:20", "Revisión de cojera", "Estrella · Rosa Huamán"),
    VetAgendaVisitItem("4", "17:00", "Seguimiento postratamiento", "Torito · Carlos Medina")
)

@Composable
fun VeterinarianAgendaScreen(
    onNavigateToScheduleVisit: () -> Unit,
    onNavigateToVisitDetail: (String) -> Unit
) {
    var selectedDayIndex by remember { mutableStateOf(2) } // Seleccionado el día Mié 18

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
                        text = "Mi agenda",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = PrimaryGreenAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Organiza tus visitas",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = NeutralSlate
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // SELECTOR HORIZONTAL DE DÍAS (LUN 16 a VIE 20)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    sampleCalendarDays.forEachIndexed { index, day ->
                        val isSelected = selectedDayIndex == index

                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) PrimaryGreenAccent else Color(0xFFF3F5F8)
                                )
                                .clickable { selectedDayIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = day.dayName,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else NeutralSlate
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = day.dayNumber,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = if (isSelected) Color.White else NeutralDark
                                )
                            }
                        }
                    }
                }
            }

            // BOTÓN "+ Programar Visita"
            item {
                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onNavigateToScheduleVisit,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_plus),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Programar Visita",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // SUBTÍTULO DE LA FECHA SELECCIONADA
            item {
                Text(
                    text = "Miércoles, 18 de junio",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NeutralDark
                )
            }

            // CONTENEDOR TARJETA CON LA LISTA DE VISITAS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        sampleVetAgendaVisits.forEachIndexed { index, visit ->
                            VetAgendaVisitRowItem(
                                item = visit,
                                onClick = { onNavigateToVisitDetail(visit.id) }
                            )
                            if (index < sampleVetAgendaVisits.size - 1) {
                                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// COMPONENTE: Fila de la Agenda de Visitas
@Composable
private fun VetAgendaVisitRowItem(
    item: VetAgendaVisitItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hora
        Text(
            text = item.time,
            fontFamily = Poppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = PrimaryGreenAccent,
            modifier = Modifier.width(48.dp)
        )

        // Punto indicador verde
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(PrimaryGreenAccent)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Título y Subtítulo
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NeutralDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = NeutralSlate
            )
        }

        // Flecha derecha (ic_arrow_right)
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = "Ver detalle",
            tint = NeutralSlate,
            modifier = Modifier.size(18.dp)
        )
    }
}