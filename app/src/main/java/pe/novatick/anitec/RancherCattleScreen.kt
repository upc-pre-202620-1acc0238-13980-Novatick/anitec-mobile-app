package pe.novatick.anitec

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import pe.novatick.anitec.data.model.Animal
import pe.novatick.anitec.data.model.AnimalCategory
import pe.novatick.anitec.data.model.sampleAnimalsList
import pe.novatick.anitec.ui.components.CattleHeaderShape
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.NeutralSlate
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent

@Composable
fun RancherCattleScreen(
    onNavigateToRegisterAnimal: () -> Unit,
    onNavigateToAnimalDetail: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AnimalCategory.ALL) }

    // Filtrado en tiempo real por búsqueda y categoría
    val filteredAnimals by remember(searchQuery, selectedCategory) {
        derivedStateOf {
            sampleAnimalsList.filter { animal ->
                val matchesSearch = animal.name.contains(searchQuery, ignoreCase = true) ||
                        animal.code.contains(searchQuery, ignoreCase = true)
                val matchesCategory = (selectedCategory == AnimalCategory.ALL) || (animal.category == selectedCategory)

                matchesSearch && matchesCategory
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ENCABEZADO CURVO SUPERIOR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(CattleHeaderShape)
                .background(PrimaryGreenAccent)
                .statusBarsPadding()
        )

        // CONTENIDO CON LAZY COLUMN PARA SCROLL FLUIDO
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. TÍTULO Y CONTADOR DINÁMICO
            item {
                Spacer(modifier = Modifier.height(65.dp))

                Text(
                    text = "Mi ganado",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = PrimaryGreenAccent
                )

                Text(
                    text = "${filteredAnimals.size} animales activos",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = NeutralSlate
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. BUSCADOR
            item {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Buscar por código o nombre",
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
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F5F8),
                        unfocusedContainerColor = Color(0xFFF3F5F8),
                        disabledContainerColor = Color(0xFFF3F5F8),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 3. BOTÓN REGISTRAR ANIMAL
            item {
                Button(
                    onClick = onNavigateToRegisterAnimal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreenAccent,
                        contentColor = Color.White
                    )
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
                            text = "Registrar animal",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. CHIPS DE FILTRO POR ESPECIES (HORIZONTAL)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimalCategory.values().forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) PrimaryGreenAccent else Color(0xFFF3F5F8))
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.label,
                                fontFamily = Poppins,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else NeutralSlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 5. TARJETAS DE LA LISTA DE ANIMALES
            items(filteredAnimals, key = { it.id }) { animal ->
                AnimalCardItem(
                    animal = animal,
                    onClick = { onNavigateToAnimalDetail(animal.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// COMPONENTE: Tarjeta Individual de Animal
@Composable
private fun AnimalCardItem(
    animal: Animal,
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
            // Caja del Ícono
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F8EE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = animal.iconRes),
                    contentDescription = animal.name,
                    tint = PrimaryGreenAccent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Información del Animal
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = animal.name,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${animal.code} · ${animal.typeLabel}",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = NeutralSlate
                )
            }

            // Insignia Estado "Activo"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F8EE))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = animal.status,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PrimaryGreenAccent
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Flecha Derecha
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "Ver detalle",
                tint = NeutralSlate,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}