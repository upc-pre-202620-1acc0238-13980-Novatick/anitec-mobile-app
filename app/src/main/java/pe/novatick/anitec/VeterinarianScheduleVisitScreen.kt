package pe.novatick.anitec.ui.screens.veterinarian

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
fun VeterinarianScheduleVisitScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    // Estados del formulario
    var selectedRancher by remember { mutableStateOf("") }
    var selectedAnimal by remember { mutableStateOf("") }
    var visitReason by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf("") }
    var visitTime by remember { mutableStateOf("") }

    // Listas mock para los desplegables
    val rancherOptions = listOf("Juan García", "Rosa Huamán", "Carlos Medina")
    val animalOptions = listOf("Luna (BOV-024)", "Mora (BOV-025)", "Estrella (BOV-026)")

    var rancherDropdownExpanded by remember { mutableStateOf(false) }
    var animalDropdownExpanded by remember { mutableStateOf(false) }

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

            // TOP BAR: BOTÓN ATRÁS Y TÍTULO "Programar visita"
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
                            text = "Programar visita",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = PrimaryGreenAccent
                        )
                        Text(
                            text = "Solo ganaderos con acceso activo",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = NeutralSlate
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // CAMPO: GANADERO *
            item {
                Text(
                    text = "Ganadero *",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF3F5F8))
                            .clickable { rancherDropdownExpanded = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedRancher.ifEmpty { "Seleccionar" },
                                fontFamily = Poppins,
                                fontSize = 14.sp,
                                color = if (selectedRancher.isNotEmpty()) NeutralDark else NeutralSlate
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dropdown),
                                contentDescription = null,
                                tint = NeutralDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = rancherDropdownExpanded,
                        onDismissRequest = { rancherDropdownExpanded = false }
                    ) {
                        rancherOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(text = option, fontFamily = Poppins) },
                                onClick = {
                                    selectedRancher = option
                                    rancherDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // CAMPO: ANIMAL *
            item {
                Text(
                    text = "Animal *",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF3F5F8))
                            .clickable { animalDropdownExpanded = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedAnimal.ifEmpty { "Seleccionar" },
                                fontFamily = Poppins,
                                fontSize = 14.sp,
                                color = if (selectedAnimal.isNotEmpty()) NeutralDark else NeutralSlate
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dropdown),
                                contentDescription = null,
                                tint = NeutralDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = animalDropdownExpanded,
                        onDismissRequest = { animalDropdownExpanded = false }
                    ) {
                        animalOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(text = option, fontFamily = Poppins) },
                                onClick = {
                                    selectedAnimal = option
                                    animalDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // CAMPO: MOTIVO DE LA VISITA *
            item {
                Text(
                    text = "Motivo de la visita *",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
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

            // FECHA * Y HORA * (FILA DE 2 COLUMNAS)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Campo Fecha
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fecha *",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
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

                    // Campo Hora
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hora *",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
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
                }
            }

            // BANNER INFORMATIVO (ic_calendar)
            item {
                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1FBF5))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = null,
                            tint = PrimaryGreenAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "La visita también aparecerá en las próximas visitas del ganadero.",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = NeutralDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // BOTÓN GUARDAR VISITA
            item {
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onSaveSuccess,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Guardar visita",
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