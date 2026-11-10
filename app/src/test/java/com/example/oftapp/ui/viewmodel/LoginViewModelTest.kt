package com.example.oftapp.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias del login (lógica del LoginViewModel).
 * Se ejecutan en el PC, sin emulador: clic derecho sobre la clase -> Run 'LoginViewModelTest'.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        // viewModelScope usa Dispatchers.Main, que no existe en tests unitarios:
        // lo reemplazamos por un dispatcher de prueba que ejecuta todo al instante.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LoginViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Función de ayuda: escribe usuario y contraseña y presiona "Iniciar Sesión". */
    private fun intentarLogin(usuario: String, clave: String) {
        viewModel.onEmailChange(usuario)
        viewModel.login(clave)
    }

    // ---------- Casos exitosos ----------

    @Test
    fun loginCorrecto_medico() {
        intentarLogin("Medico", "1234")

        val estado = viewModel.uiState.value
        assertTrue(estado.isAuthenticated)
        assertEquals("Médico Oftalmólogo", estado.selectedRole)
        assertEquals("Dra. Carla Ruiz", estado.userName)
        assertNull(estado.errorMessage)
    }

    @Test
    fun loginCorrecto_tecnologo() {
        intentarLogin("Tecnologo", "1234")

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertEquals("Tecnólogo Médico", viewModel.uiState.value.selectedRole)
    }

    @Test
    fun loginCorrecto_admin() {
        intentarLogin("Admin", "1234")

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertEquals("Personal Administrativo", viewModel.uiState.value.selectedRole)
    }

    @Test
    fun loginCorrecto_paciente() {
        intentarLogin("Paciente", "1234")

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertEquals("Paciente", viewModel.uiState.value.selectedRole)
        assertEquals("Juan Pérez", viewModel.uiState.value.userName)
    }

    @Test
    fun loginCorrecto_ignoraMayusculasYEspacios() {
        intentarLogin("  mEdIcO  ", " 1234 ")

        assertTrue(viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun loginCorrecto_aceptaTildes() {
        intentarLogin("Médico", "1234")

        assertTrue(viewModel.uiState.value.isAuthenticated)
    }

    // ---------- Casos fallidos ----------

    @Test
    fun claveIncorrecta() {
        intentarLogin("Medico", "0000")

        val estado = viewModel.uiState.value
        assertFalse(estado.isAuthenticated)
        assertEquals("Credenciales incorrectas. Verifique usuario y contraseña.", estado.errorMessage)
    }

    @Test
    fun usuarioInexistente() {
        intentarLogin("Hacker", "1234")

        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertEquals(
            "Credenciales incorrectas. Verifique usuario y contraseña.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun camposVacios() {
        intentarLogin("", "")

        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertEquals("Por favor, ingrese usuario y contraseña", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun soloUsuario_sinClave() {
        intentarLogin("Medico", "")

        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertEquals("Por favor, ingrese usuario y contraseña", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun soloEspacios_cuentaComoVacio() {
        intentarLogin("   ", "   ")

        assertEquals("Por favor, ingrese usuario y contraseña", viewModel.uiState.value.errorMessage)
    }

    // ---------- Otros comportamientos ----------

    @Test
    fun escribirDeNuevo_borraElError() {
        intentarLogin("Medico", "0000")
        assertTrue(viewModel.uiState.value.errorMessage != null)

        viewModel.onEmailChange("Medic")

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun logout_vuelveAlEstadoInicial() {
        intentarLogin("Admin", "1234")
        assertTrue(viewModel.uiState.value.isAuthenticated)

        viewModel.logout()

        val estado = viewModel.uiState.value
        assertFalse(estado.isAuthenticated)
        assertEquals("", estado.email)
        assertEquals("", estado.userName)
    }
}
