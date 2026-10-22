package com.example.oftapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta corporativa oficial de OftAPP (Material Design 3).
 *
 * Colores clínicos definidos para garantizar identidad institucional,
 * trazabilidad de exámenes y lectura óptima de acuerdo a estándares WCAG AAA/AA.
 */

// 1. Paleta Corporativa Principal (Modo Día / Light Theme)
val AzulClinico = Color(0xFF006689)   // Primario: Azul Clínico (#006689)
val BlancoClinico = Color(0xFFF8FDFF) // Fondo: Blanco Clínico (#F8FDFF)
val VerdeSalud = Color(0xFF006E36)    // Éxito: Verde Salud (#006E36)
val RojoAlerta = Color(0xFFB02F00)    // Alerta: Rojo/Naranja Clínico (#B02F00)
val GrisAzulado = Color(0xFF4F616E)   // Secundario: Gris Azulado (#4F616E)
val TextoOscuro = Color(0xFF191C1E)   // Texto de alto contraste sobre fondo claro

// 2. Variantes de Alto Contraste para Modo Noche (Dark Theme)
val AzulClinicoDark = Color(0xFF7BD0FF) // Tono luminoso de Azul Clínico para modo noche (>7:1 contraste)
val VerdeSaludDark = Color(0xFF68DC8F)  // Tono accesible de Verde Salud para modo noche (>7:1 contraste)
val RojoAlertaDark = Color(0xFFFFB4AB)  // Tono accesible de Rojo Alerta para modo noche (>7:1 contraste)
val FondoOscuro = Color(0xFF101416)     // Fondo clínico oscuro para modo noche
val SuperficieOscura = Color(0xFF191C1E)// Tarjetas y superficies elevadas modo noche
val SuperficieContenedorOscura = Color(0xFF23282B)
val TextoClaro = Color(0xFFE1E3E5)      // Texto principal alto contraste en modo noche
val GrisClaro = Color(0xFFC0C7CD)       // Texto secundario sobre superficies oscuras
