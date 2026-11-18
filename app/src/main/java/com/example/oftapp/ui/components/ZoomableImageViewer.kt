package com.example.oftapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.util.Locale

/**
 * Visor de Imágenes Médicas Especializado con soporte Multitouch y Zoom Continuo (1x a 5x).
 *
 * Diseñado para retinografías, topografías corneales y capturas oftalmológicas de alta resolución.
 * Cumple con los requerimientos de accesibilidad WCAG 2.2 AA (gestos alternativos y touch targets >= 48.dp).
 *
 * @param imageUri URI o fuente de la imagen médica.
 * @param contentDescription Descripción accesible para TalkBack.
 * @param modifier Modificador composable.
 */
@Composable
fun ZoomableImageViewer(
    imageUri: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = "Imagen clínica oftalmológica con soporte de zoom"
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
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
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )

        // Barra flotante de controles accesibles (WCAG 2.2 AA: alternativas táctiles sin requerir pellizco multitouch)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    scale = (scale - 0.5f).coerceIn(1f, 5f)
                    if (scale == 1f) offset = Offset.Zero
                },
                enabled = scale > 1f,
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .semantics {
                        role = Role.Button
                        this.contentDescription = "Reducir zoom de imagen"
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = null,
                    tint = if (scale > 1f) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            Text(
                text = "${String.format(Locale.ROOT, "%.1f", scale)}x",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics {
                    this.contentDescription = "Nivel de zoom actual: ${String.format(Locale.ROOT, "%.1f", scale)} veces"
                }
            )

            IconButton(
                onClick = {
                    scale = (scale + 0.5f).coerceIn(1f, 5f)
                },
                enabled = scale < 5f,
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .semantics {
                        role = Role.Button
                        this.contentDescription = "Aumentar zoom de imagen"
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = null,
                    tint = if (scale < 5f) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            FilledTonalButton(
                onClick = {
                    scale = 1f
                    offset = Offset.Zero
                },
                enabled = scale != 1f || offset != Offset.Zero,
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .semantics {
                        role = Role.Button
                        this.contentDescription = "Restablecer vista a escala normal"
                    },
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Restablecer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
