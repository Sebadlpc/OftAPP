package com.example.oftapp.data.repository

import com.example.oftapp.data.local.dao.ConsultaDao
import com.example.oftapp.data.local.dao.DocumentoDao
import com.example.oftapp.data.local.dao.ExamDao
import com.example.oftapp.data.local.dao.PacienteDao
import com.example.oftapp.data.local.entity.ConsultaEntity
import com.example.oftapp.data.local.entity.DocumentoEntity
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.data.local.entity.PacienteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class OftAppRepositoryTest {

    private lateinit var fakeExamDao: FakeExamDao
    private lateinit var fakePacienteDao: FakePacienteDao
    private lateinit var fakeConsultaDao: FakeConsultaDao
    private lateinit var fakeDocumentoDao: FakeDocumentoDao
    private lateinit var repository: OftAppRepositoryImpl

    @Before
    fun setUp() {
        fakeExamDao = FakeExamDao()
        fakePacienteDao = FakePacienteDao()
        fakeConsultaDao = FakeConsultaDao()
        fakeDocumentoDao = FakeDocumentoDao()
        repository = OftAppRepositoryImpl(
            examDao = fakeExamDao,
            pacienteDao = fakePacienteDao,
            consultaDao = fakeConsultaDao,
            documentoDao = fakeDocumentoDao,
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    @Test
    fun seedDatabaseIfNeeded_seedsDataWhenEmpty() = runBlocking {
        // Inicialmente vacío
        assertEquals(0, repository.getAllExams().first().size)

        repository.seedDatabaseIfNeeded()

        val exams = repository.getAllExams().first()
        assertTrue("Los exámenes deben haber sido insertados", exams.isNotEmpty())
        assertEquals(4, exams.size)

        // Comprobar que no duplica al ejecutar nuevamente
        repository.seedDatabaseIfNeeded()
        assertEquals(4, repository.getAllExams().first().size)
    }

    @Test
    fun insertAndGetExamById_worksCorrectly() = runBlocking {
        val testExam = ExamEntity(
            id = "test-123",
            patientName = "Paciente Test",
            examType = "Biomicroscopía",
            eye = "OD",
            status = "Completado",
            branch = "Sucursal Norte",
            doctorName = "Dr. Prueba"
        )

        repository.insertExam(testExam)

        val retrieved = repository.getExamById("test-123").first()
        assertNotNull(retrieved)
        assertEquals("Paciente Test", retrieved?.patientName)
        assertEquals("Biomicroscopía", retrieved?.examType)
    }

    @Test
    fun searchExams_returnsFilteredResults() = runBlocking {
        repository.seedDatabaseIfNeeded()

        val results = repository.searchExams("Tonometría").first()
        assertEquals(1, results.size)
        assertEquals("Tonometría", results.first().examType)

        val emptyQueryResults = repository.searchExams("   ").first()
        assertEquals(4, emptyQueryResults.size)
    }

    @Test
    fun getExamsByPatient_returnsMatchingPatientExams() = runBlocking {
        repository.seedDatabaseIfNeeded()

        val results = repository.getExamsByPatient("Juan Pérez").first()
        assertEquals(1, results.size)
        assertEquals("Juan Pérez", results.first().patientName)
    }

    // --- Fake DAOs para pruebas unitarias sin emulador ---

    private class FakeExamDao : ExamDao {
        val examsFlow = MutableStateFlow<List<ExamEntity>>(emptyList())

        override fun getAllExams(): Flow<List<ExamEntity>> = examsFlow

        override fun getExamById(id: String): Flow<ExamEntity?> =
            examsFlow.map { list -> list.find { it.id == id } }

        override suspend fun findExamById(id: String): ExamEntity? =
            examsFlow.value.find { it.id == id }

        override fun searchExams(query: String): Flow<List<ExamEntity>> =
            examsFlow.map { list ->
                list.filter {
                    it.patientName.contains(query, ignoreCase = true) ||
                            it.examType.contains(query, ignoreCase = true) ||
                            it.id.contains(query, ignoreCase = true) ||
                            it.branch.contains(query, ignoreCase = true)
                }
            }

        override fun getExamsByPatientName(patientName: String): Flow<List<ExamEntity>> =
            examsFlow.map { list -> list.filter { it.patientName == patientName } }

        override fun getExamsByConsultaId(consultaId: String): Flow<List<ExamEntity>> =
            examsFlow.map { list -> list.filter { it.consultaId == consultaId } }

        override suspend fun insertExam(exam: ExamEntity): Long {
            examsFlow.value = examsFlow.value.filterNot { it.id == exam.id } + exam
            return 1L
        }

        override suspend fun insertExams(exams: List<ExamEntity>): List<Long> {
            val currentMap = examsFlow.value.associateBy { it.id }.toMutableMap()
            exams.forEach { currentMap[it.id] = it }
            examsFlow.value = currentMap.values.toList()
            return exams.map { 1L }
        }

        override suspend fun updateExam(exam: ExamEntity): Int = 1
        override suspend fun deleteExam(exam: ExamEntity): Int = 1
        override suspend fun deleteExamById(id: String): Int = 1
    }

    private class FakePacienteDao : PacienteDao {
        val pacientesFlow = MutableStateFlow<List<PacienteEntity>>(emptyList())

        override fun getAllPacientes(): Flow<List<PacienteEntity>> = pacientesFlow
        override fun getPacienteById(id: String): Flow<PacienteEntity?> =
            pacientesFlow.map { list -> list.find { it.id == id } }

        override suspend fun findPacienteById(id: String): PacienteEntity? =
            pacientesFlow.value.find { it.id == id }

        override fun getPacienteByRut(rut: String): Flow<PacienteEntity?> =
            pacientesFlow.map { list -> list.find { it.rut == rut } }

        override fun searchPacientes(query: String): Flow<List<PacienteEntity>> =
            pacientesFlow.map { list -> list.filter { it.fullName.contains(query, ignoreCase = true) } }

        override suspend fun insertPaciente(paciente: PacienteEntity): Long {
            pacientesFlow.value = pacientesFlow.value.filterNot { it.id == paciente.id } + paciente
            return 1L
        }

        override suspend fun insertPacientes(pacientes: List<PacienteEntity>): List<Long> {
            val currentMap = pacientesFlow.value.associateBy { it.id }.toMutableMap()
            pacientes.forEach { currentMap[it.id] = it }
            pacientesFlow.value = currentMap.values.toList()
            return pacientes.map { 1L }
        }

        override suspend fun updatePaciente(paciente: PacienteEntity): Int = 1
        override suspend fun deletePaciente(paciente: PacienteEntity): Int = 1
        override suspend fun deletePacienteById(id: String): Int = 1
    }

    private class FakeConsultaDao : ConsultaDao {
        val consultasFlow = MutableStateFlow<List<ConsultaEntity>>(emptyList())

        override fun getAllConsultas(): Flow<List<ConsultaEntity>> = consultasFlow
        override fun getConsultaById(id: String): Flow<ConsultaEntity?> =
            consultasFlow.map { list -> list.find { it.id == id } }

        override suspend fun findConsultaById(id: String): ConsultaEntity? =
            consultasFlow.value.find { it.id == id }

        override fun getConsultasByPacienteId(pacienteId: String): Flow<List<ConsultaEntity>> =
            consultasFlow.map { list -> list.filter { it.pacienteId == pacienteId } }

        override suspend fun insertConsulta(consulta: ConsultaEntity): Long {
            consultasFlow.value = consultasFlow.value.filterNot { it.id == consulta.id } + consulta
            return 1L
        }

        override suspend fun insertConsultas(consultas: List<ConsultaEntity>): List<Long> {
            val currentMap = consultasFlow.value.associateBy { it.id }.toMutableMap()
            consultas.forEach { currentMap[it.id] = it }
            consultasFlow.value = currentMap.values.toList()
            return consultas.map { 1L }
        }

        override suspend fun updateConsulta(consulta: ConsultaEntity): Int = 1
        override suspend fun deleteConsulta(consulta: ConsultaEntity): Int = 1
        override suspend fun deleteConsultaById(id: String): Int = 1
    }

    private class FakeDocumentoDao : DocumentoDao {
        val documentosFlow = MutableStateFlow<List<DocumentoEntity>>(emptyList())

        override fun getAllDocumentos(): Flow<List<DocumentoEntity>> = documentosFlow
        override fun getDocumentoById(id: String): Flow<DocumentoEntity?> =
            documentosFlow.map { list -> list.find { it.id == id } }

        override suspend fun findDocumentoById(id: String): DocumentoEntity? =
            documentosFlow.value.find { it.id == id }

        override fun getDocumentosByExamId(examId: String): Flow<List<DocumentoEntity>> =
            documentosFlow.map { list -> list.filter { it.examId == examId } }

        override suspend fun insertDocumento(documento: DocumentoEntity): Long {
            documentosFlow.value = documentosFlow.value.filterNot { it.id == documento.id } + documento
            return 1L
        }

        override suspend fun insertDocumentos(documentos: List<DocumentoEntity>): List<Long> {
            val currentMap = documentosFlow.value.associateBy { it.id }.toMutableMap()
            documentos.forEach { currentMap[it.id] = it }
            documentosFlow.value = currentMap.values.toList()
            return documentos.map { 1L }
        }

        override suspend fun updateDocumento(documento: DocumentoEntity): Int = 1
        override suspend fun deleteDocumento(documento: DocumentoEntity): Int = 1
        override suspend fun deleteDocumentoById(id: String): Int = 1
    }
}
