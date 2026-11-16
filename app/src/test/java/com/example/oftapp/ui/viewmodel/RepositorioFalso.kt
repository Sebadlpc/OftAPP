package com.example.oftapp.ui.viewmodel

import com.example.oftapp.data.local.entity.ConsultaEntity
import com.example.oftapp.data.local.entity.DocumentoEntity
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.data.local.entity.PacienteEntity
import com.example.oftapp.data.repository.OftAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Repositorio "de mentira" para las pruebas unitarias.
 *
 * Reemplaza a la base de datos Room: guarda todo en listas en memoria,
 * así las pruebas son rápidas y cada una parte con los datos que necesita.
 *
 * Lo comparten las pruebas de todas las pantallas (Dashboard, Registro,
 * Detalle, Listado e Historial).
 */
class RepositorioFalso(examenesIniciales: List<ExamEntity> = emptyList()) : OftAppRepository {

    val examenes = MutableStateFlow(examenesIniciales)
    val consultas = MutableStateFlow<List<ConsultaEntity>>(emptyList())
    val documentos = MutableStateFlow<List<DocumentoEntity>>(emptyList())
    val pacientes = MutableStateFlow<List<PacienteEntity>>(emptyList())

    /** Si es true, guardar una consulta lanza un error (para probar fallos de la base de datos). */
    var fallarAlGuardar = false

    override fun getAllExams(): Flow<List<ExamEntity>> = examenes

    override fun searchExams(query: String): Flow<List<ExamEntity>> =
        examenes.map { lista ->
            if (query.isBlank()) lista
            else lista.filter {
                it.patientName.contains(query, ignoreCase = true) ||
                        it.examType.contains(query, ignoreCase = true) ||
                        it.id.contains(query, ignoreCase = true) ||
                        it.branch.contains(query, ignoreCase = true)
            }
        }

    override fun getExamById(id: String): Flow<ExamEntity?> =
        examenes.map { lista -> lista.find { it.id == id } }

    override fun getExamsByPatient(patientName: String): Flow<List<ExamEntity>> =
        examenes.map { lista -> lista.filter { it.patientName.equals(patientName, ignoreCase = true) } }

    override suspend fun insertExam(exam: ExamEntity) {
        examenes.value = examenes.value.filterNot { it.id == exam.id } + exam
    }

    override suspend fun insertPaciente(paciente: PacienteEntity) {
        pacientes.value = pacientes.value + paciente
    }

    override suspend fun insertConsulta(consulta: ConsultaEntity) {
        if (fallarAlGuardar) throw IllegalStateException("No se pudo guardar en la base de datos")
        consultas.value = consultas.value + consulta
    }

    override suspend fun insertDocumento(documento: DocumentoEntity) {
        documentos.value = documentos.value + documento
    }

    /** No hace nada: cada prueba decide con qué exámenes parte. */
    override suspend fun seedDatabaseIfNeeded() = Unit
}

/** Atajo para crear exámenes de prueba escribiendo solo lo importante. */
fun examen(
    id: String,
    paciente: String = "María González",
    tipo: String = "Campimetría",
    estado: String = "Pendiente",
    sede: String = "Sede Centro",
    ojo: String = "OD"
) = ExamEntity(
    id = id,
    patientName = paciente,
    examType = tipo,
    eye = ojo,
    status = estado,
    branch = sede,
    doctorName = "Dr. Silva"
)
