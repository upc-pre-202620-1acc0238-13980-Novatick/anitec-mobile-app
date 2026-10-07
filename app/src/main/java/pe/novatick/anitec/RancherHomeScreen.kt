package pe.novatick.anitec

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.components.RancherHeaderShape
import pe.novatick.anitec.ui.navigation.RancherTab
import pe.novatick.anitec.ui.theme.NeutralDark
import pe.novatick.anitec.ui.theme.NeutralSlate
import pe.novatick.anitec.ui.theme.Poppins
import pe.novatick.anitec.ui.theme.PrimaryGreenAccent

@Composable
fun RancherHomeScreen(
    onNavigateToCattle: () -> Unit,
    onNavigateToLinkedVets: () -> Unit,
    onNavigateToRegisterAnimal: () -> Unit,
    onNavigateToInviteVets: () -> Unit,
    onNavigateToVisits: () -> Unit,
    onNavigateToVisitDetail: () -> Unit,
    onNavigateToIndicationDetail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
    ) {
        // 1. ENCABEZADO VERDE CURVO CON FOTO Y DATOS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RancherHeaderShape)
                .background(PrimaryGreenAccent)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MI FINCA",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Hola, Juan",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Revisa las novedades de tu ganado",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }

                // Imagen Avatar del Ganadero
                Image(
                    painter = painterResource(id = R.drawable.profile_rancher_user),
                    contentDescription = "Foto de perfil Juan",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. TARJETAS DE MÉTRICAS (Animales Activos y Vet. Vinculados)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tarjeta: Animales Activos
            MetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_cow,
                value = "12",
                label = "animales activos",
                onClick = onNavigateToCattle
            )

            // Tarjeta: Veterinarios Vinculados
            MetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_stethoscope,
                value = "2",
                label = "veterinarios vinculados",
                onClick = onNavigateToLinkedVets
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. SECCIÓN: ACCESOS RÁPIDOS
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Accesos rápidos",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NeutralDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Registrar Animal
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_plus,
                    text = "Registrar animal",
                    onClick = onNavigateToRegisterAnimal
                )

                // Botón Invitar Veterinario
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_mail,
                    text = "Invitar veterinario",
                    onClick = onNavigateToInviteVets
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. SECCIÓN: PRÓXIMAS VISITAS
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Próximas visitas",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NeutralDark
                )

                Text(
                    text = "Ver visitas",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PrimaryGreenAccent,
                    modifier = Modifier.clickable { onNavigateToVisits() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visit Item 1
            VisitCardItem(
                day = "18",
                month = "JUN",
                title = "Luna · Control general",
                timeAndVet = "10:30 · Dra. María López",
                onClick = onNavigateToVisitDetail
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Visit Item 2
            VisitCardItem(
                day = "18",
                month = "JUN",
                title = "Mora · Vacunación",
                timeAndVet = "14:00 · Dra. María López",
                onClick = onNavigateToVisitDetail
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. SECCIÓN: INDICACIONES RECIENTES
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Indicaciones recientes",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NeutralDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Indication Card Item
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToIndicationDetail() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícono Documento Ámbar
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF8E7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_document),
                            contentDescription = "Documento",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cuidados posteriores · Luna",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NeutralDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Dra. María López · 16 jun",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = NeutralSlate
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Controlar apetito y mantener hidratación...",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = NeutralSlate
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "Ir",
                        tint = NeutralSlate,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// COMPONENTE: Tarjeta de Métrica Elevada
@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    iconRes: Int,
    value: String,
    label: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F8EE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = PrimaryGreenAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = NeutralDark
                )
                Text(
                    text = label,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = NeutralSlate,
                    lineHeight = 13.sp
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "Ir",
                tint = NeutralSlate,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// COMPONENTE: Botón de Acceso Rápido
@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    iconRes: Int,
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF1FBF5))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreenAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = text,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = NeutralDark,
                lineHeight = 16.sp
            )
        }
    }
}

// COMPONENTE: Tarjeta de Visita
@Composable
private fun VisitCardItem(
    day: String,
    month: String,
    title: String,
    timeAndVet: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fecha Badge Verde
            Column(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F8EE)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = PrimaryGreenAccent
                )
                Text(
                    text = month,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = PrimaryGreenAccent
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clock),
                        contentDescription = "Reloj",
                        tint = NeutralSlate,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeAndVet,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = NeutralSlate
                    )
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "Ir",
                tint = NeutralSlate,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}