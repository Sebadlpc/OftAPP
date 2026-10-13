package com.example.oftapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores basados en tu diseño
val OftPrimary = Color(0xFF00647C) // Azul petróleo/Teal
val OftBackground = Color(0xFFFFFFFF) // Blanco puro
val OftBorder = Color(0xFFE0E0E0) // Gris claro para bordes
val OftTextDark = Color(0xFF1E1E1E) // Texto principal
val OftTextMuted = Color(0xFF5C5C5C) // Texto secundario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userRole: String = "Médico",
    onNavigateToRegister: () -> Unit = {},
    onNavigateToUpload: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    Scaffold(
        containerColor = OftBackground, // Forzar fondo blanco
        topBar = {
            TopAppBar(
                title = { Text("OftApp", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { /* Acción de notificaciones */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = "Notificaciones", tint = Color.White)
                        // Punto de alerta
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCA5A5))
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OftPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToRegister,
                containerColor = OftPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OftPrimary,
                        selectedTextColor = OftPrimary,
                        indicatorColor = Color(0xFFDDF2F9),
                        unselectedIconColor = OftTextMuted,
                        unselectedTextColor = OftTextMuted
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSearch,
                    icon = { Icon(Icons.Outlined.Search, contentDescription = "Búsqueda") },
                    label = { Text("Búsqueda") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.Outlined.History, contentDescription = "Historial") },
                    label = { Text("Historial") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Welcome Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = OftPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Bienvenida,", fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                        Text("Dra. Carla Ruiz", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("$userRole · Sede Norte", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp), color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            // 2. Acciones Rápidas
            item {
                Text("Acciones rápidas", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = OftPrimary, modifier = Modifier.padding(bottom = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButton("Registrar", Icons.Default.NoteAdd, onNavigateToRegister)
                    QuickActionButton("Subir", Icons.Default.Upload, onNavigateToUpload)
                    QuickActionButton("Buscar", Icons.Default.Search, onNavigateToSearch)
                    QuickActionButton("Historial", Icons.Default.Assignment, onNavigateToHistory)
                }
            }

            // 3. Resumen de Exámenes
            item {
                Text("Resumen de exámenes", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = OftPrimary, modifier = Modifier.padding(bottom = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryTile(Modifier.weight(1f), Icons.Default.Schedule, 1, "Pendientes", Color(0xFF5A7184), Color(0xFFE1E8EE))
                    SummaryTile(Modifier.weight(1f), Icons.Default.CheckCircleOutline, 2, "Validados", Color(0xFF1B8038), Color(0xFFA8E5B3))
                    SummaryTile(Modifier.weight(1f), Icons.Default.WarningAmber, 1, "Alertas", Color(0xFFD32F2F), Color(0xFFF9D6D1))
                }
            }

            // 4. Recientes
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recientes", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = OftPrimary)
                    Text(
                        "Ver todos",
                        fontSize = 13.sp,
                        color = OftPrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onNavigateToSearch() }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Examen Dummy 1
                RecentExamCard("Campimetría", "PAC-04821 · OD · 18 Sep 2026", "Validado", Color(0xFF1B8038), Color(0xFFA8E5B3))
                Spacer(modifier = Modifier.height(8.dp))
                // Puedes agregar más tarjetas aquí
            }

            item { Spacer(modifier = Modifier.height(80.dp)) } // Espacio extra para el FAB
        }
    }
}

@Composable
fun QuickActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OftBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(78.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDDF2F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = OftPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OftTextDark)
        }
    }
}

@Composable
fun SummaryTile(modifier: Modifier, icon: ImageVector, value: Int, label: String, iconColor: Color, bgColor: Color) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OftBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OftTextDark)
            Text(label, fontSize = 12.sp, color = OftTextMuted)
        }
    }
}

@Composable
fun RecentExamCard(type: String, details: String, status: String, statusColor: Color, statusBg: Color) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OftBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(type, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OftTextDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(details, fontSize = 13.sp, color = OftTextMuted)
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Puntito de estado
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(status, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}