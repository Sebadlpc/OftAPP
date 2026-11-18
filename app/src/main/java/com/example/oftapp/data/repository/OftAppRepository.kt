package com.example.oftapp.data.repository

import com.example.oftapp.data.local.entity.ConsultaEntity
import com.example.oftapp.data.local.entity.DocumentoEntity
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.data.local.entity.PacienteEntity
import com.example.oftapp.data.remote.NetworkResult
import com.example.oftapp.data.remote.dto.FhirBundleDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repositorio central de OftAPP que actúa como Única Fuente de la Verdad (Single Source of Truth).
 *
 * Abstrae las fuentes de datos locales (Room DAOs) y remotas (Microservicio Spring Boot
 * y API HL7 HAPI FHIR), implementando la estrategia Offline-First con sincronización reactiva.
 */
interface OftAppRepository {

    /**
     * Expone el estado de conectividad en tiempo real (true = Online, false = Modo Offline).
     */
    val isOnline: StateFlow<Boolean>
        get() = kotlinx.coroutines.flow.MutableStateFlow(true)

    /**
     * Obtiene el flujo reactivo de todos los exámenes registrados, ordenados cronológicamente descendente.
     */
    fun getAllExams(): Flow<List<ExamEntity>>

    /**
     * Busca exámenes según un término de búsqueda coincidente en nombre del paciente, tipo de examen,
     * identificador o sucursal.
     */
    fun searchExams(query: String): Flow<List<ExamEntity>>

    /**
     * Obtiene un examen específico mediante su identificador único.
     */
    fun getExamById(id: String): Flow<ExamEntity?>

    /**
     * Obtiene los exámenes asociados a un paciente específico por su nombre.
     */
    fun getExamsByPatient(patientName: String): Flow<List<ExamEntity>>

    /**
     * Flujo reactivo con la cantidad de exámenes guardados localmente pendientes de sincronizar.
     */
    fun getPendingExamsCount(): Flow<Int> = kotlinx.coroutines.flow.flowOf(0)

    /**
     * Sincroniza los exámenes pendientes hacia el servidor central Spring Boot.
     */
    suspend fun synchronizePendingExams(): NetworkResult<Int> = NetworkResult.Success(0)

    /**
     * Consulta observaciones clínicas interoperables desde la API externa HL7 HAPI FHIR.
     */
    suspend fun fetchFhirObservations(): NetworkResult<FhirBundleDto> = NetworkResult.Success(FhirBundleDto())

    /**
     * Inserta un examen clínico. Si hay red, lo envía al servidor remoto; si falla o está offline,
     * lo almacena en Room con estado "PENDIENTE" de forma transparente para el usuario.
     */
    suspend fun insertExam(exam: ExamEntity)

    /**
     * Actualiza el estado clínico y notas de un examen local y remotamente.
     */
    suspend fun updateExamStatus(examId: String, newStatus: String, notes: String? = null) {}

    /**
     * Inserta o actualiza un paciente en la base de datos.
     */
    suspend fun insertPaciente(paciente: PacienteEntity)

    /**
     * Inserta o actualiza una consulta médica en la base de datos.
     */
    suspend fun insertConsulta(consulta: ConsultaEntity)

    /**
     * Inserta o actualiza un documento anexo (PDF, imagen) asociado a un examen.
     */
    suspend fun insertDocumento(documento: DocumentoEntity)

    /**
     * Pre-pobla la base de datos con registros iniciales de prueba si está vacía.
     */
    suspend fun seedDatabaseIfNeeded()
}
