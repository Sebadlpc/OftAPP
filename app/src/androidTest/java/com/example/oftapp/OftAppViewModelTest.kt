package com.example.oftapp

import androidx.lifecycle.ViewModel
import com.example.oftapp.data.local.entity.ConsultaEntity
import com.example.oftapp.data.local.entity.DocumentoEntity
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.data.local.entity.PacienteEntity
import com.example.oftapp.data.repository.OftAppRepository
import com.example.oftapp.ui.viewmodel.AppViewModelFactory
import com.example.oftapp.ui.viewmodel.DashboardViewModel
import com.example.oftapp.ui.viewmodel.DetalleExamenViewModel
import com.example.oftapp.ui.viewmodel.HistorialClinicoViewModel
import com.example.oftapp.ui.viewmodel.ListadoBusquedaViewModel
import com.example.oftapp.ui.viewmodel.LoginViewModel
import com.example.oftapp.ui.viewmodel.RegistroAtencionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class FakeOftAppRepository : OftAppRepository {

    private val examsFlow = MutableStateFlow<List<ExamEntity>>(emptyList())
    val insertedConsultas = mutableListOf<ConsultaEntity>()
    val insertedDocumentos = mutableListOf<DocumentoEntity>()
    val insertedPacientes = mutableListOf<PacienteEntity>()
    var shouldThrowOnInsertConsulta = false

    fun setExams(exams: List<ExamEntity>) {
        examsFlow.value = exams
    }

    override fun getAllExams(): Flow<List<ExamEntity>> = examsFlow

    override fun searchExams(query: String): Flow<List<ExamEntity>> {
        val clean = query.trim()
        return examsFlow.map { list ->
            if (clean.isEmpty()) list
            else list.filter {
                it.patientName.contains(clean, ignoreCase = true) ||
                        it.examType.contains(clean, ignoreCase = true) ||
                        it.branch.contains(clean, ignoreCase = true) ||
                        it.id.contains(clean, ignoreCase = true)
            }
        }
    }

    override fun getExamById(id: String): Flow<ExamEntity?> {
        return examsFlow.map { list -> list.find { it.id == id } }
    }

    override fun getExamsByPatient(patientName: String): Flow<List<ExamEntity>> {
        return examsFlow.map { list ->
            list.filter { it.patientName.equals(patientName, ignoreCase = true) }
        }
    }

    override suspend fun insertExam(exam: ExamEntity) {
        val current = examsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == exam.id }
        if (index >= 0) {
            current[index] = exam
        } else {
            current.add(exam)
        }
        examsFlow.value = current
    }

    override suspend fun insertPaciente(paciente: PacienteEntity) {
        insertedPacientes.add(paciente)
    }

    override suspend fun insertConsulta(consulta: ConsultaEntity) {
        if (shouldThrowOnInsertConsulta) {
            throw RuntimeException("Error de base de datos")
        }
        insertedConsultas.add(consulta)
    }

    override suspend fun insertDocumento(documento: DocumentoEntity) {
        insertedDocumentos.add(documento)
    }

    override suspend fun seedDatabaseIfNeeded() {
        // No-op en pruebas unitarias
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OftAppViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeOftAppRepository

    private fun createSampleExam(
        patientName: String = "Juan Pérez",
        examType: String = "OCT Macular",
        eye: String = "AO",
        status: String = "Pendiente",
        branch: String = "Sede Central",
        doctorName: String = "Dra. Carla Ruiz"
    ): ExamEntity {
        return ExamEntity(
            consultaId = "consulta-1",
            patientName = patientName,
            examType = examType,
            eye = eye,
            date = Date(),
            status = status,
            branch = branch,
            doctorName = doctorName,
            notes = "Observación clínica de prueba",
            fileUri = null
        )
    }

    @Before
    fun setUp() {
        repository = FakeOftAppRepository()
    }

    // =========================================================================
    // 1. TESTS DE LoginViewModel
    // =========================================================================

    @Test
    fun loginConCamposVaciosRetornaMensajeDeError() = runTest {
        val viewModel = LoginViewModel(repository)

        viewModel.onEmailChange("")
        viewModel.login("")

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals("Por favor, ingrese usuario y contraseña", state.errorMessage)
    }

    @Test
    fun loginExitosoParaMedicoAsignaRolYNombreCorrecto() = runTest {
        val viewModel = LoginViewModel(repository)

        viewModel.onEmailChange("Medico")
        viewModel.login("1234")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
        assertEquals("Médico Oftalmólogo", state.selectedRole)
        assertEquals("Dra. Carla Ruiz", state.userName)
        assertNull(state.errorMessage)
    }

    @Test
    fun loginExitosoParaPacienteAsignaRolPacienteYNombreJuanPerez() = runTest {
        val viewModel = LoginViewModel(repository)

        viewModel.onEmailChange("Paciente")
        viewModel.login("1234")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
        assertEquals("Paciente", state.selectedRole)
        assertEquals("Juan Pérez", state.userName)
    }

    @Test
    fun loginConContrasenaIncorrectaFallaYMuestraError() = runTest {
        val viewModel = LoginViewModel(repository)

        viewModel.onEmailChange("Tecnologo")
        viewModel.login("9999")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals("Credenciales incorrectas. Verifique usuario y contraseña.", state.errorMessage)
    }

    @Test
    fun logoutReiniciaElEstadoDeAutenticacion() = runTest {
        val viewModel = LoginViewModel(repository)
        viewModel.onEmailChange("Admin")
        viewModel.login("1234")
        advanceUntilIdle()

        viewModel.logout()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals("", state.email)
        assertEquals("", state.userName)
    }

    // =========================================================================
    // 2. TESTS DE DashboardViewModel
    // =========================================================================

    @Test
    fun dashboardCalculaMetricasGlobalesParaTecnologoMedicoYLimitaRecientesA5() = runTest {
        repository.setExams(
            listOf(
                createSampleExam(patientName = "Juan Pérez", status = "Pendiente"),
                createSampleExam(patientName = "María López", status = "En Proceso"),
                createSampleExam(patientName = "Carlos Díaz", status = "Validado"),
                createSampleExam(patientName = "Ana Torres", status = "Completado"),
                createSampleExam(patientName = "Pedro Soto", status = "Pendiente"),
                createSampleExam(patientName = "Lucía Vera", status = "Validado")
            )
        )

        val viewModel = DashboardViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(6, state.totalCount)
        assertEquals(3, state.pendingCount)
        assertEquals(3, state.validatedCount)
        assertEquals(5, state.recentExams.size)
    }

    @Test
    fun dashboardConRolPacienteFiltraEstrictamenteSusPropiosExamenes() = runTest {
        repository.setExams(
            listOf(
                createSampleExam(patientName = "Juan Pérez", status = "Pendiente"),
                createSampleExam(patientName = "Juan Pérez", status = "Validado"),
                createSampleExam(patientName = "María López", status = "Pendiente")
            )
        )

        val viewModel = DashboardViewModel(repository)
        viewModel.setUserRole("Paciente", "Juan Pérez")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.totalCount)
        assertEquals(1, state.pendingCount)
        assertEquals(1, state.validatedCount)
        assertTrue(state.recentExams.all { it.patientName == "Juan Pérez" })
    }

    // =========================================================================
    // 3. TESTS DE DetalleExamenViewModel
    // =========================================================================

    @Test
    fun loadExamCargaElExamenPorIdYValidateExamActualizaEstadoAValidado() = runTest {
        val exam = createSampleExam(status = "Pendiente")
        repository.setExams(listOf(exam))

        val viewModel = DetalleExamenViewModel(repository, initialExamId = exam.id)
        advanceUntilIdle()

        assertEquals(exam, viewModel.uiState.value.exam)
        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.validateExam()
        advanceUntilIdle()

        assertEquals("Validado", viewModel.uiState.value.exam?.status)
    }

    // =========================================================================
    // 4. TESTS DE HistorialClinicoViewModel
    // =========================================================================

    @Test
    fun historialAplicaLazyLoadingEnBloquesDe5YCargaSiguienteBloque() = runTest {
        val manyExams = (1..8).map { index ->
            createSampleExam(patientName = "Juan Pérez", examType = "Examen $index")
        }
        repository.setExams(manyExams)

        val viewModel = HistorialClinicoViewModel(repository)
        advanceUntilIdle()

        val firstState = viewModel.uiState.value
        assertEquals(5, firstState.patientExams.size)
        assertEquals(8, firstState.totalExamsCount)
        assertTrue(firstState.hasMoreItems)

        viewModel.loadNextBlock()
        advanceTimeBy(200)
        advanceUntilIdle()

        val secondState = viewModel.uiState.value
        assertEquals(8, secondState.patientExams.size)
        assertFalse(secondState.hasMoreItems)
        assertFalse(secondState.isLoadingMore)
    }

    @Test
    fun historialConRolPacienteBloqueaBusquedaDeOtrosPacientes() = runTest {
        repository.setExams(listOf(createSampleExam(patientName = "Juan Pérez")))

        val viewModel = HistorialClinicoViewModel(repository)
        viewModel.setUserRole("Paciente", "Juan Pérez")
        advanceUntilIdle()

        viewModel.onSearchQueryChange("María López")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isRestrictedToPatient)
        assertEquals("Juan Pérez", state.selectedPatient)
        assertEquals("Juan Pérez", state.searchQuery)
    }

    // =========================================================================
    // 5. TESTS DE ListadoBusquedaViewModel
    // =========================================================================

    @Test
    fun listadoFiltraPorEstadoPendientesYRestringePorRolPaciente() = runTest {
        repository.setExams(
            listOf(
                createSampleExam(patientName = "Juan Pérez", status = "Pendiente"),
                createSampleExam(patientName = "Juan Pérez", status = "Validado"),
                createSampleExam(patientName = "María López", status = "Pendiente")
            )
        )

        val viewModel = ListadoBusquedaViewModel(repository)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onFilterSelect("Pendientes")
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.exams.size)

        viewModel.setUserRole("Paciente", "Juan Pérez")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.exams.size)
        assertEquals("Juan Pérez", viewModel.uiState.value.exams.first().patientName)

        collectJob.cancel()
    }

    // =========================================================================
    // 6. TESTS DE RegistroAtencionViewModel
    // =========================================================================

    @Test
    fun registrarAtencionValidaCamposObligatoriosEnOrden() = runTest {
        val viewModel = RegistroAtencionViewModel(repository)

        viewModel.registrarAtencion()
        assertEquals("El ID del paciente es obligatorio", viewModel.uiState.value.errorMessage)

        viewModel.onPatientIdChange("Juan Pérez")
        viewModel.registrarAtencion()
        assertEquals("Debe seleccionar una sede", viewModel.uiState.value.errorMessage)

        viewModel.onBranchChange("Sede Central")
        viewModel.registrarAtencion()
        assertEquals("Debe seleccionar el tipo de examen", viewModel.uiState.value.errorMessage)

        viewModel.onExamTypeChange("Campimetría")
        viewModel.registrarAtencion()
        assertEquals("Debe seleccionar el ojo evaluado", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun registrarAtencionExitosoConArchivoAdjuntoInsertaConsultaExamenYDocumento() = runTest {
        val viewModel = RegistroAtencionViewModel(repository)

        viewModel.onPatientIdChange("Juan Pérez")
        viewModel.onBranchChange("Sede Norte")
        viewModel.onExamTypeChange("OCT")
        viewModel.onEyeChange("OD")
        viewModel.onNotesChange("Control anual")
        viewModel.onFileSelected(uri = null, fileName = "oct_juan.pdf")

        viewModel.registrarAtencion(doctorName = "Dra. Carla Ruiz")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertFalse(state.isSaving)
        assertNotNull(state.createdExamId)
        assertNull(state.errorMessage)

        assertEquals(1, repository.insertedConsultas.size)
        assertEquals(1, repository.insertedDocumentos.size)
        assertEquals("oct_juan.pdf", repository.insertedDocumentos.first().fileName)
    }

    @Test
    fun registrarAtencionCapturaExcepcionesDelRepositorioYMuestraError() = runTest {
        repository.shouldThrowOnInsertConsulta = true

        val viewModel = RegistroAtencionViewModel(repository)
        viewModel.onPatientIdChange("Juan Pérez")
        viewModel.onBranchChange("Sede Central")
        viewModel.onExamTypeChange("OCT")
        viewModel.onEyeChange("AO")

        viewModel.registrarAtencion()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertFalse(state.isSaving)
        assertEquals("Error de base de datos", state.errorMessage)
    }

    // =========================================================================
    // 7. TESTS DE AppViewModelFactory
    // =========================================================================

    @Test
    fun factoryCreaCorrectamenteTodosLosViewModelsSoportados() {
        val factory = AppViewModelFactory(repository, examId = "123")

        assertTrue(factory.create(DashboardViewModel::class.java) is DashboardViewModel)
        assertTrue(factory.create(RegistroAtencionViewModel::class.java) is RegistroAtencionViewModel)
        assertTrue(factory.create(ListadoBusquedaViewModel::class.java) is ListadoBusquedaViewModel)
        assertTrue(factory.create(DetalleExamenViewModel::class.java) is DetalleExamenViewModel)
        assertTrue(factory.create(HistorialClinicoViewModel::class.java) is HistorialClinicoViewModel)
        assertTrue(factory.create(LoginViewModel::class.java) is LoginViewModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun factoryLanzaIllegalArgumentExceptionParaViewModelDesconocido() {
        class UnknownViewModel : ViewModel()
        val factory = AppViewModelFactory(repository)
        factory.create(UnknownViewModel::class.java)
    }
}