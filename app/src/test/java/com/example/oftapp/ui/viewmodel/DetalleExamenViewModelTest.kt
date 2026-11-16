package com.example.oftapp.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias de la pantalla Detalle de Examen (DetalleExamenViewModel):
 * cargar un examen por su ID y validarlo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleExamenViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: RepositorioFalso

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = RepositorioFalso(
            listOf(
                examen("EX-101", paciente = "María González", tipo = "Campimetría", estado = "Pendiente"),
                examen("EX-102", paciente = "Juan Pérez", tipo = "Tonometría", estado = "Validado")
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- Cargar examen ----------

    @Test
    fun cargaElExamenPorSuId() = runBlocking {
        val viewModel = DetalleExamenViewModel(repo, "EX-101")

        val estado = viewModel.uiState.value
        assertFalse(estado.isLoading)
        assertEquals("EX-101", estado.exam?.id)
        assertEquals("María González", estado.exam?.patientName)
        assertEquals("Campimetría", estado.exam?.examType)
    }

    @Test
    fun examenQueNoExiste_quedaVacio() = runBlocking {
        val viewModel = DetalleExamenViewModel(repo, "EX-999")

        val estado = viewModel.uiState.value
        assertFalse(estado.isLoading)
        assertNull(estado.exam)
    }

    @Test
    fun sinId_sigueCargando() = runBlocking {
        val viewModel = DetalleExamenViewModel(repo)

        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.exam)
    }

    @Test
    fun cambiarDeExamen_muestraElNuevo() = runBlocking {
        val viewModel = DetalleExamenViewModel(repo, "EX-101")

        viewModel.loadExam("EX-102")

        assertEquals("EX-102", viewModel.uiState.value.exam?.id)
        assertEquals("Juan Pérez", viewModel.uiState.value.exam?.patientName)
    }

    // ---------- Validar examen ----------

    @Test
    fun validar_cambiaEstadoAValidado() = runBlocking {
        val viewModel = DetalleExamenViewModel(repo, "EX-101")

        viewModel.validateExam()

        val guardado = repo.examenes.value.first { it.id == "EX-101" }
        assertEquals("Validado", guardado.status)
        // La pantalla se actualiza sola al cambiar la base de datos
        assertEquals("Validado", viewModel.uiState.value.exam?.status)
    }

    @Test
    fun validar_noCambiaLosDemasDatos() = runBlocking {
        val viewModel = DetalleExamenViewModel(repo, "EX-101")

        viewModel.validateExam()

        val guardado = repo.examenes.value.first { it.id == "EX-101" }
        assertEquals("María González", guardado.patientName)
        assertEquals("Campimetría", guardado.examType)
        assertEquals(2, repo.examenes.value.size) // no se creó un examen nuevo
    }

    @Test
    fun validar_sinExamenCargado_noHaceNada() = runBlocking {
        val viewModel = DetalleExamenViewModel(repo, "EX-999")

        viewModel.validateExam()

        assertEquals("Pendiente", repo.examenes.value.first { it.id == "EX-101" }.status)
        assertEquals(2, repo.examenes.value.size)
    }
}
