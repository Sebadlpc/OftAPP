package com.example.oftapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.oftapp.ui.viewmodel.HistorialClinicoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialClinicoScreen(
    initialPatientName: String? = null,
    viewModel: HistorialClinicoViewModel = viewModel(),
    userRole: String = "Tecnólogo Médico",
    onSelectExam: (String) -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPatient = userRole.equals("Paciente", ignoreCase = true)

    // Configura el rol y restricciones de privacidad en el ViewModel
    LaunchedEffect(userRole) {
        viewModel.setUserRole(userRole, "Juan Pérez")
    }

    // Si recibimos un parámetro de paciente inicial por navegación, aplicarlo si no es paciente restringido
    LaunchedEffect(initialPatientName) {
        if (!initialPatientName.isNullOrBlank() && !isPatient) {
            viewModel.selectPatient(initialPatientName)
        }
    }

    val listState = rememberLazyListState()

    // Detección de scroll para Lazy Loading por bloques sucesivos
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.hasMoreItems && !uiState.isLoadingMore && !uiState.isLoading) {
            viewModel.loadNextBlock()
        }
    }

    Scaffold(
        containerColor = BlancoClinico,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Historial Clínico",
                            fontWeight = FontWeight.Bold,
                            color = BlancoClinico,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isPatient) "Mi Expediente Oftalmológico" else "Línea de Tiempo del Paciente",
                            color = BlancoClinico.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = BlancoClinico
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulClinico)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = BlancoClinico, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
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
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.History, contentDescription = "Historial") },
                    label = { Text("Historial") },
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
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Buscador dinámico de paciente (reemplaza al Dropdown)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isPatient) "Paciente Identificado:" else "Buscar Paciente:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrisAzulado,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = if (isPatient) "Juan Pérez" else uiState.searchQuery,
                        onValueChange = { newQuery ->
                            if (!isPatient) {
                                viewModel.onSearchQueryChange(newQuery)
                            }
                        },
                        placeholder = {
                            Text(
                                text = "Buscar por nombre (ej. Juan, María)...",
                                color = GrisAzulado,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar paciente",
                                tint = AzulClinico
                            )
                        },
                        trailingIcon = {
                            if (!isPatient && uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = GrisAzulado
                                    )
                                }
                            }
                        },
                        readOnly = isPatient,
                        enabled = !isPatient,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulClinico,
                            unfocusedBorderColor = GrisAzulado.copy(alpha = 0.5f),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = AzulClinico.copy(alpha = 0.05f),
                            disabledTextColor = TextoOscuro,
                            disabledBorderColor = AzulClinico.copy(alpha = 0.3f),
                            focusedTextColor = TextoOscuro,
                            unfocusedTextColor = TextoOscuro
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextoOscuro
                        )
                    )
                }
            }

            // 2. Tarjeta Resumen Responsiva
            item {
                val nombrePaciente = when {
                    isPatient -> "Juan Pérez"
                    uiState.selectedPatient.isNotBlank() -> uiState.selectedPatient
                    uiState.searchQuery.isNotBlank() -> uiState.searchQuery
                    else -> "Todos los Pacientes"
                }
                val inicial = nombrePaciente.take(1).uppercase()
                val totalExams = uiState.totalExamsCount

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AzulClinico.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, AzulClinico.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(AzulClinico),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = inicial,
                                    color = BlancoClinico,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = nombrePaciente,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextoOscuro,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isPatient) "Ficha personal exclusiva" else "Ficha clínica vinculada",
                                    fontSize = 12.sp,
                                    color = GrisAzulado
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = AzulClinico,
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Text(
                                    text = if (totalExams == 1) "1 examen" else "$totalExams exámenes",
                                    color = BlancoClinico,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = AzulClinico.copy(alpha = 0.15f)
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Seguimiento Oftalmológico:",
                                color = AzulClinico,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Línea de tiempo con carga por bloques perezosos de exámenes clínicos.",
                                color = TextoOscuro,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 3. Encabezado de la línea de tiempo
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AzulClinico,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Evolución Cronológica",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextoOscuro
                        )
                    }
                    Text(
                        text = "Recientes primero",
                        fontSize = 11.sp,
                        color = GrisAzulado
                    )
                }
            }

            // 4. Contenido de la Línea de Tiempo con Lazy Loading por bloques
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AzulClinico)
                    }
                }
            } else if (uiState.patientExams.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                text = "No se encontraron exámenes para el criterio seleccionado",
                                color = GrisAzulado,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(uiState.patientExams, key = { it.id }) { exam ->
                    TimelineExamCard(
                        exam = exam,
                        onClick = { onSelectExam(exam.id) }
                    )
                }

                // Elemento de carga perezosa al pie del LazyColumn
                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = AzulClinico,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                } else if (uiState.hasMoreItems) {
                    item {
                        OutlinedButton(
                            onClick = { viewModel.loadNextBlock() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AzulClinico.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulClinico)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cargar más exámenes (${uiState.patientExams.size} de ${uiState.totalExamsCount})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun TimelineExamCard(exam: ExamEntity, onClick: () -> Unit) {
    val isValidated = exam.status.equals("Validado", ignoreCase = true) ||
            exam.status.equals("Completado", ignoreCase = true)
    val statusColor = if (isValidated) VerdeSalud else RojoAlerta
    val statusBgColor = if (isValidated) VerdeSalud.copy(alpha = 0.12f) else RojoAlerta.copy(alpha = 0.12f)
    val statusIcon = if (isValidated) Icons.Default.CheckCircle else Icons.Default.ErrorOutline

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-CL"))
    val fechaTexto = try {
        dateFormat.format(exam.date)
    } catch (_: Exception) {
        "N/D"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Columna de la línea y el punto de la línea de tiempo
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(AzulClinico.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(statusColor)
                    .border(3.dp, BlancoClinico, CircleShape)
            )
        }

        // Tarjeta del examen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Fila Superior: Fecha y Estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = AzulClinico,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = fechaTexto,
                            color = AzulClinico,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBgColor)
                            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = exam.status,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fila Media: Título y Ojo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exam.examType,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextoOscuro
                        )
                        Text(
                            text = "${exam.branch} · ${exam.doctorName}",
                            fontSize = 11.sp,
                            color = GrisAzulado
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GrisAzulado.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = exam.eye,
                            color = AzulClinico,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Observaciones
                if (exam.notes.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GrisAzulado.copy(alpha = 0.08f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "\"${exam.notes}\"",
                            fontSize = 12.sp,
                            color = TextoOscuro,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                HorizontalDivider(color = GrisAzulado.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                // Fila Inferior: Acción y enlace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = AzulClinico,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (exam.fileUri != null) "Informe adjunto disponible" else "Ficha digital",
                            fontSize = 11.sp,
                            color = AzulClinico,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ver detalle",
                            fontSize = 11.sp,
                            color = AzulClinico,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = AzulClinico,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
