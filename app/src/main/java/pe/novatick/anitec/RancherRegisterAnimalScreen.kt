package pe.novatick.anitec

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.components.CattleHeaderShape
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.NeutralSlate
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent
import kotlin.random.Random

@Composable
fun RancherRegisterAnimalScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    // ESTADOS DEL FORMULARIO
    var code by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    // Estado para controlar errores de validación
    var showValidationError by remember { mutableStateOf(false) }

    // Opciones para Dropdowns
    val speciesOptions = listOf("Bovino", "Ovino", "Caprino", "Porcino", "Equino")
    val genderOptions = listOf("Macho", "Hembra")

    var speciesDropdownExpanded by remember { mutableStateOf(false) }
    var genderDropdownExpanded by remember { mutableStateOf(false) }

    // ESTADOS DE IMAGEN (Foto tomada o seleccionada de galería)
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedImageUri = null
        }
    }

    // Launcher Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            capturedBitmap = null
        }
    }

    // Función para generar código aleatorio según la especie
    fun generateRandomCode() {
        val prefix = when (selectedSpecies) {
            "Bovino" -> "BOV"
            "Ovino" -> "OV"
            "Caprino" -> "CAP"
            "Porcino" -> "POR"
            "Equino" -> "EQ"
            else -> "ANI"
        }
        val randomNum = Random.nextInt(100, 999)
        code = "$prefix-$randomNum"
    }

    // Validación de campos obligatorios
    fun validateAndSave() {
        if (code.isNotBlank() && selectedSpecies.isNotBlank() && selectedGender.isNotBlank()) {
            showValidationError = false
            onSaveSuccess()
        } else {
            showValidationError = true
        }
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

        // 2. CONTENIDO SCROLLABLE DE LA PANTALLA
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ESPACIO SUPERIOR
            item {
                Spacer(modifier = Modifier.height(65.dp))
            }

            // HEADER BAR (BOTÓN ATRÁS + TÍTULO)
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
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

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Registrar animal",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = PrimaryGreenAccent
                        )
                        Text(
                            text = "Completa los datos del animal",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = NeutralSlate
                        )
                    }
                }
            }

            // CONTENEDOR DE FOTO (TOMAR / SUBIR FOTO)
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
                        // PREVIEW DE FOTO O ÍCONO POR DEFECTO
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFFE8F8EE)),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                capturedBitmap != null -> {
                                    Image(
                                        bitmap = capturedBitmap!!.asImageBitmap(),
                                        contentDescription = "Foto tomada",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                selectedImageUri != null -> {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = selectedImageUri),
                                        contentDescription = "Foto subida",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_cow),
                                        contentDescription = "Vaca por defecto",
                                        tint = PrimaryGreenAccent,
                                        modifier = Modifier.size(52.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // BOTONES DE CÁMARA Y GALERÍA
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Botón Tomar Foto
                            Button(
                                onClick = { cameraLauncher.launch(null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_plus),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tomar Foto",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Botón Subir Foto
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_folder),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Subir Foto",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CAMPO CÓDIGO + BOTÓN GENERAR CÓDIGO
            item {
                Text(
                    text = "Código *",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = code,
                        onValueChange = { code = it },
                        placeholder = {
                            Text(
                                text = "Ej. BOV-042",
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
                            .weight(1f)
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { generateRandomCode() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(
                            text = "Generar código",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // DROPDOWNS: ESPECIE * Y SEXO *
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dropdown Especie
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Especie *",
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
                                    .clickable { speciesDropdownExpanded = true }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selectedSpecies.isEmpty()) "Seleccionar" else selectedSpecies,
                                        fontFamily = Poppins,
                                        fontSize = 14.sp,
                                        color = if (selectedSpecies.isEmpty()) NeutralSlate else NeutralDark
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_dropdown),
                                        contentDescription = null,
                                        tint = NeutralDark
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = speciesDropdownExpanded,
                                onDismissRequest = { speciesDropdownExpanded = false }
                            ) {
                                speciesOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option, fontFamily = Poppins) },
                                        onClick = {
                                            selectedSpecies = option
                                            speciesDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Dropdown Sexo
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sexo *",
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
                                    .clickable { genderDropdownExpanded = true }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selectedGender.isEmpty()) "Seleccionar" else selectedGender,
                                        fontFamily = Poppins,
                                        fontSize = 14.sp,
                                        color = if (selectedGender.isEmpty()) NeutralSlate else NeutralDark
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_dropdown),
                                        contentDescription = null,
                                        tint = NeutralDark
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = genderDropdownExpanded,
                                onDismissRequest = { genderDropdownExpanded = false }
                            ) {
                                genderOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option, fontFamily = Poppins) },
                                        onClick = {
                                            selectedGender = option
                                            genderDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // NOMBRE (OPCIONAL)
            item {
                Text(
                    text = "Nombre (opcional)",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            text = "Ej. Canela",
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

            // RAZA (OPCIONAL)
            item {
                Text(
                    text = "Raza (opcional)",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = breed,
                    onValueChange = { breed = it },
                    placeholder = {
                        Text(
                            text = "Ej. Holstein",
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

            // FECHA DE NACIMIENTO (OPCIONAL)
            item {
                Text(
                    text = "Fecha de nacimiento (opcional)",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    placeholder = {
                        Text(
                            text = "DD / MM / AAAA",
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

            // TARJETA INFORMATIVA
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8F8EE))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_document),
                            contentDescription = null,
                            tint = PrimaryGreenAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Podrás agregar observaciones y consultar su historial después de registrarlo.",
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            color = NeutralDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // BOTÓN GUARDAR ANIMAL + MENSAJE DE ERROR DE VALIDACIÓN
            item {
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { validateAndSave() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Guardar animal",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                // Mensaje de Error en Rojo
                if (showValidationError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "falta especificar los campos obligatorios",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}