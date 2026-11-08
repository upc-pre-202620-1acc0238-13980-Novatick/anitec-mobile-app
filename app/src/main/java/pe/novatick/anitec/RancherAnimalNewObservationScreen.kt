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
import androidx.compose.foundation.shape.CircleShape
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
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.components.CattleHeaderShape
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.NeutralSlate
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent

@Composable
fun RancherAnimalNewObservationScreen(
    animalName: String = "Luna",
    animalCode: String = "BOV-024 · Bovino",
    onNavigateBack: () -> Unit
) {
    var observationText by remember { mutableStateOf("") }

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

            // TOP BAR: BOTÓN ATRÁS Y TÍTULO "Agregar observación"
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Botón Atrás (ic_arrow_left)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F8EE))
                            .clickable { onNavigateBack() }
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

                    // Título de la vista
                    Text(
                        text = "Agregar observación",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = PrimaryGreenAccent
                    )
                }
            }

            // TARJETA DE RESUMEN DEL ANIMAL (ÍCONO + NOMBRE Y CÓDIGO)
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
                            Text(
                                text = animalName,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = PrimaryGreenAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = animalCode,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = NeutralSlate
                            )
                        }
                    }
                }
            }

            // BANNER INFORMATIVO (ic_document)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1FBF5))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_document),
                            contentDescription = null,
                            tint = PrimaryGreenAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Describe lo que has notado en tu animal. Esta nota quedará en su ficha.",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = NeutralDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // CAMPO MULTILÍNEA: OBSERVACIÓN *
            item {
                Text(
                    text = "Observación *",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = observationText,
                    onValueChange = { observationText = it },
                    placeholder = {
                        Text(
                            text = "Desde ayer come menos y permanece separado del grupo",
                            fontFamily = Poppins,
                            color = NeutralSlate,
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F5F8),
                        unfocusedContainerColor = Color(0xFFF3F5F8),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )
            }

            // BOTONES GUARDAR OBSERVACIÓN Y CANCELAR
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onNavigateBack() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Guardar observación",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Enlace de texto "Cancelar"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancelar",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = NeutralSlate,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable { onNavigateBack() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}