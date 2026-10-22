package com.example.oftapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Esquema de color para Modo Noche (Dark Theme).
 *
 * Utiliza tonos luminosos de la paleta institucional (AzulClinicoDark, VerdeSaludDark, RojoAlertaDark)
 * sobre superficies oscuras (#101416 y #191C1E) para garantizar una relación de contraste superior a 7:1 (WCAG AAA).
 */
private val DarkColorScheme = darkColorScheme(
    primary = AzulClinicoDark,
    onPrimary = SuperficieOscura,
    primaryContainer = AzulClinico,
    onPrimaryContainer = BlancoClinico,
    secondary = GrisClaro,
    onSecondary = SuperficieOscura,
    background = FondoOscuro,
    surface = SuperficieOscura,
    surfaceVariant = SuperficieContenedorOscura,
    onBackground = TextoClaro,
    onSurface = TextoClaro,
    onSurfaceVariant = GrisClaro,
    error = RojoAlertaDark,
    onError = SuperficieOscura
)

/**
 * Esquema de color para Modo Día (Light Theme).
 *
 * Aplica con estricta fidelidad la paleta corporativa oficial de OftAPP:
 * - Primario: #006689 (Azul Clínico)
 * - Fondo / Superficie: #F8FDFF (Blanco Clínico)
 * - Éxito: #006E36 (Verde Salud)
 * - Alerta: #B02F00 (Rojo/Naranja Clínico)
 * - Textos: #191C1E (TextoOscuro, contraste 15.8:1 sobre Blanco Clínico)
 */
private val LightColorScheme = lightColorScheme(
    primary = AzulClinico,
    onPrimary = BlancoClinico,
    primaryContainer = BlancoClinico,
    onPrimaryContainer = AzulClinico,
    secondary = GrisAzulado,
    onSecondary = BlancoClinico,
    background = BlancoClinico,
    surface = BlancoClinico,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro,
    error = RojoAlerta,
    onError = BlancoClinico
)

/**
 * Tema principal de OftAPP (Material Design 3).
 *
 * @param darkTheme Define si se activa el modo noche. Por defecto respeta la configuración del sistema.
 * @param dynamicColor Se mantiene en false por defecto para preservar la identidad médica corporativa.
 * @param content Árbol composable que hereda el tema.
 */
@Composable
fun OftAPPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        typography = Typography,
        content = content
    )
}
