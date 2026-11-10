package com.example.oftapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.oftapp.ui.screens.LoginScreen
import com.example.oftapp.ui.theme.OftAPPTheme
import com.example.oftapp.ui.viewmodel.LoginViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas de interfaz (UI) de la pantalla de Login.
 * Necesitan un emulador o celular conectado: clic derecho sobre la clase -> Run 'LoginUITest'.
 */
class LoginUITest {

    @get:Rule
    val rule = createComposeRule()

    /** Guarda el rol que la pantalla entrega al iniciar sesión (null = no navegó). */
    private var rolRecibido: String? = null

    @Before
    fun mostrarPantallaLogin() {
        rolRecibido = null
        rule.setContent {
            OftAPPTheme {
                LoginScreen(
                    viewModel = LoginViewModel(),
                    onLoginSuccess = { rol -> rolRecibido = rol }
                )
            }
        }
    }

    @Test
    fun elementosVisibles() {
        rule.onNodeWithText("OftApp").assertIsDisplayed()
        rule.onNodeWithTag("usuario").assertIsDisplayed()
        rule.onNodeWithTag("clave").assertIsDisplayed()
        rule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
    }

    @Test
    fun loginCorrecto_navegaConElRol() {
        rule.onNodeWithTag("usuario").performTextInput("Medico")
        rule.onNodeWithTag("clave").performTextInput("1234")
        rule.onNodeWithTag("botonLogin").performClick()

        rule.waitForIdle()
        assertEquals("Médico Oftalmólogo", rolRecibido)
    }

    @Test
    fun loginIncorrecto_muestraError() {
        rule.onNodeWithTag("usuario").performTextInput("Medico")
        rule.onNodeWithTag("clave").performTextInput("0000")
        rule.onNodeWithTag("botonLogin").performClick()

        rule.onNodeWithTag("mensajeError")
            .assertIsDisplayed()
            .assertTextEquals("Credenciales incorrectas. Verifique usuario y contraseña.")
        assertNull(rolRecibido)
    }

    @Test
    fun camposVacios_muestraError() {
        rule.onNodeWithTag("botonLogin").performClick()

        rule.onNodeWithText("Por favor, ingrese usuario y contraseña").assertIsDisplayed()
        assertNull(rolRecibido)
    }

    @Test
    fun noHayErrorAlAbrirLaPantalla() {
        rule.onNodeWithTag("mensajeError").assertDoesNotExist()
    }
}
