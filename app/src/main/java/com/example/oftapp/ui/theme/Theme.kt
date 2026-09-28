package com.example.oftapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


private val DarkColorScheme = darkColorScheme(
    primary = AzulClinico,
    secondary = GrisAzulado,
    background = TextoOscuro,
    surface = TextoOscuro,
    onPrimary = BlancoClinico,
    onBackground = BlancoClinico,
    onSurface = BlancoClinico,
    error = RojoAlerta
)

// Paleta oficial de OftApp para modo claro
private val LightColorScheme = lightColorScheme(
    primary = AzulClinico,
    secondary = GrisAzulado,
    background = BlancoClinico,
    surface = BlancoClinico,
    onPrimary = BlancoClinico,
    onSecondary = BlancoClinico,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro,
    error = RojoAlerta
)

@Composable
fun OftAPPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color deshabilitado por defecto para forzar la paleta del equipo
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asegúrate de tener tu archivo Typography.kt
        content = content
    )
}