package com.example.oftapp.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oftapp.ui.theme.AzulClinico
import com.example.oftapp.ui.theme.BlancoClinico
import com.example.oftapp.ui.theme.GrisAzulado
import com.example.oftapp.ui.theme.OftAPPTheme
import com.example.oftapp.ui.theme.RojoAlerta
import com.example.oftapp.ui.theme.TextoOscuro
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargaDocumentosScreen(
    examId: String? = null,
    onFinishUpload: () -> Unit
) {
    // Controlador nativo para volver atrás sin romper OftAppNavigation
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val context = LocalContext.current

    // Estados para guardar el archivo real seleccionado
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var fileSize by remember { mutableStateOf("") }

    // Abre el explorador de archivos real del teléfono
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

    OftAPPTheme(darkTheme = false) {
        Scaffold(
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
            },
            containerColor = BlancoClinico
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Caja para abrir el selector de archivos del teléfono
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.dp, GrisAzulado, RoundedCornerShape(16.dp))
                        .clickable {
                            launcher.launch("*/*")
                        },
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

                // Tarjeta que muestra el archivo real cargado
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
                                        imageVector = Icons.Default.InsertDriveFile,
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

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Carga completada",
                                color = GrisAzulado,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón final (solo se habilita cuando seleccionas un archivo real)
                Button(
                    onClick = onFinishUpload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulClinico,
                        disabledContainerColor = GrisAzulado
                    ),
                    enabled = fileUri != null
                ) {
                    Text(
                        text = "Terminar y volver al Dashboard",
                        color = BlancoClinico,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
