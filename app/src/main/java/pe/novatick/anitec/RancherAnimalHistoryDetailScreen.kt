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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
fun RancherAnimalHistoryDetailScreen(
    animalName: String = "Luna",
    animalCode: String = "BOV-024 · Bovino",
    onBackClick: () -> Unit,
    onNavigateToIndicationDetail: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(35.dp)) }

            // TOP BAR: BOTÓN ATRÁS Y TÍTULO "Detalle de atención"
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

                    // Título de la pantalla
                    Text(
                        text = "Detalle de atención",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = PrimaryGreenAccent
                    )
                }
            }

            // TARJETA 1: RESUMEN DEL ANIMAL (ÍCONO + NOMBRE Y CÓDIGO)
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

            // TARJETA 2: METADATOS DE LA ATENCIÓN (3 FILAS)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Fila 1: Atención relacionada
                        MetaDataRow(
                            label = "Atención relacionada",
                            value = "Vacunación preventiva"
                        )

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        // Fila 2: Veterinaria responsable
                        MetaDataRow(
                            label = "Veterinaria responsable",
                            value = "Dra. María López"
                        )

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        // Fila 3: Fecha
                        MetaDataRow(
                            label = "Fecha",
                            value = "3 de marzo de 2025"
                        )
                    }
                }
            }

            // SECCIÓN 3: RESUMEN DE LA ATENCIÓN
            item {
                Text(
                    text = "Resumen de la atención",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F5F8))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Consulta de rutina para aplicación de vacunas del calendario sanitario del semestre. Animal en buen estado general.",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = NeutralDark,
                        lineHeight = 18.sp
                    )
                }
            }

            // SECCIÓN 4: TRATAMIENTOS REALIZADOS
            item {
                Text(
                    text = "Tratamientos realizados",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F5F8))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "No se registraron tratamientos en esta atención.",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = NeutralSlate
                    )
                }
            }

            // SECCIÓN 5: VACUNAS APLICADAS (TARJETA AZUL)
            item {
                Text(
                    text = "Vacunas aplicadas",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF62A1F7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEBF3FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_vaccine),
                                contentDescription = "Vacuna",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Vacuna contra fiebre aftosa",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF3B82F6)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Aplicada el 16 de junio de 2025",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = NeutralSlate
                            )
                        }
                    }
                }
            }

            // SECCIÓN 6: INDICACIONES DE CUIDADO (TARJETA ÁMBAR CLICKABLE)
            item {
                Text(
                    text = "Indicaciones de cuidado",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFBBF24)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToIndicationDetail() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFFF8E7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_document),
                                contentDescription = "Indicaciones",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ver indicaciones de cuidado",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Instrucciones del veterinario para cuidados posteriores",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = NeutralSlate
                            )
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = "Ir a indicaciones",
                            tint = NeutralDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// COMPONENTE: Fila Reutilizable para Metadatos de Atención
@Composable
private fun MetaDataRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
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