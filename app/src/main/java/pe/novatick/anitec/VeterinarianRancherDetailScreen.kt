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

@Composable
fun VeterinarianRancherDetailScreen(
    rancherName: String = "Juan García",
    rancherEmail: String = "juan.garcia@email.com",
    joinedDate: String = "16 jun 2025",
    establishment: String = "Fundo Los Álamos",
    department: String = "Cajamarca",
    province: String = "Cajamarca",
    district: String = "Baños del Inca",
    address: String = "Sector El Mirador, parcela 12",
    directionsReference: String = "Ingresar por el camino junto a la escuela del sector y continuar hasta el portón verde. El ganado se encuentra en el corral detrás de la vivienda. Coordinar con Juan antes de la visita.",
    onBackClick: () -> Unit,
    onUnlinkClick: () -> Unit,
    onNavigateToAnimals: () -> Unit
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

            // TOP BAR: BOTÓN ATRÁS + TÍTULO Y SUBTÍTULO
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
                            text = rancherName,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            color = PrimaryGreenAccent
                        )
                        Text(
                            text = "Ganadero vinculado",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = NeutralSlate
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // TARJETA DE RESUMEN DEL GANADERO
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
                        // Avatar Circular con Iniciales
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF50C878)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JG",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = rancherName,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = NeutralDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Vinculado desde el $joinedDate - $rancherEmail",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                color = NeutralSlate
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Badge de Estado Acceso activo
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE8F8EE))
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Acceso activo",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = PrimaryGreenAccent
                                )
                            }
                        }
                    }
                }
            }

            // BOTÓN DESVINCULAR
            item {
                OutlinedButton(
                    onClick = onUnlinkClick,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFEF5350)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Desvincular",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // TARJETA DE DETALLES DE UBICACIÓN / ESTABLECIMIENTO (5 FILAS)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Fila 1: Predio o establecimiento (ic_farm)
                        RancherDetailInfoRow(
                            iconRes = R.drawable.ic_farm,
                            label = "Predio o establecimiento",
                            value = establishment
                        )

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        // Fila 2: Departamento / Región (ic_map)
                        RancherDetailInfoRow(
                            iconRes = R.drawable.ic_map,
                            label = "Departamento / Región",
                            value = department
                        )

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        // Fila 3: Provincia (ic_location)
                        RancherDetailInfoRow(
                            iconRes = R.drawable.ic_location,
                            label = "Provincia",
                            value = province
                        )

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        // Fila 4: Distrito (ic_pin)
                        RancherDetailInfoRow(
                            iconRes = R.drawable.ic_pin,
                            label = "Distrito",
                            value = district
                        )

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        // Fila 5: Dirección o sector (ic_route)
                        RancherDetailInfoRow(
                            iconRes = R.drawable.ic_route,
                            label = "Dirección o sector",
                            value = address
                        )
                    }
                }
            }

            // BANNER: REFERENCIA PARA LLEGAR (TIRA VERDE LATERAL)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1FBF5)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Tira Verde Lateral
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(110.dp)
                                .background(PrimaryGreenAccent)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "Referencia para llegar",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PrimaryGreenAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = directionsReference,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = NeutralDark,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // BOTÓN VER ANIMALES (ic_edit)
            item {
                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onNavigateToAnimals,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ver animales",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// COMPONENTE: Fila de Información con Ícono Verde
@Composable
private fun RancherDetailInfoRow(
    iconRes: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = PrimaryGreenAccent,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column {
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
                fontSize = 14.sp,
                color = PrimaryGreenAccent
            )
        }
    }
}