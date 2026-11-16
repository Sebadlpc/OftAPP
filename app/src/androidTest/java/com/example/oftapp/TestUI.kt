package com.example.oftapp

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TestUI {

    class OftappLoginUITest {
        @get:Rule
        val rule = createAndroidComposeRule<MainActivity>()

        @Test
        fun verificarBotonIngresoVisible(){
            // Verifica que el texto del botón coincida exactamente con el de tu UI
            rule.onNodeWithText("Ingresar").assertIsDisplayed()
        }

        @Test
        fun loginExitoso_muestraPantallaInicio(){
            rule.onNodeWithTag("correo").performTextInput("usuario@oftapp.com")
            rule.onNodeWithTag("clave").performTextInput("secreta123")

            rule.onNodeWithText("Ingresar").performClick()

            // Reemplaza "Bienvenido" con el texto real que aparece tras un login exitoso en Oftapp
            rule.onNodeWithText("Bienvenido").assertIsDisplayed()
        }

        @Test
        fun loginFallido_muestraMensajeError(){
            rule.onNodeWithText("Ingresar").performClick()

            // Reemplaza con el mensaje de error exacto que uses (ej. "Usuario no encontrado")
            rule.onNodeWithText("Credenciales incorrectas").assertIsDisplayed()
        }
    }
}