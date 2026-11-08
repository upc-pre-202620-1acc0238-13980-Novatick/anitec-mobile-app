package pe.novatick.anitec.ui.screens.veterinarian

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.window.Dialog
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.components.CattleHeaderShape
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.NeutralSlate
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent

@Composable
fun VeterinarianRegisterAttentionScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    // Estados de Formulario
    var selectedRancher by remember { mutableStateOf("") }
    var selectedAnimalCode by remember { mutableStateOf("") }
    var attentionDate by remember { mutableStateOf("") }
    var attentionReason by remember { mutableStateOf("") }
    var attentionDescription by remember { mutableStateOf("") }
    var careInstructions by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Estados de Desplegables
    var rancherDropdownExpanded by remember { mutableStateOf(false) }
    var animalCodeDropdownExpanded by remember { mutableStateOf(false) }

    // Listas Mock para Desplegables
    val rancherOptions = listOf("Juan García", "Ana Pérez", "Luis Mendoza")
    val animalCodeOptions = listOf("BOV-024", "OV-031", "EQ-008", "BOV-015")

    // Modales Dialogs
    var showTreatmentModal by remember { mutableStateOf(false) }
    var showVaccineModal by remember { mutableStateOf(false) }

    // Launcher para seleccionar imagen de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Modal 1: Tratamiento Realizado
    if (showTreatmentModal) {
        TreatmentModalDialog(
            onDismiss = { showTreatmentModal = false },
            onAdd = { showTreatmentModal = false }
        )
    }

    // Modal 2: Vacuna Aplicada
    if (showVaccineModal) {
        VaccineModalDialog(
            onDismiss = { showVaccineModal = false },
            onAdd = { showVaccineModal = false }
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
                            text = "Registrar atención",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = PrimaryGreenAccent
                        )
                        Text(
                            text = "Guarda la atención y sus indicaciones",
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

            // FILA CON CÓDIGO DEL ANIMAL * Y FECHA *
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Campo: Código del animal *
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Código del animal *",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
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
                                    .clickable { animalCodeDropdownExpanded = true }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedAnimalCode.ifEmpty { "Ingrese código" },
                                        fontFamily = Poppins,
                                        fontSize = 13.sp,
                                        color = if (selectedAnimalCode.isNotEmpty()) NeutralDark else NeutralSlate
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_dropdown),
                                        contentDescription = null,
                                        tint = NeutralDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = animalCodeDropdownExpanded,
                                onDismissRequest = { animalCodeDropdownExpanded = false }
                            ) {
                                animalCodeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option, fontFamily = Poppins) },
                                        onClick = {
                                            selectedAnimalCode = option
                                            animalCodeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Campo: Fecha *
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fecha *",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NeutralDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        TextField(
                            value = attentionDate,
                            onValueChange = { attentionDate = it },
                            placeholder = {
                                Text(
                                    text = "dd/mm/aaaa",
                                    fontFamily = Poppins,
                                    color = NeutralSlate,
                                    fontSize = 13.sp
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calendar),
                                    contentDescription = null,
                                    tint = NeutralSlate,
                                    modifier = Modifier.size(18.dp)
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

            // CAMPO: MOTIVO *
            item {
                Text(
                    text = "Motivo *",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = attentionReason,
                    onValueChange = { attentionReason = it },
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

            // CAMPO: DESCRIPCIÓN *
            item {
                Text(
                    text = "Descripción *",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = attentionDescription,
                    onValueChange = { attentionDescription = it },
                    placeholder = {
                        Text(
                            text = "Describe la atención realizada",
                            fontFamily = Poppins,
                            color = NeutralSlate,
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F5F8),
                        unfocusedContainerColor = Color(0xFFF3F5F8),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }

            // BOTÓN: AGREGAR TRATAMIENTO (Borde Naranja)
            item {
                Spacer(modifier = Modifier.height(2.dp))

                OutlinedButton(
                    onClick = { showTreatmentModal = true },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFD97706)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_book),
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Agregar tratamiento",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // BOTÓN: AGREGAR VACUNA (Borde Azul)
            item {
                OutlinedButton(
                    onClick = { showVaccineModal = true },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF2563EB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_vaccine),
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Agregar vacuna",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
            }

            // CAMPO: DESCRIBE LOS CUIDADOS QUE DEBERÁ SEGUIR EL GANADERO
            item {
                Text(
                    text = "Describe los cuidados que deberá seguir el ganadero",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = careInstructions,
                    onValueChange = { careInstructions = it },
                    placeholder = {
                        Text(
                            text = "Describe los cuidados aquí",
                            fontFamily = Poppins,
                            color = NeutralSlate,
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F5F8),
                        unfocusedContainerColor = Color(0xFFF3F5F8),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                )

                Spacer(modifier = Modifier.height(2.dp))
            }

            // BOTÓN: AGREGAR IMAGEN
            item {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, PrimaryGreenAccent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_picture),
                            contentDescription = null,
                            tint = PrimaryGreenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedImageUri != null) "Imagen seleccionada" else "Agregar Imagen",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
            }

            // BANNER INFORMATIVO (Verde Claro con ic_document)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F8EE))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_document),
                            contentDescription = null,
                            tint = PrimaryGreenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Siempre es importante detallar el caso a los ganaderos.",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = NeutralDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // BOTÓN GUARDAR ATENCIÓN
            item {
                Button(
                    onClick = onSaveSuccess,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Guardar atención",
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

// COMPONENTE: Modal Dialog para "Tratamiento Realizado"
@Composable
private fun TreatmentModalDialog(
    onDismiss: () -> Unit,
    onAdd: () -> Unit
) {
    var treatmentName by remember { mutableStateOf("") }
    var treatmentDate by remember { mutableStateOf("") }
    var treatmentDetail by remember { mutableStateOf("") }

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
                    text = "Tratamiento Realizado",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = PrimaryGreenAccent
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Nombre del tratamiento o procedimiento *",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = treatmentName,
                        onValueChange = { treatmentName = it },
                        placeholder = {
                            Text(
                                text = "Ej. Curación de herida",
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

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Fecha de realización *",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = treatmentDate,
                        onValueChange = { treatmentDate = it },
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

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Detalle de lo realizado *",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = treatmentDetail,
                        onValueChange = { treatmentDetail = it },
                        placeholder = {
                            Text(
                                text = "Describe los cuidados aquí",
                                fontFamily = Poppins,
                                color = NeutralSlate,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3F5F8),
                            unfocusedContainerColor = Color(0xFFF3F5F8),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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

                    Button(
                        onClick = onAdd,
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
                            text = "Agregar",
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

// COMPONENTE: Modal Dialog para "Vacuna Aplicada"
@Composable
private fun VaccineModalDialog(
    onDismiss: () -> Unit,
    onAdd: () -> Unit
) {
    var vaccineName by remember { mutableStateOf("") }
    var vaccineDate by remember { mutableStateOf("") }
    var vaccineNotes by remember { mutableStateOf("") }

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
                    text = "Vacuna Aplicada",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = PrimaryGreenAccent
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Nombre de la vacuna *",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = vaccineName,
                        onValueChange = { vaccineName = it },
                        placeholder = {
                            Text(
                                text = "Escribe el nombre de la vacuna aplicada",
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

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Fecha de aplicación *",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = vaccineDate,
                        onValueChange = { vaccineDate = it },
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

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Observaciones *",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = vaccineNotes,
                        onValueChange = { vaccineNotes = it },
                        placeholder = {
                            Text(
                                text = "Información adicional sobre la aplicación",
                                fontFamily = Poppins,
                                color = NeutralSlate,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3F5F8),
                            unfocusedContainerColor = Color(0xFFF3F5F8),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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

                    Button(
                        onClick = onAdd,
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
                            text = "Agregar",
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