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

// Modelo interno para items de observación
data class ObservationItem(
    val id: String,
    val text: String,
    val authorAndDate: String
)

// Listado de observaciones mock idéntico al prototipo
val sampleObservationsList = listOf(
    ObservationItem(
        id = "1",
        text = "Se adapta bien al lote y mantiene buen apetito.",
        authorAndDate = "Juan García · 10 jun 2025"
    ),
    ObservationItem(
        id = "2",
        text = "Desde ayer come un poco menos, pero sin otros síntomas visibles.",
        authorAndDate = "Juan García · 8 jun 2025"
    ),
    ObservationItem(
        id = "3",
        text = "Permanece más quieta que de costumbre, especialmente en la tarde.",
        authorAndDate = "Juan García · 2 jun 2025"
    )
)

@Composable
fun RancherAnimalObservationsScreen(
    onBackClick: () -> Unit,
    onNavigateToNewObservation: () -> Unit
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

            // TOP BAR: BOTÓN ATRÁS Y TÍTULO "Observaciones" CENTRADO
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Botón Atrás
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F8EE))
                            .clickable { onBackClick() }
                            .align(Alignment.CenterStart),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_left),
                            contentDescription = "Regresar",
                            tint = PrimaryGreenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Título de la Pantalla
                    Text(
                        text = "Observaciones",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = PrimaryGreenAccent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // TARJETAS DE LA LISTA DE OBSERVACIONES
            items(sampleObservationsList, key = { it.id }) { obs ->
                ObservationCardItem(observation = obs)
            }

            // BOTÓN "+ Agregar observación"
            item {
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onNavigateToNewObservation,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, PrimaryGreenAccent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_plus),
                            contentDescription = null,
                            tint = PrimaryGreenAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Agregar observación",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// COMPONENTE: Tarjeta Individual de Observación
@Composable
private fun ObservationCardItem(observation: ObservationItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Tira Verde Lateral Izquierda
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(80.dp)
                    .background(PrimaryGreenAccent)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = observation.text,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = NeutralDark,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = observation.authorAndDate,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = NeutralSlate
                )
            }
        }
    }
}