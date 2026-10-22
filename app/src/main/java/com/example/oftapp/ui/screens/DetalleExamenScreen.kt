package com.example.oftapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.oftapp.ui.viewmodel.DetalleExamenViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleExamenScreen(
    examId: String,
    viewModel: DetalleExamenViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar el examen correspondiente al examId
    LaunchedEffect(examId) {
        if (examId.isNotBlank()) {
            viewModel.loadExam(examId)
        }
    }

    Scaffold(
        containerColor = BlancoClinico,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle del examen",
                        color = BlancoClinico,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás",
                            tint = BlancoClinico
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Opciones adicionales */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = BlancoClinico
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulClinico
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AzulClinico)
            }
        } else if (uiState.exam == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontró el examen con identificador: $examId",
                    color = GrisAzulado,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            val exam = uiState.exam!!
            val isValidated = exam.status.equals("Validado", ignoreCase = true) ||
                    exam.status.equals("Completado", ignoreCase = true)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta 1: Ficha Clínica Completa
                InfoExamenCard(exam = exam)

                // Tarjeta 2: Archivo Adjunto y Enlace / URI
                ArchivoAdjuntoCard(exam = exam)

                // Tarjeta 3: Observaciones Clínicas
                ObservacionesCard(notes = exam.notes)

                Spacer(modifier = Modifier.height(12.dp))

                // Sección Inferior: Botón para cambiar de estado ("Validar")
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isValidated) "Examen validado clínicamente" else "Solo personal facultativo puede validar",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isValidated) VerdeSalud else GrisAzulado,
                        fontWeight = if (isValidated) FontWeight.Bold else FontWeight.Normal
                    )

                    Button(
                        onClick = { viewModel.validateExam() },
                        enabled = !isValidated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulClinico,
                            contentColor = BlancoClinico,
                            disabledContainerColor = VerdeSalud.copy(alpha = 0.2f),
                            disabledContentColor = VerdeSalud
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isValidated) "Examen Validado" else "Validar examen",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoExamenCard(exam: ExamEntity) {
    val isValidated = exam.status.equals("Validado", ignoreCase = true) ||
            exam.status.equals("Completado", ignoreCase = true)
    val statusColor = if (isValidated) VerdeSalud else GrisAzulado
    val statusBg = if (isValidated) VerdeSalud.copy(alpha = 0.12f) else GrisAzulado.copy(alpha = 0.12f)

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-CL"))
    val fechaTexto = try {
        dateFormat.format(exam.date)
    } catch (_: Exception) {
        "N/D"
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exam.examType,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )

                // Insignia de estado
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = exam.status,
                            color = statusColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Text(
                text = "${exam.id} · ${exam.patientName}",
                fontSize = 14.sp,
                color = GrisAzulado,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            // Grilla de detalles 2x2
            Row(modifier = Modifier.fillMaxWidth()) {
                CampoDetalle(
                    label = "Ojo evaluado",
                    valor = exam.eye,
                    modifier = Modifier.weight(1f)
                )
                CampoDetalle(
                    label = "Fecha",
                    valor = fechaTexto,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                CampoDetalle(
                    label = "Sede",
                    valor = exam.branch,
                    modifier = Modifier.weight(1f)
                )
                CampoDetalle(
                    label = "Especialista / Tecnólogo",
                    valor = exam.doctorName,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ArchivoAdjuntoCard(exam: ExamEntity) {
    val context = LocalContext.current
    val hasUri = exam.fileUri != null
    val fileName = exam.fileUri?.lastPathSegment ?: "informe_clinico_${exam.id}.pdf"
    val fileUriString = exam.fileUri?.toString() ?: "URI no registrada"

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Archivo o informe adjunto",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextoOscuro,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono PDF o Documento
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RojoAlerta.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Documento",
                        tint = RojoAlerta
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextoOscuro
                    )
                    Text(
                        text = if (hasUri) fileUriString else "Sin archivo físico asociado",
                        fontSize = 12.sp,
                        color = GrisAzulado,
                        maxLines = 1
                    )
                }

                if (hasUri) {
                    IconButton(onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = exam.fileUri
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // En caso de que no haya visor asociado en el dispositivo
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Abrir documento",
                            tint = AzulClinico
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ObservacionesCard(notes: String) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Observaciones clínicas",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextoOscuro,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = GrisAzulado.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = GrisAzulado.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = notes.ifBlank { "Sin observaciones registradas para esta atención médica." },
                    fontSize = 14.sp,
                    color = TextoOscuro,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun CampoDetalle(
    label: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = GrisAzulado
        )
        Text(
            text = valor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOscuro,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
