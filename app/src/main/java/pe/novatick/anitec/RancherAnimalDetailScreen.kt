package pe.novatick.anitec.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.components.CattleHeaderShape
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.NeutralSlate
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent
import kotlin.math.round

@Composable
fun RancherAnimalDetailScreen(
    animalId: String? = "1",
    onBackClick: () -> Unit,
    onNavigateToObservations: () -> Unit,
    onNavigateToNewObservation: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToHistoryDetail: (String) -> Unit,
    onNavigateToEditAnimal: () -> Unit,
    onConfirmDeactivate: () -> Unit
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showDeactivateModal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ENCABEZADO CURVO VERDE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(CattleHeaderShape)
                .background(PrimaryGreenAccent)
        )

        // CONTENIDO PRINCIPAL SCROLLABLE
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(35.dp)) }

            // 1. TOP BAR: BOTÓN ATRÁS, NOMBRE/CÓDIGO Y MENÚ DE MÁS OPCIONES
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Atrás
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

                    // Título y Código
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Luna",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = PrimaryGreenAccent
                        )
                        Text(
                            text = "BOV-024 · Bovino",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = NeutralSlate
                        )
                    }

                    // Botón Más Opciones (ic_more_options)
                    Box {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F8EE))
                                .clickable { showOptionsMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_more_options),
                                contentDescription = "Más opciones",
                                tint = PrimaryGreenAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // DROPDOWN MENU DE MÁS OPCIONES (FOTO 2)
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(220.dp)
                        ) {
                            // Opción: Editar datos
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_edit),
                                            contentDescription = null,
                                            tint = NeutralDark,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Editar datos",
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = NeutralDark
                                        )
                                    }
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onNavigateToEditAnimal()
                                }
                            )

                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                            // Opción: Dar de baja
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_warning),
                                            contentDescription = null,
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Dar de baja",
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFFE53935)
                                        )
                                    }
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    showDeactivateModal = true
                                }
                            )
                        }
                    }
                }
            }

            // 2. RESUMEN DEL ANIMAL (ÍCONO, ESTADO Y FINCA)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFE8F8EE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cow),
                                contentDescription = null,
                                tint = PrimaryGreenAccent,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE8F8EE))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Activo",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
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

            // 3. SECCIÓN: DATOS GENERALES (TABLA 2X2)
            item {
                Text(
                    text = "Datos generales",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Fila Superior (Especie | Sexo)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DataCell(
                                label = "Especie",
                                value = "Bovino",
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(65.dp)
                                    .background(Color(0xFFE2E8F0))
                            )
                            DataCell(
                                label = "Sexo",
                                value = "Hembra",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        // Fila Inferior (Raza | Nacimiento)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DataCell(
                                label = "Raza",
                                value = "Holstein",
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(65.dp)
                                    .background(Color(0xFFE2E8F0))
                            )
                            DataCell(
                                label = "Nacimiento",
                                value = "12/03/2021",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. SECCIÓN: OBSERVACIONES
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

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Anota cambios que notes en el comportamiento o estado del animal",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = NeutralSlate
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tarjeta de Observación Reciente
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Barra Verde Lateral Izquierda
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(72.dp)
                                .background(PrimaryGreenAccent)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Se adapta bien al lote y mantiene buen apetito.",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = NeutralDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Juan García · 10 jun 2025",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = NeutralSlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón "+ Agregar observación"
                OutlinedButton(
                    onClick = onNavigateToNewObservation,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, PrimaryGreenAccent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
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
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // 5. SECCIÓN: HISTORIAL DE ATENCIONES
            item {
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

                Spacer(modifier = Modifier.height(10.dp))

                // Atención 1: Control general
                AttentionHistoryCard(
                    title = "Control general",
                    subtitle = "16 de junio de 2025 · Dra. María López",
                    description = "Se realizó revisión preventiva completa, control de peso y...",
                    onClick = { onNavigateToHistoryDetail("1") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Atención 2: Vacunación preventiva
                AttentionHistoryCard(
                    title = "Vacunación preventiva",
                    subtitle = "3 de marzo de 2025 · Dra. María López",
                    description = "Consulta de rutina para aplicación de vacunas del...",
                    onClick = { onNavigateToHistoryDetail("2") }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // MODAL DE CONFIRMACIÓN DE BAJA (FOTO 3)
        if (showDeactivateModal) {
            DeactivateAnimalDialog(
                animalName = "Luna",
                onDismiss = { showDeactivateModal = false },
                onConfirm = {
                    showDeactivateModal = false
                    onConfirmDeactivate()
                }
            )
        }
    }
}

// COMPONENTE: Celda de la Tabla de Datos Generales
@Composable
private fun DataCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = NeutralSlate
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = NeutralDark
        )
    }
}

// COMPONENTE: Tarjeta de Atención Médica
@Composable
private fun AttentionHistoryCard(
    title: String,
    subtitle: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(84.dp)
                    .background(PrimaryGreenAccent)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = NeutralSlate
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = NeutralSlate
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
}

// COMPONENTE: Modal de Confirmación "Dar de baja" (Foto 3)
@Composable
private fun DeactivateAnimalDialog(
    animalName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dar de baja a $animalName",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryGreenAccent,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "El animal saldrá del inventario activo. Su historial, atenciones y observaciones se conservarán y podrán consultarse en cualquier momento.",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = NeutralSlate,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Dar de baja (Rosa/Rojo)
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFF0F0),
                            contentColor = Color(0xFFE53935)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Dar de baja",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Botón Cancelar (Verde Outlined)
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, PrimaryGreenAccent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreenAccent),
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
                }
            }
        }
    }
}