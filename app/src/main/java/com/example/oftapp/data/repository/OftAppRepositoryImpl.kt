package com.example.oftapp.data.repository

import android.content.Context
import com.example.oftapp.data.local.AppDatabase
import com.example.oftapp.data.local.dao.ConsultaDao
import com.example.oftapp.data.local.dao.DocumentoDao
import com.example.oftapp.data.local.dao.ExamDao
import com.example.oftapp.data.local.dao.PacienteDao
import com.example.oftapp.data.local.entity.ConsultaEntity
import com.example.oftapp.data.local.entity.DocumentoEntity
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.data.local.entity.PacienteEntity
import com.example.oftapp.data.local.security.EncryptedSessionManager
import com.example.oftapp.data.remote.HapiFhirApiService
import com.example.oftapp.data.remote.NetworkConnectivityObserver
import com.example.oftapp.data.remote.NetworkResult
import com.example.oftapp.data.remote.SpringBootApiService
import com.example.oftapp.data.remote.dto.ActualizarEstadoDto
import com.example.oftapp.data.remote.dto.ExamenSyncDto
import com.example.oftapp.data.remote.dto.FhirBundleDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Implementación del repositorio central de OftAPP con soporte Offline-First,
 * mapeo estricto hacia ExamenSyncDto y sincronización automática hacia Spring Boot.
 */
