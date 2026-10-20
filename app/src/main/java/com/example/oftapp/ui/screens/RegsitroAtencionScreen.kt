package com.example.oftapp.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oftapp.ui.theme.AzulClinico
import com.example.oftapp.ui.theme.BlancoClinico
import com.example.oftapp.ui.theme.GrisAzulado
import com.example.oftapp.ui.theme.OftAPPTheme
import com.example.oftapp.ui.theme.TextoOscuro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroAtencionScreen(onNextToDocumentUpload: () -> Unit) {
    // Controlador para que la flecha superior vuelva atrás sin cambiar OftAppNavigation
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // Estados del formulario
    var idPaciente by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("19 Sep 2026") }
    var sede by remember { mutableStateOf("") }
    var expandedSede by remember { mutableStateOf(false) }
    val sedesDisponibles = listOf("Sede Norte", "Sede Centro", "Sede Sur")

    var tipoExamen by remember { mutableStateOf("Campimetría") }
    var ojoEvaluado by remember { mutableStateOf("OD (derecho)") }

    // Estados del Calendario
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Detecta el toque en cualquier parte de la caja de fecha para abrir el calendario
    val dateInteractionSource = remember { MutableInteractionSource() }
    LaunchedEffect(dateInteractionSource) {
        dateInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDatePicker = true
            }
        }
    }

    // Forzamos el tema claro en esta vista para evitar letras blancas sobre fondo blanco
    OftAPPTheme(darkTheme = false) {
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextoOscuro,
            unfocusedTextColor = TextoOscuro,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = AzulClinico,
            unfocusedBorderColor = GrisAzulado,
            cursorColor = AzulClinico
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es", "CL"))
                                sdf.timeZone = TimeZone.getTimeZone("UTC")
                                selectedDate = sdf.format(Date(millis))
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Aceptar", color = AzulClinico, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar", color = GrisAzulado)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Registrar examen", color = BlancoClinico) },
                    navigationIcon = {
                        IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
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
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. ID del paciente
                Text("ID del paciente *", color = TextoOscuro, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = idPaciente,
                    onValueChange = { idPaciente = it },
                    placeholder = { Text("PAC-00000", color = GrisAzulado) },
                    textStyle = TextStyle(color = TextoOscuro, fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )

                // 2. Fecha del examen
                Text("Fecha del examen", color = TextoOscuro, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    readOnly = true,
                    interactionSource = dateInteractionSource,
                    textStyle = TextStyle(color = TextoOscuro, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Abrir calendario",
                                tint = AzulClinico
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )

                // 3. Sede (Menú desplegable)
                Text("Sede", color = TextoOscuro, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = expandedSede,
                    onExpandedChange = { expandedSede = !expandedSede }
                ) {
                    OutlinedTextField(
                        value = sede,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Selecciona una sede", color = GrisAzulado) },
                        textStyle = TextStyle(color = TextoOscuro, fontSize = 16.sp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSede)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
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
                                    sede = opcion
                                    expandedSede = false
                                }
                            )
                        }
                    }
                }

                // 4. Tipo de examen
                Text("Tipo de examen *", color = TextoOscuro, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Campimetría", "Topografía").forEach { examen ->
                        val seleccionado = tipoExamen == examen
                        OutlinedButton(
                            onClick = { tipoExamen = examen },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (seleccionado) AzulClinico else Color.White,
                                contentColor = if (seleccionado) BlancoClinico else TextoOscuro
                            ),
                            border = BorderStroke(1.dp, if (seleccionado) AzulClinico else GrisAzulado)
                        ) {
                            Text(examen)
                        }
                    }
                }

                // 5. Ojo evaluado
                Text("Ojo evaluado *", color = TextoOscuro, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("OD (derecho)", "OI (izquierdo)").forEach { ojo ->
                        val seleccionado = ojoEvaluado == ojo
                        OutlinedButton(
                            onClick = { ojoEvaluado = ojo },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (seleccionado) AzulClinico else Color.White,
                                contentColor = if (seleccionado) BlancoClinico else TextoOscuro
                            ),
                            border = BorderStroke(1.dp, if (seleccionado) AzulClinico else GrisAzulado)
                        ) {
                            Text(ojo)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón para avanzar a Carga de Documentos
                Button(
                    onClick = onNextToDocumentUpload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulClinico)
                ) {
                    Text(
                        text = "Siguiente: Cargar Documentos",
                        color = BlancoClinico,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}