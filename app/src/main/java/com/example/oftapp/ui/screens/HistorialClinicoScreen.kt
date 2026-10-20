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
import androidx.compose.foundation.border


// Colores de la App
val AzulClinico = Color(0xFF006689)
val GrisAzulado = Color(0xFF4F616E)
val BlancoClinico = Color(0xFFF8FDFF)
val TextoOscuro = Color(0xFF191C1E)
val VerdeSalud = Color(0xFF006E36)
val RojoAlerta = Color(0xFFB02F00)
val BordeGris = Color(0xFFC0C8CD)
val FondoTarjeta = Color(0xFFC3E8FF).copy(alpha = 0.4f)
val FondoObservaciones = Color(0xFFEBF2F6)

// Modelos de prueba para que la pantalla compile
data class PatientMock(val id: String, val name: String, val rut: String, val age: Int, val gender: String, val diagnosis: String)
data class ExamMock(val id: String, val date: String, val time: String, val status: String, val examType: String, val branch: String, val technologist: String, val eye: String, val observations: String, val hasFile: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialClinicoScreen(
    onSelectExam: (String) -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // Datos de prueba
    val patients = listOf(
        PatientMock("PAT-001", "María González", "12.345.678-9", 65, "F", "Glaucoma Crónico Simple OD. Sospecha OS."),
        PatientMock("PAT-002", "Juan Pérez", "9.876.543-2", 72, "M", "Catarata Senil bilateral.")
    )
    val exams = listOf(
        ExamMock("EX-1", "12 Oct 2026", "10:30", "Validado", "Campo Visual Computarizado", "Sede Norte", "Tec. A. Rojas", "OD", "Defecto arciforme superior consistente con daño glaucomatoso.", true),
        ExamMock("EX-2", "05 Sep 2026", "09:15", "Pendiente", "Tonometría", "Sede Centro", "Tec. M. Silva", "Ambos", "PIO OD: 18mmHg, OS: 16mmHg.", false)
    )

    var selectedPatient by remember { mutableStateOf(patients[0]) }
    var expandedDropdown by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BlancoClinico,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Historial Clínico", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text("Línea de Tiempo del Paciente", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Menú lateral */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulClinico)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(selected = false, onClick = onNavigateToHome, icon = { Icon(Icons.Outlined.Home, null) }, label = { Text("Inicio") })
                NavigationBarItem(selected = false, onClick = onNavigateToSearch, icon = { Icon(Icons.Outlined.Search, null) }, label = { Text("Búsqueda") })
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.History, null) },
                    label = { Text("Historial") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AzulClinico, selectedTextColor = AzulClinico, indicatorColor = Color(0xFFDDF2F9))
                )
                NavigationBarItem(selected = false, onClick = onNavigateToProfile, icon = { Icon(Icons.Outlined.Person, null) }, label = { Text("Perfil") })
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

            // 1. Selector de Paciente
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Paciente:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GrisAzulado, modifier = Modifier.padding(end = 8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "${selectedPatient.name} (${selectedPatient.rut})",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AzulClinico,
                                unfocusedBorderColor = GrisAzulado,
                                focusedContainerColor = BlancoClinico,
                                unfocusedContainerColor = BlancoClinico
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextoOscuro)
                        )
                        ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                            patients.forEach { patient ->
                                DropdownMenuItem(
                                    text = { Text("${patient.name} (${patient.rut})") },
                                    onClick = {
                                        selectedPatient = patient
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Tarjeta Resumen del Paciente
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    border = BorderStroke(1.dp, AzulClinico.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(CircleShape).background(AzulClinico),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(selectedPatient.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(selectedPatient.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuro)
                                    Text("RUT: ${selectedPatient.rut} · ${selectedPatient.age} años (${selectedPatient.gender})", fontSize = 12.sp, color = GrisAzulado)
                                }
                            }
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(AzulClinico).padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("${exams.size} exámenes", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = AzulClinico.copy(alpha = 0.15f))

                        Text("Diagnóstico Oftalmológico Base:", color = AzulClinico, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(selectedPatient.diagnosis, color = TextoOscuro, fontWeight = FontWeight.Medium, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = AzulClinico, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Evolución Cronológica de Exámenes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextoOscuro)
                    }
                    Text("Más reciente primero", fontSize = 11.sp, color = GrisAzulado)
                }
            }

            // 4. Línea de Tiempo (Exámenes)
            items(exams) { exam ->
                TimelineExamCard(exam = exam, onClick = { onSelectExam(exam.id) })
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun TimelineExamCard(exam: ExamMock, onClick: () -> Unit) {
    val isStatusValid = exam.status == "Validado"
    val statusColor = if (isStatusValid) VerdeSalud else RojoAlerta
    val statusBgColor = if (isStatusValid) Color(0xFF9EF5B4).copy(alpha = 0.6f) else Color(0xFFFFDAD3).copy(alpha = 0.6f)
    val statusIcon = if (isStatusValid) Icons.Default.CheckCircle else Icons.Default.ErrorOutline

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Columna de la línea y el punto
        Box(modifier = Modifier.width(24.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
            // Línea vertical
            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(AzulClinico.copy(alpha = 0.3f)))
            // Punto de estado
            Box(modifier = Modifier
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
            colors = CardDefaults.cardColors(containerColor = BlancoClinico),
            border = BorderStroke(1.dp, BordeGris.copy(alpha = 0.6f)),
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
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AzulClinico, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(exam.date, color = AzulClinico, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("(${exam.time})", color = GrisAzulado, fontSize = 11.sp)
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
                        Text(exam.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                        Text(exam.examType, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextoOscuro)
                        Text("${exam.branch.split("-")[0]} · ${exam.technologist}", fontSize = 11.sp, color = GrisAzulado)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FondoObservaciones)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(exam.eye, color = AzulClinico, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Observaciones
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FondoObservaciones.copy(alpha = 0.6f))
                        .padding(8.dp)
                ) {
                    Text("\"${exam.observations}\"", fontSize = 12.sp, color = TextoOscuro, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = BordeGris.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                // Fila Inferior: Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = AzulClinico, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (exam.hasFile) "informe_adjunto.pdf" else "Ver informe digital", fontSize = 11.sp, color = AzulClinico, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Revisar estudio", fontSize = 11.sp, color = AzulClinico, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AzulClinico, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}