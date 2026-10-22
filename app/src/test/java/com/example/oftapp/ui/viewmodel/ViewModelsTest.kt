package com.example.oftapp.ui.viewmodel

import com.example.oftapp.data.local.entity.ConsultaEntity
import com.example.oftapp.data.local.entity.DocumentoEntity
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.data.local.entity.PacienteEntity
import com.example.oftapp.data.repository.OftAppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {

    private lateinit var fakeRepository: FakeOftAppRepository
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeOftAppRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun dashboardViewModel_loadsDataAndCalculatesMetrics() = runBlocking {
        val viewModel = DashboardViewModel(fakeRepository)
        viewModel.loadDashboardData()

        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(2, state.totalCount)
        assertEquals(1, state.pendingCount)
        assertEquals(1, state.validatedCount)
        assertEquals(2, state.recentExams.size)
    }

    @Test
    fun registroAtencionViewModel_validatesAndRegistersExam() = runBlocking {
        val viewModel = RegistroAtencionViewModel(fakeRepository)

        // Sin paciente ID
        viewModel.registrarAtencion()
        assertEquals("El ID del paciente es obligatorio", viewModel.uiState.value.errorMessage)

        // Llenar campos
        viewModel.onPatientIdChange("PAT-999")
        viewModel.onBranchChange("Sede Norte")
        viewModel.onExamTypeChange("Fundoscopía")
        viewModel.onEyeChange("OD")

        viewModel.registrarAtencion("Dr. Castro", "Sin anomalías")

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertNotNull(state.createdExamId)
        assertNull(state.errorMessage)
        assertEquals(1, fakeRepository.consultasFlow.value.size)
        assertEquals(1, fakeRepository.examsFlow.value.size)
        assertEquals("PAT-999", fakeRepository.examsFlow.value.first().patientName)
    }

    @Test
    fun listadoBusquedaViewModel_filtersExamsBySearchQueryAndFilter() = runBlocking {
        fakeRepository.seedDatabaseIfNeeded()
        val viewModel = ListadoBusquedaViewModel(fakeRepository)

        viewModel.onSearchQueryChanged("María")
        val state = viewModel.uiState.first { it.searchQuery == "María" }
        assertEquals(1, state.exams.size)
        assertEquals("María González", state.exams.first().patientName)

        viewModel.onSearchQueryChanged("")
        viewModel.onFilterSelect("Tonometría")
        val filterState = viewModel.uiState.first { it.selectedFilter == "Tonometría" }
        assertEquals(1, filterState.exams.size)
        assertEquals("Tonometría", filterState.exams.first().examType)
    }

    @Test
    fun detalleExamenViewModel_loadsExamAndValidates() = runBlocking {
        fakeRepository.seedDatabaseIfNeeded()
        val viewModel = DetalleExamenViewModel(fakeRepository, "EX-101")

        val state = viewModel.uiState.first { !it.isLoading && it.exam != null }
        assertNotNull(state.exam)
        assertEquals("EX-101", state.exam?.id)
        assertEquals("Pendiente", state.exam?.status)

        viewModel.validateExam()
        val updatedExam = fakeRepository.getExamById("EX-101").first()
        assertEquals("Validado", updatedExam?.status)
    }

    @Test
    fun historialClinicoViewModel_filtersByPatient() = runBlocking {
        fakeRepository.seedDatabaseIfNeeded()
        val viewModel = HistorialClinicoViewModel(fakeRepository)

        viewModel.selectPatient("Juan Pérez")
        val state = viewModel.uiState.first { it.selectedPatient == "Juan Pérez" }
        assertEquals(1, state.patientExams.size)
        assertEquals("Juan Pérez", state.patientExams.first().patientName)
    }

    @Test
    fun loginViewModel_handlesAuthenticationFlow() = runBlocking {
        val viewModel = LoginViewModel(fakeRepository)

        // Intento con credenciales inválidas
        viewModel.onEmailChange("UsuarioDesconocido")
        viewModel.login("0000")
        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertNotNull(viewModel.uiState.value.errorMessage)

        // Autenticación exitosa como Médico con credenciales ("Medico" / "1234")
        viewModel.onEmailChange("Medico")
        viewModel.login("1234")
        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
        assertEquals("Medico", state.email)
        assertEquals("Médico Oftalmólogo", state.selectedRole)
        assertNull(state.errorMessage)

        // Logout
        viewModel.logout()
        assertFalse(viewModel.uiState.value.isAuthenticated)

        // Autenticación como Tecnólogo con credenciales ("Tecnologo" / "1234")
        viewModel.onEmailChange("Tecnologo")
        viewModel.login("1234")
        val stateTecnologo = viewModel.uiState.value
        assertTrue(stateTecnologo.isAuthenticated)
        assertEquals("Tecnólogo Médico", stateTecnologo.selectedRole)
    }

    // --- Fake Repository ---

    private class FakeOftAppRepository : OftAppRepository {
        val examsFlow = MutableStateFlow<List<ExamEntity>>(emptyList())
        val consultasFlow = MutableStateFlow<List<ConsultaEntity>>(emptyList())
        val pacientesFlow = MutableStateFlow<List<PacienteEntity>>(emptyList())
        val documentosFlow = MutableStateFlow<List<DocumentoEntity>>(emptyList())

        override fun getAllExams(): Flow<List<ExamEntity>> = examsFlow

        override fun searchExams(query: String): Flow<List<ExamEntity>> =
            examsFlow.map { list ->
                if (query.isBlank()) list
                else list.filter {
                    it.patientName.contains(query, ignoreCase = true) ||
                            it.examType.contains(query, ignoreCase = true) ||
                            it.id.contains(query, ignoreCase = true)
                }
            }

        override fun getExamById(id: String): Flow<ExamEntity?> =
            examsFlow.map { list -> list.find { it.id == id } }

        override fun getExamsByPatient(patientName: String): Flow<List<ExamEntity>> =
            examsFlow.map { list -> list.filter { it.patientName.equals(patientName, ignoreCase = true) } }

        override suspend fun insertExam(exam: ExamEntity) {
            examsFlow.value = examsFlow.value.filterNot { it.id == exam.id } + exam
        }

        override suspend fun insertPaciente(paciente: PacienteEntity) {
            pacientesFlow.value = pacientesFlow.value.filterNot { it.id == paciente.id } + paciente
        }

        override suspend fun insertConsulta(consulta: ConsultaEntity) {
            consultasFlow.value = consultasFlow.value.filterNot { it.id == consulta.id } + consulta
        }

        override suspend fun insertDocumento(documento: DocumentoEntity) {
            documentosFlow.value = documentosFlow.value.filterNot { it.id == documento.id } + documento
        }

        override suspend fun seedDatabaseIfNeeded() {
            if (examsFlow.value.isEmpty()) {
                val exam1 = ExamEntity(
                    id = "EX-101",
                    patientName = "María González",
                    examType = "Campimetría",
                    eye = "OD",
                    status = "Pendiente",
                    branch = "Sede Norte",
                    doctorName = "Dr. Silva"
                )
                val exam2 = ExamEntity(
                    id = "EX-102",
                    patientName = "Juan Pérez",
                    examType = "Tonometría",
                    eye = "AO",
                    status = "Validado",
                    branch = "Sede Centro",
                    doctorName = "Dra. Morales"
                )
                examsFlow.value = listOf(exam1, exam2)
            }
        }
    }
}
