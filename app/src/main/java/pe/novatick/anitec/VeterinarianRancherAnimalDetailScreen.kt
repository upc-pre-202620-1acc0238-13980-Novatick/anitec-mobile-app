package pe.novatick.anitec.ui.screens.veterinarian

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.window.Dialog
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.components.CattleHeaderShape
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.NeutralSlate
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent

// Modelos locales para las muestras de observaciones e historial
data class AnimalObservation(
    val id: String,
    val text: String,
    val authorAndDate: String
)

data class AnimalAttentionHistory(
    val id: String,
    val title: String,
    val dateAndVet: String,
    val description: String
)

val sampleObservations = listOf(
    AnimalObservation("1", "Se adapta bien al lote y mantiene buen apetito.", "Juan García · 10 jun 2025"),
    AnimalObservation("2", "Desde ayer come un poco menos, pero sin otros síntomas visibles.", "Juan García · 8 jun 2025")
)

val sampleAttentions = listOf(
    AnimalAttentionHistory("1", "Control general", "16 de junio de 2025 · Dra. María López", "Se realizó revisión preventiva completa, control de peso y..")
)

@Composable
fun VeterinarianRancherAnimalDetailScreen(
    animalName: String = "Luna",
    animalCode: String = "BOV-024",
    animalSpecies: String = "Bovino",
    onBackClick: () -> Unit,
    onNavigateToObservations: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToHistoryDetail: (String) -> Unit,
    onNavigateToRegisterAttention: () -> Unit
) {
    var showScheduleVisitModal by remember { mutableStateOf(false) }

    // Modal para "Programar visita"
    if (showScheduleVisitModal) {
        ScheduleVisitDialogModal(
            onDismiss = { showScheduleVisitModal = false },
            onSave = { showScheduleVisitModal = false }
        )
    }

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

            // TOP BAR: BOTÓN ATRÁS + TÍTULO Y SUBTÍTULO
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            text = animalName,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            color = PrimaryGreenAccent
                        )
                        Text(
                            text = "$animalCode · $animalSpecies",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = NeutralSlate
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // TARJETA 1: ÍCONO DEL ANIMAL + BADGE DE ESTADO
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFE8F8EE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cow),
                                contentDescription = null,
                                tint = PrimaryGreenAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE8F8EE))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Activo",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PrimaryGreenAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Registrado en Mi finca",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = NeutralSlate
                            )
                        }
                    }
                }
            }

            // SECCIÓN 2: DATOS GENERALES (TABLA 2x2)
            item {
                Text(
                    text = "Datos generales",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NeutralDark
                )

                Spacer(modifier = Modifier.height(2.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(14.dp)
                            ) {
                                Text("Especie", fontFamily = Poppins, fontSize = 12.sp, color = NeutralSlate)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Bovino", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NeutralDark)
                            }
                            VerticalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.height(55.dp).width(1.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(14.dp)
                            ) {
                                Text("Sexo", fontFamily = Poppins, fontSize = 12.sp, color = NeutralSlate)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Hembra", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NeutralDark)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(14.dp)
                            ) {
                                Text("Raza", fontFamily = Poppins, fontSize = 12.sp, color = NeutralSlate)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Holstein", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NeutralDark)
                            }
                            VerticalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.height(55.dp).width(1.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(14.dp)
                            ) {
                                Text("Nacimiento", fontFamily = Poppins, fontSize = 12.sp, color = NeutralSlate)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("12/03/2021", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NeutralDark)
                            }
                        }
                    }
                }
            }

            // SECCIÓN 3: OBSERVACIONES
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Observaciones",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NeutralDark
                    )
                    Text(
                        text = "Ver todas (3)",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryGreenAccent,
                        modifier = Modifier.clickable { onNavigateToObservations() }
                    )
                }
                Text(
                    text = "Anota cambios que notes en el comportamiento o estado del animal",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = NeutralSlate
                )

                Spacer(modifier = Modifier.height(2.dp))
            }

            items(sampleObservations, key = { "obs_${it.id}" }) { obs ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(72.dp)
                                .background(PrimaryGreenAccent)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = obs.text,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = NeutralDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = obs.authorAndDate,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = NeutralSlate
                            )
                        }
                    }
                }
            }

            // SECCIÓN 4: HISTORIAL DE ATENCIONES
            item {
                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial de atenciones",
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
                        modifier = Modifier.clickable { onNavigateToHistory() }
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
            }

            items(sampleAttentions, key = { "att_${it.id}" }) { att ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToHistoryDetail(att.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(52.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(PrimaryGreenAccent)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = att.title,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = NeutralDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = att.dateAndVet,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = NeutralSlate
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = att.description,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = NeutralSlate,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = "Ver detalle",
                            tint = NeutralSlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // SECCIÓN 5: ACCESOS RÁPIDOS (PROGRAMAR VISITA / REGISTRAR ATENCIÓN)
            item {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Tarjeta: Programar visita
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FBF5)),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showScheduleVisitModal = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryGreenAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calendar),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Programar visita",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NeutralDark
                            )
                        }
                    }

                    // Tarjeta: Registrar atención
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FBF5)),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToRegisterAttention() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryGreenAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_stethoscope),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Registrar atención",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NeutralDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// COMPONENTE: Modal Dialog para "Programar visita" (Foto 2)
@Composable
private fun ScheduleVisitDialogModal(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var visitReason by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf("") }
    var visitTime by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Programar visita",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = PrimaryGreenAccent
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo 1: Motivo de la visita *
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Motivo de la visita *",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = visitReason,
                        onValueChange = { visitReason = it },
                        placeholder = {
                            Text(
                                text = "Ej. Control general",
                                fontFamily = Poppins,
                                color = NeutralSlate,
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
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
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Campo 2: Fecha *
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Fecha *",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = visitDate,
                        onValueChange = { visitDate = it },
                        placeholder = {
                            Text(
                                text = "dd/mm/aaaa",
                                fontFamily = Poppins,
                                color = NeutralSlate,
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_calendar),
                                contentDescription = null,
                                tint = NeutralSlate,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
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
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Campo 3: Hora *
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Hora *",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = visitTime,
                        onValueChange = { visitTime = it },
                        placeholder = {
                            Text(
                                text = "--:--",
                                fontFamily = Poppins,
                                color = NeutralSlate,
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clock),
                                contentDescription = null,
                                tint = NeutralSlate,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
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
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botones Cancelar / Guardar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Cancelar
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFF0F0),
                            contentColor = Color(0xFFEF5350)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Botón Guardar
                    Button(
                        onClick = onSave,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreenAccent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Guardar",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}