package com.example.oftapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oftapp.data.repository.OftAppRepository
import com.example.oftapp.ui.components.PdfRendererView
import com.example.oftapp.ui.components.ZoomableImageViewer
import com.example.oftapp.ui.theme.AzulClinico
import com.example.oftapp.ui.theme.BlancoClinico
import com.example.oftapp.ui.theme.GrisAzulado
import com.example.oftapp.util.DocumentUtils

/**
 * Pantalla Unificada del Visor Clínico de OftAPP.
 *
 * Detecta de forma inteligente si el archivo adjunto es una imagen médica (retinografía, OCT, topografía)
 * o un documento PDF (informe clínico digital o escaneo). Muestra el visor nativo correspondiente
 * dentro de la aplicación, sin depender de visores externos.
 *
 * Incluye botón de descarga/exportación del informe PDF hacia la carpeta pública Downloads.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisorClinicoScreen(
    examId: String,
    repository: OftAppRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val exam by repository.getExamById(examId).collectAsState(initial = null)

    // Detección automática del tipo de medio (Imagen vs PDF)
    val isImage = remember(exam?.fileUri) {
        val uri = exam?.fileUri ?: return@remember false
        val mimeType = try {
            context.contentResolver.getType(uri)
        } catch (_: Exception) {
            null
        }

        if (mimeType != null && mimeType.startsWith("image/", ignoreCase = true)) {
            true
        } else {
            val path = (uri.path ?: uri.toString()).lowercase()
            path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") ||
                    path.endsWith(".webp") || path.endsWith(".bmp")
        }
    }

    // Si no tiene archivo físico adjunto, se genera dinámicamente el informe PDF oficial en caché
    val activeUri: Uri? = remember(exam, isImage) {
        val currentExam = exam ?: return@remember null
        if (currentExam.fileUri != null) {
            currentExam.fileUri
        } else {
            DocumentUtils.generateCachePdfUri(context, currentExam)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isImage) "Visor de Imagen Médica" else "Visor de Informe PDF",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlancoClinico,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        exam?.let { currentExam ->
                            Text(
                                text = "${currentExam.examType} · ${currentExam.patientName}",
                                fontSize = 12.sp,
                                color = BlancoClinico.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Volver atrás desde visor clínico"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = BlancoClinico
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            exam?.let { currentExam ->
                                DocumentUtils.downloadDocument(context, currentExam) { resultado ->
                                    Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = exam != null,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Descargar o exportar informe PDF a la carpeta Descargas"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = BlancoClinico,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulClinico
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BlancoClinico),
            contentAlignment = Alignment.Center
        ) {
            when {
                exam == null -> {
                    CircularProgressIndicator(color = AzulClinico)
                }
                activeUri == null -> {
                    Text(
                        text = "No fue posible cargar el recurso clínico.",
                        color = GrisAzulado,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                isImage -> {
                    ZoomableImageViewer(
                        imageUri = activeUri,
                        contentDescription = "Imagen médica oftalmológica: ${exam?.examType} del paciente ${exam?.patientName}"
                    )
                }
                else -> {
                    PdfRendererView(
                        uri = activeUri,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
