package com.example.oftapp.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oftapp.ui.theme.AzulClinico
import com.example.oftapp.ui.theme.BlancoClinico
import com.example.oftapp.ui.theme.GrisAzulado
import com.example.oftapp.ui.theme.RojoAlerta
import com.example.oftapp.ui.theme.TextoOscuro
import com.example.oftapp.ui.viewmodel.RegistroAtencionViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroAtencionScreen(
    viewModel: RegistroAtencionViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToUpload: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var expandedSede by remember { mutableStateOf(false) }
    val sedesDisponibles = listOf("Sede Central", "Sede Norte", "Sede Las Condes", "Sede Providencia")
    val tiposExamen = listOf("Campimetría", "Tonometría", "OCT Retinal", "Fundoscopía")
    val ojosDisponibles = listOf("OD (derecho)", "OI (izquierdo)", "Ambos Ojos (AO)")

    // Selector de archivo local opcional en el formulario
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var extractedName = "documento_adjunto.pdf"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    extractedName = cursor.getString(nameIndex)
                }
            }
            viewModel.onFileSelected(uri, extractedName)
        }
    }

    // Al registrar exitosamente, navegar automáticamente a la pantalla de carga de documentos con el ID del examen
    LaunchedEffect(uiState.isSuccess, uiState.createdExamId) {
        if (uiState.isSuccess && uiState.createdExamId != null) {
            val examId = uiState.createdExamId!!
            snackbarHostState.showSnackbar("Examen registrado exitosamente")
            delay(300)
            viewModel.resetState()
            onNavigateToUpload(examId)
        }
    }

    // Manejo de errores de validación
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Registrar examen", color = BlancoClinico, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = BlancoClinico
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulClinico)
            )
        },
        containerColor = BlancoClinico
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Identificador / Nombre del Paciente
            Text("Paciente / ID *", color = TextoOscuro, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = uiState.patientId,
                onValueChange = { viewModel.onPatientIdChange(it) },
                placeholder = { Text("Ej. PAC-04821 o Nombre del paciente", color = GrisAzulado) },
                textStyle = TextStyle(color = TextoOscuro, fontSize = 16.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulClinico,
                    unfocusedBorderColor = GrisAzulado.copy(alpha = 0.5f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = TextoOscuro,
                    unfocusedTextColor = TextoOscuro
                )
            )

            // 2. Sede / Sucursal
            Text("Sede *", color = TextoOscuro, fontWeight = FontWeight.Bold)
            ExposedDropdownMenuBox(
                expanded = expandedSede,
                onExpandedChange = { expandedSede = !expandedSede },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.branch,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Selecciona una sede médica", color = GrisAzulado) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSede) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulClinico,
                        unfocusedBorderColor = GrisAzulado.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = TextoOscuro,
                        unfocusedTextColor = TextoOscuro
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedSede,
                    onDismissRequest = { expandedSede = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    sedesDisponibles.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion, color = TextoOscuro) },
                            onClick = {
                                viewModel.onBranchChange(opcion)
                                expandedSede = false
                            }
                        )
                    }
                }
            }

            // 3. Tipo de examen
            Text("Tipo de examen *", color = TextoOscuro, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tiposExamen.take(2).forEach { examen ->
                    val seleccionado = uiState.examType == examen
                    OutlinedButton(
                        onClick = { viewModel.onExamTypeChange(examen) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (seleccionado) AzulClinico else Color.White,
                            contentColor = if (seleccionado) BlancoClinico else TextoOscuro
                        ),
                        border = BorderStroke(1.dp, if (seleccionado) AzulClinico else GrisAzulado.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(examen)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tiposExamen.drop(2).forEach { examen ->
                    val seleccionado = uiState.examType == examen
                    OutlinedButton(
                        onClick = { viewModel.onExamTypeChange(examen) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (seleccionado) AzulClinico else Color.White,
                            contentColor = if (seleccionado) BlancoClinico else TextoOscuro
                        ),
                        border = BorderStroke(1.dp, if (seleccionado) AzulClinico else GrisAzulado.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(examen)
                    }
                }
            }

            // 4. Ojo evaluado
            Text("Ojo evaluado *", color = TextoOscuro, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ojosDisponibles.forEach { ojo ->
                    val seleccionado = uiState.eye == ojo
                    OutlinedButton(
                        onClick = { viewModel.onEyeChange(ojo) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (seleccionado) AzulClinico else Color.White,
                            contentColor = if (seleccionado) BlancoClinico else TextoOscuro
                        ),
                        border = BorderStroke(1.dp, if (seleccionado) AzulClinico else GrisAzulado.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (ojo) {
                                "OD (derecho)" -> "OD"
                                "OI (izquierdo)" -> "OI"
                                else -> "AO"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 5. Notas Clínicas / Observaciones
            Text("Observaciones clínicas", color = TextoOscuro, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onNotesChange(it) },
                placeholder = { Text("Escriba aquí las notas clínicas del procedimiento...", color = GrisAzulado) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulClinico,
                    unfocusedBorderColor = GrisAzulado.copy(alpha = 0.5f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = TextoOscuro,
                    unfocusedTextColor = TextoOscuro
                )
            )

            // 6. Selector de Archivo Adjunto (Opcional en esta pantalla)
            Text("Documento o informe anexo", color = TextoOscuro, fontWeight = FontWeight.Bold)
            if (uiState.fileUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AzulClinico.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Archivo adjunto",
                                tint = RojoAlerta,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = uiState.fileName ?: "archivo_adjunto.pdf",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextoOscuro
                                )
                                Text(
                                    text = "Adjunto listo para registrar",
                                    fontSize = 12.sp,
                                    color = GrisAzulado
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.onFileSelected(null, null) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar archivo",
                                tint = GrisAzulado
                            )
                        }
                    }
                }
            } else {
                OutlinedCard(
                    onClick = { fileLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Adjuntar",
                            tint = AzulClinico
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Adjuntar documento (PDF o imagen)",
                            color = AzulClinico,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. Botón de Guardar y Registrar
            Button(
                onClick = { viewModel.registrarAtencion() },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulClinico,
                    contentColor = BlancoClinico
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = BlancoClinico,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Guardar y Registrar",
                        color = BlancoClinico,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
