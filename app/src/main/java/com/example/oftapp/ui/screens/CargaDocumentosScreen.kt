package com.example.oftapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oftapp.data.local.entity.DocumentoEntity
import com.example.oftapp.data.repository.OftAppRepository
import com.example.oftapp.ui.theme.AzulClinico
import com.example.oftapp.ui.theme.BlancoClinico
import com.example.oftapp.ui.theme.GrisAzulado
import com.example.oftapp.ui.theme.RojoAlerta
import com.example.oftapp.ui.theme.TextoOscuro
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargaDocumentosScreen(
    examId: String? = null,
    repository: OftAppRepository? = null,
    onFinishUpload: () -> Unit
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fileUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var fileName by rememberSaveable { mutableStateOf("") }
    var fileSize by rememberSaveable { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fileUri = uri
        if (uri != null) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    fileName = if (nameIndex != -1) cursor.getString(nameIndex) else "documento_adjunto"
                    if (sizeIndex != -1) {
                        val bytes = cursor.getLong(sizeIndex)
                        val kb = bytes / 1024.0
                        val mb = kb / 1024.0
                        fileSize = if (mb >= 1.0) {
                            String.format(Locale.US, "%.1f MB", mb)
                        } else {
                            String.format(Locale.US, "%.0f KB", kb)
                        }
                    } else {
                        fileSize = "Archivo listo"
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = BlancoClinico,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Adjuntar documento", color = BlancoClinico, fontWeight = FontWeight.Bold)
                        if (!examId.isNullOrBlank()) {
                            Text(
                                text = "Asociado a examen: $examId",
                                color = BlancoClinico.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = BlancoClinico
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulClinico)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Zona para seleccionar archivo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, GrisAzulado.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { launcher.launch("*/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Subir archivo",
                        tint = AzulClinico,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selecciona un archivo",
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                    Text(
                        text = "PDF o imagen · máx. 20 MB",
                        color = GrisAzulado,
                        fontSize = 12.sp
                    )
                }
            }

            // Tarjeta que muestra el archivo real cargado con opción de previsualizar
            if (fileUri != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                    contentDescription = "Archivo",
                                    tint = RojoAlerta
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = fileName,
                                        fontWeight = FontWeight.Bold,
                                        color = TextoOscuro
                                    )
                                    Text(
                                        text = fileSize,
                                        color = GrisAzulado,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    fileUri = null
                                    fileName = ""
                                    fileSize = ""
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar archivo",
                                    tint = GrisAzulado
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxWidth(),
                            color = AzulClinico,
                            trackColor = Color(0xFFEAEEEE)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val previewIntent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(fileUri, "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(Intent.createChooser(previewIntent, "Previsualizar archivo"))
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "No se puede previsualizar este archivo", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, AzulClinico),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = AzulClinico
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Previsualizar", fontSize = 12.sp, color = AzulClinico)
                            }

                            Text(
                                text = "Carga completada",
                                color = GrisAzulado,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón final para guardar y volver al Dashboard
            Button(
                onClick = {
                    if (repository != null && !examId.isNullOrBlank() && fileUri != null) {
                        isUploading = true
                        coroutineScope.launch {
                            try {
                                val currentExam = repository.getExamById(examId).firstOrNull()
                                if (currentExam != null) {
                                    val updatedExam = currentExam.copy(fileUri = fileUri)
                                    repository.insertExam(updatedExam)
                                }
                                val doc = DocumentoEntity(
                                    id = UUID.randomUUID().toString(),
                                    examId = examId,
                                    fileName = fileName.ifBlank { "documento_adjunto.pdf" },
                                    fileUri = fileUri,
                                    uploadDate = Date()
                                )
                                repository.insertDocumento(doc)
                            } catch (_: Exception) {
                            } finally {
                                isUploading = false
                                onFinishUpload()
                            }
                        }
                    } else {
                        onFinishUpload()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulClinico,
                    disabledContainerColor = GrisAzulado
                ),
                enabled = fileUri != null && !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = BlancoClinico,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Terminar y volver al Dashboard",
                        color = BlancoClinico,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
