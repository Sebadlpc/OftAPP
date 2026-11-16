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
 * Pruebas unitarias de la pantalla Registro de Atención (RegistroAtencionViewModel):
 * validación de campos obligatorios y guardado de consulta, examen y documento.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegistroAtencionViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: RepositorioFalso
    private lateinit var viewModel: RegistroAtencionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = RepositorioFalso()
        viewModel = RegistroAtencionViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Llena el formulario completo con datos válidos. */
    private fun llenarFormulario() {
        viewModel.onPatientIdChange("PAT-001")
        viewModel.onBranchChange("Sede Norte")
        viewModel.onExamTypeChange("Tonometría")
        viewModel.onEyeChange("OI")
    }

    // ---------- Campos obligatorios ----------

    @Test
    fun sinPaciente_muestraError() = runBlocking {
        viewModel.registrarAtencion()

        assertEquals("El ID del paciente es obligatorio", viewModel.uiState.value.errorMessage)
        assertTrue(repo.examenes.value.isEmpty())
    }

    @Test
    fun pacienteSoloEspacios_muestraError() = runBlocking {
        llenarFormulario()
        viewModel.onPatientIdChange("   ")

        viewModel.registrarAtencion()

        assertEquals("El ID del paciente es obligatorio", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun sinSede_muestraError() = runBlocking {
        viewModel.onPatientIdChange("PAT-001")

        viewModel.registrarAtencion()

        assertEquals("Debe seleccionar una sede", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun sinTipoDeExamen_muestraError() = runBlocking {
        viewModel.onPatientIdChange("PAT-001")
        viewModel.onBranchChange("Sede Norte")

        viewModel.registrarAtencion()

        assertEquals("Debe seleccionar el tipo de examen", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun sinOjo_muestraError() = runBlocking {
        viewModel.onPatientIdChange("PAT-001")
        viewModel.onBranchChange("Sede Norte")
        viewModel.onExamTypeChange("Tonometría")

        viewModel.registrarAtencion()

        assertEquals("Debe seleccionar el ojo evaluado", viewModel.uiState.value.errorMessage)
        assertTrue(repo.examenes.value.isEmpty())
    }

    @Test
    fun escribirDeNuevo_borraElError() = runBlocking {
        viewModel.registrarAtencion()
        assertEquals("El ID del paciente es obligatorio", viewModel.uiState.value.errorMessage)

        viewModel.onPatientIdChange("P")

        assertNull(viewModel.uiState.value.errorMessage)
    }

    // ---------- Guardado exitoso ----------

    @Test
    fun formularioCompleto_guardaConsultaYExamen() = runBlocking {
        llenarFormulario()

        viewModel.registrarAtencion(doctorName = "Dra. Ruiz")

        val estado = viewModel.uiState.value
        assertTrue(estado.isSuccess)
        assertFalse(estado.isSaving)
        assertNull(estado.errorMessage)

        val consulta = repo.consultas.value.single()
        assertEquals("PAT-001", consulta.pacienteId)
        assertEquals("Sede Norte", consulta.sucursal)
        assertEquals("Dra. Ruiz", consulta.profesionalACargo)

        val examenGuardado = repo.examenes.value.single()
        assertEquals(estado.createdExamId, examenGuardado.id)
        assertEquals(consulta.id, examenGuardado.consultaId)
        assertEquals("Tonometría", examenGuardado.examType)
        assertEquals("OI", examenGuardado.eye)
        assertEquals("Pendiente", examenGuardado.status)
    }

    @Test
    fun sinArchivo_noGuardaDocumento() = runBlocking {
        llenarFormulario()

        viewModel.registrarAtencion()

        assertTrue(repo.documentos.value.isEmpty())
    }

    @Test
    fun conArchivo_guardaDocumentoDelExamen() = runBlocking {
        llenarFormulario()
        viewModel.onFileSelected(uri = null, fileName = "informe.pdf")

        viewModel.registrarAtencion()

        val documento = repo.documentos.value.single()
        assertEquals("informe.pdf", documento.fileName)
        assertEquals(viewModel.uiState.value.createdExamId, documento.examId)
    }

    @Test
    fun usaLasNotasDelFormulario() = runBlocking {
        llenarFormulario()
        viewModel.onNotesChange("Presión ocular normal")

        viewModel.registrarAtencion()

        assertEquals("Presión ocular normal", repo.examenes.value.single().notes)
    }

    @Test
    fun notasEnviadasAlRegistrar_tienenPrioridad() = runBlocking {
        llenarFormulario()
        viewModel.onNotesChange("Nota del formulario")

        viewModel.registrarAtencion(notes = "Nota del médico")

        assertEquals("Nota del médico", repo.examenes.value.single().notes)
    }

    // ---------- Errores y reinicio ----------

    @Test
    fun errorEnBaseDeDatos_muestraMensaje() = runBlocking {
        repo.fallarAlGuardar = true
        llenarFormulario()

        viewModel.registrarAtencion()

        val estado = viewModel.uiState.value
        assertFalse(estado.isSuccess)
        assertFalse(estado.isSaving)
        assertEquals("No se pudo guardar en la base de datos", estado.errorMessage)
        assertTrue(repo.examenes.value.isEmpty())
    }

    @Test
    fun resetState_limpiaElFormulario() = runBlocking {
        llenarFormulario()
        viewModel.registrarAtencion()

        viewModel.resetState()

        val estado = viewModel.uiState.value
        assertEquals("", estado.patientId)
        assertEquals("", estado.branch)
        assertFalse(estado.isSuccess)
        assertNull(estado.createdExamId)
    }
}
