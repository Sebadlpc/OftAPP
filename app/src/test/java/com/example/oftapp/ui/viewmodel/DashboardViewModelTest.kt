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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias de la pantalla Dashboard (DashboardViewModel):
 * contadores de exámenes, lista de recientes y privacidad por rol.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- Contadores ----------

    @Test
    fun sinExamenes_contadoresEnCero() = runBlocking {
        val viewModel = DashboardViewModel(RepositorioFalso())

        val estado = viewModel.uiState.value
        assertEquals(0, estado.totalCount)
        assertEquals(0, estado.pendingCount)
        assertEquals(0, estado.validatedCount)
        assertTrue(estado.recentExams.isEmpty())
    }

    @Test
    fun terminaDeCargar() = runBlocking {
        val viewModel = DashboardViewModel(RepositorioFalso(listOf(examen("EX-1"))))

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun cuentaTotalPendientesYValidados() = runBlocking {
        val repo = RepositorioFalso(
            listOf(
                examen("EX-1", estado = "Pendiente"),
                examen("EX-2", estado = "En Proceso"),
                examen("EX-3", estado = "Validado"),
                examen("EX-4", estado = "Completado"),
                examen("EX-5", estado = "Observado")
            )
        )
        val viewModel = DashboardViewModel(repo)

        val estado = viewModel.uiState.value
        assertEquals(5, estado.totalCount)
        assertEquals(2, estado.pendingCount)   // Pendiente + En Proceso
        assertEquals(2, estado.validatedCount) // Validado + Completado
    }

    @Test
    fun estados_ignoranMayusculas() = runBlocking {
        val repo = RepositorioFalso(
            listOf(
                examen("EX-1", estado = "pendiente"),
                examen("EX-2", estado = "VALIDADO")
            )
        )
        val viewModel = DashboardViewModel(repo)

        assertEquals(1, viewModel.uiState.value.pendingCount)
        assertEquals(1, viewModel.uiState.value.validatedCount)
    }

    // ---------- Exámenes recientes ----------

    @Test
    fun recientes_muestraMaximoCinco() = runBlocking {
        val sieteExamenes = (1..7).map { examen("EX-$it") }
        val viewModel = DashboardViewModel(RepositorioFalso(sieteExamenes))

        val estado = viewModel.uiState.value
        assertEquals(7, estado.totalCount)
        assertEquals(5, estado.recentExams.size)
        assertEquals("EX-1", estado.recentExams.first().id)
    }

    @Test
    fun seActualizaAlAgregarUnExamen() = runBlocking {
        val repo = RepositorioFalso(listOf(examen("EX-1")))
        val viewModel = DashboardViewModel(repo)
        assertEquals(1, viewModel.uiState.value.totalCount)

        repo.insertExam(examen("EX-2", estado = "Validado"))

        assertEquals(2, viewModel.uiState.value.totalCount)
        assertEquals(1, viewModel.uiState.value.validatedCount)
    }

    // ---------- Privacidad por rol ----------

    @Test
    fun paciente_soloVeSusExamenes() = runBlocking {
        val repo = RepositorioFalso(
            listOf(
                examen("EX-1", paciente = "Juan Pérez"),
                examen("EX-2", paciente = "Juan Pérez", estado = "Validado"),
                examen("EX-3", paciente = "María González"),
                examen("EX-4", paciente = "Pedro Soto")
            )
        )
        val viewModel = DashboardViewModel(repo)

        viewModel.setUserRole("Paciente", "Juan Pérez")

        val estado = viewModel.uiState.value
        assertEquals(2, estado.totalCount)
        assertTrue(estado.recentExams.all { it.patientName == "Juan Pérez" })
    }

    @Test
    fun medico_veTodosLosExamenes() = runBlocking {
        val repo = RepositorioFalso(
            listOf(
                examen("EX-1", paciente = "Juan Pérez"),
                examen("EX-2", paciente = "María González"),
                examen("EX-3", paciente = "Pedro Soto")
            )
        )
        val viewModel = DashboardViewModel(repo)

        viewModel.setUserRole("Médico Oftalmólogo")

        assertEquals(3, viewModel.uiState.value.totalCount)
    }
}
