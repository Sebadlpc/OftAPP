package com.example.oftapp.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Helper para obtener de forma segura un [ParcelFileDescriptor] a partir de cualquier [Uri].
 * Si el ContentResolver no permite apertura directa o no es seekable, se copia temporalmente a caché.
 */
private fun openParcelFileDescriptor(context: Context, uri: Uri): ParcelFileDescriptor? {
    return try {
        context.contentResolver.openFileDescriptor(uri, "r")
    } catch (_: Exception) {
        try {
            val tempFile = File(context.cacheDir, "temp_render_${System.currentTimeMillis()}.pdf")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * Visor PDF Nativo integrado directamente en Jetpack Compose sin depender de aplicaciones externas.
 *
 * Utiliza [PdfRenderer] y [ParcelFileDescriptor] de Android con:
 * - Navegación entre páginas (Anterior / Siguiente, indicador "Página X de Y").
 * - Soporte multitouch y botones accesibles para zoom (1x a 4x) y desplazamiento (paneo).
 * - Manejo seguro de ciclo de vida y cierre garantizado de recursos nativos en [DisposableEffect].
 * - Áreas táctiles con tamaño mínimo >= 48.dp y semánticas de accesibilidad WCAG 2.2 AA.
 */
@Composable
fun PdfRendererView(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pfd by remember(uri) { mutableStateOf<ParcelFileDescriptor?>(null) }
    var renderer by remember(uri) { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember(uri) { mutableIntStateOf(0) }
    var currentPageIndex by remember(uri) { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estado de Zoom y Paneo
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Apertura y ciclo de vida de recursos de PdfRenderer
    DisposableEffect(uri) {
        isLoading = true
        errorMessage = null
        val descriptor = openParcelFileDescriptor(context, uri)
        if (descriptor == null) {
            errorMessage = "No fue posible abrir el archivo PDF clínico."
            isLoading = false
        } else {
            try {
                pfd = descriptor
                val pdfRenderer = PdfRenderer(descriptor)
                renderer = pdfRenderer
                pageCount = pdfRenderer.pageCount
                currentPageIndex = 0
            } catch (e: Exception) {
                errorMessage = "Error al inicializar el visor PDF: ${e.localizedMessage ?: "Formato no compatible"}"
                try { descriptor.close() } catch (_: Exception) {}
                pfd = null
                renderer = null
            }
        }

        onDispose {
            try {
                renderer?.close()
            } catch (_: Exception) {}
            try {
                pfd?.close()
            } catch (_: Exception) {}
            renderer = null
            pfd = null
            currentBitmap?.recycle()
            currentBitmap = null
        }
    }

    // Renderizado seguro en hilo IO de la página seleccionada
    LaunchedEffect(renderer, currentPageIndex) {
        val currentRenderer = renderer ?: return@LaunchedEffect
        if (pageCount <= 0 || currentPageIndex < 0 || currentPageIndex >= pageCount) return@LaunchedEffect

        isLoading = true
        scale = 1f
        offset = Offset.Zero

        val result = withContext(Dispatchers.IO) {
            try {
                val bitmap = synchronized(currentRenderer) {
                    val page = currentRenderer.openPage(currentPageIndex)
                    // Renderizamos a 2x de resolución para nitidez en gráficos médicos
                    val targetWidth = (page.width * 2).coerceAtLeast(600)
                    val targetHeight = (page.height * 2).coerceAtLeast(800)
                    val bmp = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bmp)
                    canvas.drawColor(AndroidColor.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bmp
                }
                Result.success(bitmap)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        result.fold(
            onSuccess = { newBitmap ->
                currentBitmap?.recycle()
                currentBitmap = newBitmap
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = "Error al renderizar página: ${error.localizedMessage}"
                isLoading = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        // Área interactiva de la página con zoom y paneo
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        if (scale > 1f) {
                            val maxOffsetX = (size.width * (scale - 1)) / 2f
                            val maxOffsetY = (size.height * (scale - 1)) / 2f
                            val newOffset = offset + pan
                            offset = Offset(
                                x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        } else {
                            offset = Offset.Zero
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                errorMessage != null -> {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                currentBitmap != null -> {
                    Image(
                        bitmap = currentBitmap!!.asImageBitmap(),
                        contentDescription = "Página ${currentPageIndex + 1} de $pageCount del informe clínico",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            // Controles flotantes accesibles de zoom para WCAG 2.2 (alternativa a gestos multitouch)
            if (currentBitmap != null && !isLoading) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scale = (scale - 0.5f).coerceIn(1f, 4f)
                            if (scale == 1f) offset = Offset.Zero
                        },
                        enabled = scale > 1f,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Reducir zoom del PDF"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = null
                        )
                    }

                    Text(
                        text = "${String.format(java.util.Locale.ROOT, "%.1f", scale)}x",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = {
                            scale = (scale + 0.5f).coerceIn(1f, 4f)
                        },
                        enabled = scale < 4f,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Aumentar zoom del PDF"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null
                        )
                    }

                    if (scale != 1f || offset != Offset.Zero) {
                        IconButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            },
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Restablecer vista y zoom del PDF"
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }

        // Barra inferior accesible de navegación entre páginas (WCAG 2.2 AA)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (currentPageIndex > 0) {
                            currentPageIndex--
                        }
                    },
                    enabled = currentPageIndex > 0 && !isLoading,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Ir a la página anterior"
                        }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }

                Text(
                    text = if (pageCount > 0) "Página ${currentPageIndex + 1} de $pageCount" else "Cargando documento...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics {
                        contentDescription = if (pageCount > 0) "Estás en la página ${currentPageIndex + 1} de $pageCount" else "Cargando"
                    }
                )

                IconButton(
                    onClick = {
                        if (currentPageIndex < pageCount - 1) {
                            currentPageIndex++
                        }
                    },
                    enabled = currentPageIndex < pageCount - 1 && !isLoading,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Ir a la página siguiente"
                        }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