class OftAppRepositoryImpl(
    private val examDao: ExamDao,
    private val pacienteDao: PacienteDao,
    private val consultaDao: ConsultaDao,
    private val documentoDao: DocumentoDao,
    private val springBootApiService: SpringBootApiService? = null,
    private val hapiFhirApiService: HapiFhirApiService? = null,
    private val connectivityObserver: NetworkConnectivityObserver? = null,
    private val sessionManager: EncryptedSessionManager? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OftAppRepository {

    private val repositoryScope = CoroutineScope(ioDispatcher + SupervisorJob())

    private val fallbackOnlineFlow = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean>
        get() = connectivityObserver?.isOnline ?: fallbackOnlineFlow.asStateFlow()

    override fun getAllExams(): Flow<List<ExamEntity>> {
        return examDao.getAllExams()
    }

    override fun searchExams(query: String): Flow<List<ExamEntity>> {
        val sanitizedQuery = query.trim()
        return if (sanitizedQuery.isEmpty()) {
            examDao.getAllExams()
        } else {
            examDao.searchExams(sanitizedQuery)
        }
    }

    override fun getExamById(id: String): Flow<ExamEntity?> {
        return examDao.getExamById(id)
    }

    override fun getExamsByPatient(patientName: String): Flow<List<ExamEntity>> {
        return examDao.getExamsByPatientName(patientName)
    }

    override fun getPendingExamsCount(): Flow<Int> {
        return examDao.getPendingExamsCountFlow()
    }

    override suspend fun insertExam(exam: ExamEntity): Unit = withContext(ioDispatcher) {
        // En estrategia Offline-First, los nuevos exámenes registrados localmente se resguardan
        // en Room SQLite con syncStatus = "PENDIENTE" para ser sincronizados por lote mediante POST /api/v1/sync.
        val finalExam = if (exam.remoteId != null) {
            exam.copy(syncStatus = "SINCRONIZADO", lastSyncedAt = exam.lastSyncedAt ?: Date())
        } else {
            exam.copy(syncStatus = "PENDIENTE", remoteId = null)
        }
        examDao.insertExam(finalExam)
    }

    override suspend fun updateExamStatus(
        examId: String,
        newStatus: String,
        notes: String?
    ): Unit = withContext(ioDispatcher) {
        val localExam = examDao.findExamById(examId) ?: return@withContext

        var syncStatus = "SINCRONIZADO"
        if (localExam.remoteId != null && springBootApiService != null) {
            try {
                val response = springBootApiService.updateEstado(
                    id = localExam.remoteId,
                    request = ActualizarEstadoDto(
                        estado = if (newStatus.contains("Valid", true)) "VALIDADO" else newStatus,
                        notas = notes ?: localExam.notes
                    )
                )
                if (!response.isSuccessful) {
                    syncStatus = "PENDIENTE"
                }
            } catch (_: Exception) {
                syncStatus = "PENDIENTE"
            }
        } else {
            syncStatus = "PENDIENTE"
        }

        val updated = localExam.copy(
            status = newStatus,
            notes = notes ?: localExam.notes,
            syncStatus = syncStatus,
            lastSyncedAt = if (syncStatus == "SINCRONIZADO") Date() else localExam.lastSyncedAt
        )
        examDao.updateExam(updated)
    }

    override suspend fun synchronizePendingExams(): NetworkResult<Int> = withContext(ioDispatcher) {
        if (springBootApiService == null) {
            return@withContext NetworkResult.Error("Servicio de sincronización no disponible (SpringBootApiService no configurado)")
        }

        try {
            val pendingExams = examDao.getPendingExams()
            if (pendingExams.isEmpty()) {
                return@withContext NetworkResult.Success(0)
            }

            // Mapeo estricto de cada examen PENDIENTE hacia el DTO de Spring Boot
            val dtoList = pendingExams.map { it.toExamenSyncDto() }
            val response = springBootApiService.syncExamenes(dtoList)

            if (response.isSuccessful && response.body() != null) {
                val remoteList = response.body()!!
                val now = Date()

                pendingExams.forEachIndexed { index, local ->
                    val remoteId = remoteList.getOrNull(index)?.id
                    examDao.updateSyncStatus(
                        localId = local.id,
                        syncStatus = "SINCRONIZADO",
                        remoteId = remoteId,
                        syncedAt = now
                    )
                }
                return@withContext NetworkResult.Success(remoteList.size)
            } else {
                return@withContext NetworkResult.Error("Error en servidor Spring Boot: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Failed to connect") == true || e.message?.contains("Connection refused") == true ->
                    "No se pudo conectar con el servidor (10.0.2.2:8080)"
                else ->
                    "No se pudo conectar con el servidor (10.0.2.2:8080): ${e.localizedMessage ?: e.message}"
            }
            return@withContext NetworkResult.Error(errorMessage, e)
        }
    }

    override suspend fun fetchFhirObservations(): NetworkResult<FhirBundleDto> = withContext(ioDispatcher) {
        if (hapiFhirApiService == null) {
            return@withContext NetworkResult.Error("Cliente HAPI FHIR no configurado")
        }
        try {
            val response = hapiFhirApiService.getObservations(count = 10)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Error en HL7 FHIR: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Fallo al conectar con HAPI FHIR: ${e.message}", e)
        }
    }

    override suspend fun insertPaciente(paciente: PacienteEntity): Unit = withContext(ioDispatcher) {
        pacienteDao.insertPaciente(paciente)
    }

    override suspend fun insertConsulta(consulta: ConsultaEntity): Unit = withContext(ioDispatcher) {
        consultaDao.insertConsulta(consulta)
    }

    override suspend fun insertDocumento(documento: DocumentoEntity): Unit = withContext(ioDispatcher) {
        documentoDao.insertDocumento(documento)
    }

    override suspend fun seedDatabaseIfNeeded(): Unit = withContext(ioDispatcher) {
        val currentExams = examDao.getAllExams().first()
        if (currentExams.isNotEmpty()) {
            return@withContext
        }

        val paciente1 = PacienteEntity("paciente-001", "12.345.678-9", "Juan Pérez", "juan.perez@email.com", "+56 9 1234 5678")
        val paciente2 = PacienteEntity("paciente-002", "9.876.543-2", "María López", "maria.lopez@email.com", "+56 9 8765 4321")
        val paciente3 = PacienteEntity("paciente-003", "15.432.198-K", "Carlos Gómez", "carlos.gomez@email.com", "+56 9 7654 3210")
        val paciente4 = PacienteEntity("paciente-004", "18.765.432-1", "Ana Martínez", "ana.martinez@email.com", "+56 9 9876 5432")
        pacienteDao.insertPacientes(listOf(paciente1, paciente2, paciente3, paciente4))

        val consulta1 = ConsultaEntity("consulta-001", paciente1.id, Date(System.currentTimeMillis() - 86400000L * 3), "Sucursal Central", "Dr. Alejandro Silva")
        val consulta2 = ConsultaEntity("consulta-002", paciente2.id, Date(System.currentTimeMillis() - 86400000L * 2), "Sucursal Las Condes", "Dra. Constanza Morales")
        val consulta3 = ConsultaEntity("consulta-003", paciente3.id, Date(System.currentTimeMillis() - 86400000L), "Sucursal Providencia", "Dr. Roberto Fuentes")
        val consulta4 = ConsultaEntity("consulta-004", paciente4.id, Date(), "Sucursal Central", "Dra. Valentina Castro")
        consultaDao.insertConsultas(listOf(consulta1, consulta2, consulta3, consulta4))

        val exam1 = ExamEntity("ID-1234", consulta1.id, paciente1.fullName, "Tonometría", "Ambos Ojos (AO)", consulta1.fecha, "Completado", consulta1.sucursal, consulta1.profesionalACargo, "Presión intraocular: OD 14 mmHg, OI 15 mmHg. Parámetros normales.", null, "SINCRONIZADO", 1L, Date())
        val exam2 = ExamEntity("ID-5678", consulta2.id, paciente2.fullName, "Campo Visual", "Ojo Derecho (OD)", consulta2.fecha, "En Proceso", consulta2.sucursal, consulta2.profesionalACargo, "Ligera disminución de sensibilidad periférica cuadrante superior temporal.", null, "SINCRONIZADO", 2L, Date())
        val exam3 = ExamEntity("ID-9012", consulta3.id, paciente3.fullName, "OCT Retinal", "Ojo Izquierdo (OI)", consulta3.fecha, "Pendiente", consulta3.sucursal, consulta3.profesionalACargo, "Control anual por antecedentes familiares de glaucoma. Espesor de fibras estable.", null, "PENDIENTE", null, null)
        val exam4 = ExamEntity("ID-3456", consulta4.id, paciente4.fullName, "Fundoscopía", "Ambos Ojos (AO)", consulta4.fecha, "Completado", consulta4.sucursal, consulta4.profesionalACargo, "Papila de bordes netos, excavación fisiológica 0.3, mácula sin alteraciones.", null, "SINCRONIZADO", 4L, Date())
        examDao.insertExams(listOf(exam1, exam2, exam3, exam4))

        val doc1 = DocumentoEntity("doc-001", exam1.id, "informe_tonometria_id1234.pdf", null, exam1.date)
        val doc2 = DocumentoEntity("doc-002", exam2.id, "grafico_campo_visual_id5678.png", null, exam2.date)
        documentoDao.insertDocumentos(listOf(doc1, doc2))
    }

    private fun ExamEntity.toExamenSyncDto(): ExamenSyncDto {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val eyeCode = when {
            this.eye.contains("OD", ignoreCase = true) -> "OD"
            this.eye.contains("OI", ignoreCase = true) -> "OI"
            else -> "Bifocal"
        }
        val statusCode = if (this.status.equals("Completado", true) || this.status.equals("Validado", true)) {
            "VALIDADO"
        } else {
            "PENDIENTE"
        }

        val patientIdNumber = when {
            this.patientName.contains("Juan", ignoreCase = true) -> 1L
            this.patientName.contains("María", ignoreCase = true) || this.patientName.contains("Maria", ignoreCase = true) -> 2L
            this.patientName.contains("Carlos", ignoreCase = true) -> 3L
            this.patientName.contains("Ana", ignoreCase = true) -> 4L
            else -> {
                val digits = this.patientName.filter { it.isDigit() }
                digits.toLongOrNull() ?: 1L
            }
        }

        return ExamenSyncDto(
            id = this.remoteId,
            pacienteId = patientIdNumber,
            pacienteNombre = this.patientName.ifBlank { "Paciente General" },
            tipoExamen = this.examType.ifBlank { "Examen General" },
            ojoEvaluado = eyeCode,
            fecha = isoFormat.format(this.date),
            estado = statusCode,
            doctor = this.doctorName.ifBlank { "Dr. Oftalmólogo" },
            notas = this.notes.ifBlank { null },
            documentoUrl = this.fileUri?.toString()
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: OftAppRepositoryImpl? = null

        fun getInstance(
            examDao: ExamDao,
            pacienteDao: PacienteDao,
            consultaDao: ConsultaDao,
            documentoDao: DocumentoDao,
            springBootApiService: SpringBootApiService? = null,
            hapiFhirApiService: HapiFhirApiService? = null,
            connectivityObserver: NetworkConnectivityObserver? = null,
            sessionManager: EncryptedSessionManager? = null,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO
        ): OftAppRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OftAppRepositoryImpl(
                    examDao = examDao,
                    pacienteDao = pacienteDao,
                    consultaDao = consultaDao,
                    documentoDao = documentoDao,
                    springBootApiService = springBootApiService,
                    hapiFhirApiService = hapiFhirApiService,
                    connectivityObserver = connectivityObserver,
                    sessionManager = sessionManager,
                    ioDispatcher = ioDispatcher
                ).also { INSTANCE = it }
            }
        }

        fun fromDatabase(
            database: AppDatabase,
            context: Context? = null,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO
        ): OftAppRepositoryImpl {
            val springBootService = context?.let { SpringBootApiService.create(it) }
            val fhirService = HapiFhirApiService.create()
            val observer = context?.let { NetworkConnectivityObserver.getInstance(it) }
            val session = context?.let { EncryptedSessionManager.getInstance(it) }

            return getInstance(
                examDao = database.examDao(),
                pacienteDao = database.pacienteDao(),
                consultaDao = database.consultaDao(),
                documentoDao = database.documentoDao(),
                springBootApiService = springBootService,
                hapiFhirApiService = fhirService,
                connectivityObserver = observer,
                sessionManager = session,
                ioDispatcher = ioDispatcher
            )
        }
    }
}
