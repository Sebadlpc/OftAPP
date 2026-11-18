package com.example.oftapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

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
 * Esquemas de Alto Contraste para Accesibilidad WCAG 2.2 AA / AAA (Relación de contraste >= 7:1).
 */
private val HighContrastLightColorScheme = lightColorScheme(
    primary = AltoContrastePrimarioLuz,
    onPrimary = AltoContrasteFondoLuz,
    primaryContainer = AltoContrastePrimarioLuz,
    onPrimaryContainer = AltoContrasteFondoLuz,
    secondary = AltoContrasteTextoLuz,
    onSecondary = AltoContrasteFondoLuz,
    secondaryContainer = AltoContrasteFondoLuz,
    onSecondaryContainer = AltoContrasteTextoLuz,
    tertiary = AltoContrastePrimarioLuz,
    onTertiary = AltoContrasteFondoLuz,
    background = AltoContrasteFondoLuz,
    surface = AltoContrasteFondoLuz,
    surfaceVariant = AltoContrasteFondoLuz,
    onBackground = AltoContrasteTextoLuz,
    onSurface = AltoContrasteTextoLuz,
    onSurfaceVariant = AltoContrasteTextoLuz,
    outline = AltoContrasteBordeLuz,
    outlineVariant = AltoContrasteBordeLuz,
    error = RojoAlerta,
    onError = AltoContrasteFondoLuz
)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = AltoContrastePrimarioNoche,
    onPrimary = AltoContrasteFondoNoche,
    primaryContainer = AltoContrastePrimarioNoche,
    onPrimaryContainer = AltoContrasteFondoNoche,
    secondary = AltoContrasteAmarilloAccesible,
    onSecondary = AltoContrasteFondoNoche,
    secondaryContainer = AltoContrasteFondoNoche,
    onSecondaryContainer = AltoContrasteAmarilloAccesible,
    tertiary = AltoContrasteAmarilloAccesible,
    onTertiary = AltoContrasteFondoNoche,
    background = AltoContrasteFondoNoche,
    surface = AltoContrasteFondoNoche,
    surfaceVariant = AltoContrasteFondoNoche,
    onBackground = AltoContrasteTextoNoche,
    onSurface = AltoContrasteTextoNoche,
    onSurfaceVariant = AltoContrasteTextoNoche,
    outline = AltoContrasteBordeNoche,
    outlineVariant = AltoContrasteBordeNoche,
    error = RojoAlertaDark,
    onError = AltoContrasteFondoNoche
)

/**
 * Tema principal de OftAPP (Material Design 3).
 *
 * @param darkTheme Define si se activa el modo noche. Por defecto respeta la configuración del sistema.
 * @param dynamicColor Se mantiene en false por defecto para preservar la identidad médica corporativa.
 * @param highContrast Activa la paleta accesible WCAG 2.2 AA / AAA con contraste >= 7:1.
 * @param fontScaleFactor Factor multiplicador de escala de fuentes (1.0f, 1.2f, 1.4f).
 * @param content Árbol composable que hereda el tema.
 */
@Composable
fun OftAPPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    highContrast: Boolean = false,
    fontScaleFactor: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast && darkTheme -> HighContrastDarkColorScheme
        highContrast -> HighContrastLightColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val currentDensity = LocalDensity.current
    val customDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * fontScaleFactor
    )

    CompositionLocalProvider(
        LocalDensity provides customDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
