package com.example.oftapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.ui.theme.AzulClinico
import com.example.oftapp.ui.theme.BlancoClinico
import com.example.oftapp.ui.theme.GrisAzulado
import com.example.oftapp.ui.theme.TextoOscuro
import com.example.oftapp.ui.theme.VerdeSalud
import com.example.oftapp.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    userRole: String = "Médico",
    onNavigateToRegister: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPatient = userRole.equals("Paciente", ignoreCase = true)

    LaunchedEffect(userRole) {
        viewModel.setUserRole(userRole, "Juan Pérez")
    }

    Scaffold(
        containerColor = BlancoClinico,
        topBar = {
            TopAppBar(
                title = { Text("OftApp", fontWeight = FontWeight.Bold, color = BlancoClinico) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulClinico
                )
            )
        },
        floatingActionButton = {
            // El botón de registro rápido es exclusivo de personal facultativo/clínico
            if (!isPatient) {
                FloatingActionButton(
                    onClick = onNavigateToRegister,
                    containerColor = AzulClinico,
                    contentColor = BlancoClinico,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Registrar nuevo examen")
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = BlancoClinico,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulClinico,
                        selectedTextColor = AzulClinico,
                        indicatorColor = AzulClinico.copy(alpha = 0.15f),
                        unselectedIconColor = GrisAzulado,
                        unselectedTextColor = GrisAzulado
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSearch,
                    icon = { Icon(Icons.Outlined.Search, contentDescription = "Búsqueda") },
                    label = { Text("Búsqueda") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = GrisAzulado,
                        unselectedTextColor = GrisAzulado
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.Outlined.History, contentDescription = "Historial") },
                    label = { Text("Historial") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = GrisAzulado,
                        unselectedTextColor = GrisAzulado
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = GrisAzulado,
                        unselectedTextColor = GrisAzulado
                    )
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

            // 1. Welcome Banner adaptado al rol y usuario
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AzulClinico,
                        contentColor = BlancoClinico
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Bienvenido/a,", fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                        Text(
                            text = when {
                                userRole.contains("Médico", ignoreCase = true) -> "Dra. Carla Ruiz"
                                userRole.contains("Tecnólogo", ignoreCase = true) -> "Lic. Andrés Gómez"
                                userRole.contains("Admin", ignoreCase = true) -> "Carlos Vega"
                                else -> "Juan Pérez"
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPatient) "Portal Paciente · Mis Atenciones" else "$userRole · Sede Central",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                            color = BlancoClinico.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // 2. Resumen de Métricas (Totales, Pendientes, Validados)
            item {
                Text(
                    text = if (isPatient) "Mis exámenes registrados" else "Resumen general de exámenes",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = AzulClinico,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Assessment,
                        value = uiState.totalCount,
                        label = if (isPatient) "Mis Totales" else "Totales",
                        iconColor = AzulClinico,
                        bgColor = AzulClinico.copy(alpha = 0.12f)
                    )
                    SummaryTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Schedule,
                        value = uiState.pendingCount,
                        label = "Pendientes",
                        iconColor = GrisAzulado,
                        bgColor = GrisAzulado.copy(alpha = 0.12f)
                    )
                    SummaryTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CheckCircleOutline,
                        value = uiState.validatedCount,
                        label = "Validados",
                        iconColor = VerdeSalud,
                        bgColor = VerdeSalud.copy(alpha = 0.12f)
                    )
                }
            }

            // 3. Lista de Exámenes Recientes
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPatient) "Mis atenciones recientes" else "Exámenes recientes",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = AzulClinico
                    )
                    Text(
                        text = "Ver todos",
                        fontSize = 13.sp,
                        color = AzulClinico,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onNavigateToSearch() }
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AzulClinico)
                    }
                }
            } else if (uiState.recentExams.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BlancoClinico),
                        border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isPatient) "No tienes exámenes registrados actualmente" else "No hay exámenes registrados recientemente",
                                color = GrisAzulado,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(uiState.recentExams, key = { it.id }) { exam ->
                    RecentExamCard(
                        exam = exam,
                        onClick = { onNavigateToDetail(exam.id) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SummaryTile(
    modifier: Modifier,
    icon: ImageVector,
    value: Int,
    label: String,
    iconColor: Color,
    bgColor: Color
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = BlancoClinico),
        border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.2f)),
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
            Text(value.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
            Text(label, fontSize = 12.sp, color = GrisAzulado)
        }
    }
}

@Composable
fun RecentExamCard(
    exam: ExamEntity,
    onClick: () -> Unit
) {
    val isValidated = exam.status.equals("Validado", ignoreCase = true) ||
            exam.status.equals("Completado", ignoreCase = true)
    val statusColor = if (isValidated) VerdeSalud else GrisAzulado
    val statusBg = if (isValidated) VerdeSalud.copy(alpha = 0.12f) else GrisAzulado.copy(alpha = 0.12f)

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-CL"))
    val fechaFormateada = try {
        dateFormat.format(exam.date)
    } catch (_: Exception) {
        "Fecha no disponible"
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.outlinedCardColors(containerColor = BlancoClinico),
        border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.examType,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextoOscuro
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${exam.patientName} · ${exam.eye} · $fechaFormateada",
                    fontSize = 13.sp,
                    color = GrisAzulado
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = exam.status,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
