package com.example.oftapp.data.repository

import com.example.oftapp.data.local.entity.ConsultaEntity
import com.example.oftapp.data.local.entity.DocumentoEntity
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.data.local.entity.PacienteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio central de OftAPP que actúa como Única Fuente de la Verdad (Single Source of Truth).
 *
 * Abstrae las fuentes de datos (Room DAOs) y expone flujos reactivos ([Flow]) y operaciones
 * suspendidas ([suspend]) para la persistencia y consulta de exámenes, pacientes, consultas
 * y documentos clínicos.
 */
interface OftAppRepository {

    /**
     * Obtiene el flujo reactivo de todos los exámenes registrados, ordenados cronológicamente descendente.
     */
    fun getAllExams(): Flow<List<ExamEntity>>

    /**
     * Busca exámenes según un término de búsqueda coincidente en nombre del paciente, tipo de examen,
     * identificador o sucursal.
     *
     * @param query Criterio o texto de búsqueda.
     */
    fun searchExams(query: String): Flow<List<ExamEntity>>

    /**
     * Obtiene un examen específico mediante su identificador único.
     *
     * @param id Identificador UUID o alfanumérico del examen.
     */
    fun getExamById(id: String): Flow<ExamEntity?>

    /**
     * Obtiene los exámenes asociados a un paciente específico por su nombre.
     *
     * @param patientName Nombre completo o de referencia del paciente.
     */
    fun getExamsByPatient(patientName: String): Flow<List<ExamEntity>>

    /**
     * Inserta o actualiza un examen clínico en la base de datos.
     *
     * @param exam Entidad del examen a persistir.
     */
    suspend fun insertExam(exam: ExamEntity)

    /**
     * Inserta o actualiza un paciente en la base de datos.
     *
     * @param paciente Entidad del paciente a persistir.
     */
    suspend fun insertPaciente(paciente: PacienteEntity)

    /**
     * Inserta o actualiza una consulta médica en la base de datos.
     *
     * @param consulta Entidad de la atención o consulta oftalmológica.
     */
    suspend fun insertConsulta(consulta: ConsultaEntity)

    /**
     * Inserta o actualiza un documento anexo (PDF, imagen) asociado a un examen.
     *
     * @param documento Entidad del documento clínico a persistir.
     */
    suspend fun insertDocumento(documento: DocumentoEntity)

    /**
     * Verifica si la base de datos local se encuentra vacía y, de ser así,
     * inserta un conjunto de datos de prueba (seed data) coherentes para inicializar el sistema.
     */
    suspend fun seedDatabaseIfNeeded()
}
