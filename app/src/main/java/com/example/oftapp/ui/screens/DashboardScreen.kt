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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.ui.theme.AzulClinico
import com.example.oftapp.ui.theme.BlancoClinico
import com.example.oftapp.ui.theme.GrisAzulado
import com.example.oftapp.ui.theme.RojoAlerta
import com.example.oftapp.ui.theme.TextoOscuro
import com.example.oftapp.ui.theme.VerdeSalud
import com.example.oftapp.ui.viewmodel.DashboardViewModel
import com.example.oftapp.ui.viewmodel.FhirObservationDisplayItem
import com.example.oftapp.util.rememberTextToSpeechHelper
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
    val snackbarHostState = remember { SnackbarHostState() }
    val ttsHelper = rememberTextToSpeechHelper()
    val isSpeaking by ttsHelper.isSpeaking.collectAsState()

    LaunchedEffect(userRole) {
        viewModel.setUserRole(userRole, "Juan Pérez")
    }

    // Feedback visual mediante Snackbar al sincronizar
    LaunchedEffect(uiState.syncMessage) {
        val message = uiState.syncMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(
        containerColor = BlancoClinico,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("OftApp", fontWeight = FontWeight.Bold, color = BlancoClinico) },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                ttsHelper.stop()
                            } else {
                                val summarySpeech = buildString {
                                    append("Resumen clínico de OftApp. ")
                                    append("Total de exámenes registrados: ${uiState.totalCount}. ")
                                    append("Exámenes pendientes de validación: ${uiState.pendingCount}. ")
                                    append("Exámenes validados clínicamente: ${uiState.validatedCount}. ")
                                    val recent = uiState.recentExams.firstOrNull()
                                    if (recent != null) {
                                        append("Atención más reciente: ${recent.examType} para el paciente ${recent.patientName}, ojo evaluado ${recent.eye}, estado ${recent.status}. ")
                                        if (recent.notes.isNotBlank()) {
                                            append("Observaciones: ${recent.notes}.")
                                        }
                                    }
                                }
                                ttsHelper.speak(summarySpeech)
                            }
                        },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = if (isSpeaking) "Detener resumen audible" else "Escuchar resumen general de exámenes en voz alta"
                            }
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = BlancoClinico
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulClinico
                )
            )
        },
        floatingActionButton = {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Welcome Banner
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

            // 2. Banner de Conectividad y Sincronización Automática Offline
            item {
                NetworkSyncBanner(
                    isOnline = uiState.isOnline,
                    pendingCount = uiState.pendingSyncCount,
                    isSyncing = uiState.isSyncing,
                    onSyncClick = { viewModel.syncPendingExams() }
                )
            }

            // 3. Resumen de Métricas
            item {
                Text(
                    text = if (isPatient) "Mis exámenes registrados" else "Resumen general de exámenes",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = AzulClinico,
                    modifier = Modifier.padding(bottom = 4.dp)
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

            // 4. Lista de Exámenes Recientes
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
                    TextButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Ver listado completo de exámenes clínicos"
                            }
                    ) {
                        Text(
                            text = "Ver todos",
                            fontSize = 13.sp,
                            color = AzulClinico,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            // 5. Sección de Interoperabilidad HL7 HAPI FHIR
            item {
                FhirInteroperabilitySection(
                    observations = uiState.fhirObservations,
                    isLoading = uiState.fhirLoading,
                    errorMessage = uiState.fhirError,
                    onRefresh = { viewModel.loadFhirObservations() }
                )
            }

            item { Spacer(modifier = Modifier.height(70.dp)) }
        }
    }
}

@Composable
fun NetworkSyncBanner(
    isOnline: Boolean,
    pendingCount: Int,
    isSyncing: Boolean,
    onSyncClick: () -> Unit
) {
    val bgColor = when {
        !isOnline -> RojoAlerta.copy(alpha = 0.08f)
        pendingCount > 0 -> AzulClinico.copy(alpha = 0.08f)
        else -> VerdeSalud.copy(alpha = 0.08f)
    }

    val borderColor = when {
        !isOnline -> RojoAlerta.copy(alpha = 0.35f)
        pendingCount > 0 -> AzulClinico.copy(alpha = 0.35f)
        else -> VerdeSalud.copy(alpha = 0.35f)
    }

    val iconColor = when {
        !isOnline -> RojoAlerta
        pendingCount > 0 -> AzulClinico
        else -> VerdeSalud
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = when {
                        !isOnline -> Icons.Default.CloudOff
                        pendingCount > 0 -> Icons.Default.CloudQueue
                        else -> Icons.Default.CloudDone
                    },
                    contentDescription = "Estado de Red",
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isOnline) "En línea · Spring Boot" else "Modo Offline",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                    Text(
                        text = when {
                            !isOnline && pendingCount > 0 -> "$pendingCount examen(es) guardados localmente"
                            !isOnline -> "Sin conexión. Datos seguros en Room."
                            pendingCount > 0 -> "$pendingCount pendiente(s) por sincronizar"
                            else -> "Todos los exámenes sincronizados"
                        },
                        fontSize = 11.sp,
                        color = GrisAzulado
                    )
                }
            }

            Button(
                onClick = onSyncClick,
                enabled = !isSyncing,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulClinico,
                    contentColor = BlancoClinico,
                    disabledContainerColor = AzulClinico.copy(alpha = 0.7f),
                    disabledContentColor = BlancoClinico
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = if (isSyncing) "Sincronizando exámenes pendientes con el servidor" else "Sincronizar exámenes locales con el servidor"
                    }
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = BlancoClinico,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sincronizando...", fontSize = 11.sp, color = BlancoClinico)
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sincronizar",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sincronizar", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun FhirInteroperabilitySection(
    observations: List<FhirObservationDisplayItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlancoClinico),
        border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "Interoperabilidad HL7 FHIR",
                        tint = AzulClinico,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interoperabilidad HL7 HAPI FHIR",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextoOscuro
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Recargar observaciones clínicas HL7 FHIR"
                        }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AzulClinico,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = GrisAzulado,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Text(
                text = "Observaciones clínicas estándar consultadas desde https://hapi.fhir.org/baseR4",
                fontSize = 11.sp,
                color = GrisAzulado,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = "Aviso interoperabilidad: $errorMessage",
                    color = GrisAzulado,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else if (observations.isEmpty() && !isLoading) {
                Text(
                    text = "Presione el botón de recarga para consultar observaciones FHIR.",
                    color = GrisAzulado,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    observations.forEach { item ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = AzulClinico.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextoOscuro
                                    )
                                    Text(
                                        text = "Valor: ${item.value} · Fecha: ${item.date}",
                                        fontSize = 11.sp,
                                        color = GrisAzulado
                                    )
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(item.status, fontSize = 11.sp) },
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                        .semantics {
                                            role = Role.Button
                                            contentDescription = "Estado de observación clínica: ${item.status}"
                                        }
                                )
                            }
                        }
                    }
                }
            }
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
            .defaultMinSize(minHeight = 48.dp)
            .clickable { onClick() }
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "Examen de ${exam.examType} para el paciente ${exam.patientName}, ojo evaluado ${exam.eye}, fecha $fechaFormateada, estado ${exam.status}"
            },
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exam.examType,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = TextoOscuro
                    )
                    if (exam.syncStatus == "PENDIENTE") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(Offline)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = RojoAlerta
                        )
                    }
                }
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
