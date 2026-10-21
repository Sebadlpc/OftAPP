package com.example.oftapp.data.repository

import com.example.oftapp.data.local.AppDatabase
import com.example.oftapp.data.local.dao.ConsultaDao
import com.example.oftapp.data.local.dao.DocumentoDao
import com.example.oftapp.data.local.dao.ExamDao
import com.example.oftapp.data.local.dao.PacienteDao
import com.example.oftapp.data.local.entity.ConsultaEntity
import com.example.oftapp.data.local.entity.DocumentoEntity
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.data.local.entity.PacienteEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Implementación concreta del patrón Repositorio para OftAPP.
 *
 * Coordina las operaciones de acceso a datos utilizando los DAOs de Room ([ExamDao],
 * [PacienteDao], [ConsultaDao], [DocumentoDao]) garantizando el aislamiento de hilos
 * a través de un [CoroutineDispatcher] configurable (por defecto [Dispatchers.IO]).
 *
 * @property examDao DAO para operaciones sobre la tabla de exámenes.
 * @property pacienteDao DAO para operaciones sobre la tabla de pacientes.
 * @property consultaDao DAO para operaciones sobre la tabla de consultas médicas.
 * @property documentoDao DAO para operaciones sobre la tabla de documentos anexos.
 * @property ioDispatcher Despachador de corrutinas para operaciones de I/O y persistencia.
 */
class OftAppRepositoryImpl(
    private val examDao: ExamDao,
    private val pacienteDao: PacienteDao,
    private val consultaDao: ConsultaDao,
    private val documentoDao: DocumentoDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OftAppRepository {

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

    override suspend fun insertExam(exam: ExamEntity): Unit = withContext(ioDispatcher) {
        examDao.insertExam(exam)
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

    /**
     * Pre-pobla la base de datos con registros iniciales de prueba si no existen exámenes registrados.
     * Inserta pacientes, consultas, exámenes clínicos y documentos vinculados relacionalmente.
     */
    override suspend fun seedDatabaseIfNeeded(): Unit = withContext(ioDispatcher) {
        val currentExams = examDao.getAllExams().first()
        if (currentExams.isNotEmpty()) {
            return@withContext
        }

        // 1. Semilla de Pacientes
        val paciente1 = PacienteEntity(
            id = "paciente-001",
            rut = "12.345.678-9",
            fullName = "Juan Pérez",
            email = "juan.perez@email.com",
            phone = "+56 9 1234 5678"
        )
        val paciente2 = PacienteEntity(
            id = "paciente-002",
            rut = "9.876.543-2",
            fullName = "María López",
            email = "maria.lopez@email.com",
            phone = "+56 9 8765 4321"
        )
        val paciente3 = PacienteEntity(
            id = "paciente-003",
            rut = "15.432.198-K",
            fullName = "Carlos Gómez",
            email = "carlos.gomez@email.com",
            phone = "+56 9 7654 3210"
        )
        val paciente4 = PacienteEntity(
            id = "paciente-004",
            rut = "18.765.432-1",
            fullName = "Ana Martínez",
            email = "ana.martinez@email.com",
            phone = "+56 9 9876 5432"
        )

        pacienteDao.insertPacientes(listOf(paciente1, paciente2, paciente3, paciente4))

        // 2. Semilla de Consultas
        val consulta1 = ConsultaEntity(
            id = "consulta-001",
            pacienteId = paciente1.id,
            fecha = Date(System.currentTimeMillis() - (86400000L * 3)),
            sucursal = "Sucursal Central",
            profesionalACargo = "Dr. Alejandro Silva"
        )
        val consulta2 = ConsultaEntity(
            id = "consulta-002",
            pacienteId = paciente2.id,
            fecha = Date(System.currentTimeMillis() - (86400000L * 2)),
            sucursal = "Sucursal Las Condes",
            profesionalACargo = "Dra. Constanza Morales"
        )
        val consulta3 = ConsultaEntity(
            id = "consulta-003",
            pacienteId = paciente3.id,
            fecha = Date(System.currentTimeMillis() - 86400000L),
            sucursal = "Sucursal Providencia",
            profesionalACargo = "Dr. Roberto Fuentes"
        )
        val consulta4 = ConsultaEntity(
            id = "consulta-004",
            pacienteId = paciente4.id,
            fecha = Date(),
            sucursal = "Sucursal Central",
            profesionalACargo = "Dra. Valentina Castro"
        )

        consultaDao.insertConsultas(listOf(consulta1, consulta2, consulta3, consulta4))

        // 3. Semilla de Exámenes Clínicos
        val exam1 = ExamEntity(
            id = "ID-1234",
            consultaId = consulta1.id,
            patientName = paciente1.fullName,
            examType = "Tonometría",
            eye = "Ambos Ojos (AO)",
            date = consulta1.fecha,
            status = "Completado",
            branch = consulta1.sucursal,
            doctorName = consulta1.profesionalACargo,
            notes = "Presión intraocular: OD 14 mmHg, OI 15 mmHg. Parámetros normales.",
            fileUri = null
        )
        val exam2 = ExamEntity(
            id = "ID-5678",
            consultaId = consulta2.id,
            patientName = paciente2.fullName,
            examType = "Campo Visual",
            eye = "Ojo Derecho (OD)",
            date = consulta2.fecha,
            status = "En Proceso",
            branch = consulta2.sucursal,
            doctorName = consulta2.profesionalACargo,
            notes = "Ligera disminución de sensibilidad periférica cuadrante superior temporal.",
            fileUri = null
        )
        val exam3 = ExamEntity(
            id = "ID-9012",
            consultaId = consulta3.id,
            patientName = paciente3.fullName,
            examType = "OCT Retinal",
            eye = "Ojo Izquierdo (OI)",
            date = consulta3.fecha,
            status = "Pendiente",
            branch = consulta3.sucursal,
            doctorName = consulta3.profesionalACargo,
            notes = "Control anual por antecedentes familiares de glaucoma. Espesor de fibras estable.",
            fileUri = null
        )
        val exam4 = ExamEntity(
            id = "ID-3456",
            consultaId = consulta4.id,
            patientName = paciente4.fullName,
            examType = "Fundoscopía",
            eye = "Ambos Ojos (AO)",
            date = consulta4.fecha,
            status = "Completado",
            branch = consulta4.sucursal,
            doctorName = consulta4.profesionalACargo,
            notes = "Papila de bordes netos, excavación fisiológica 0.3, mácula sin alteraciones.",
            fileUri = null
        )

        examDao.insertExams(listOf(exam1, exam2, exam3, exam4))

        // 4. Semilla de Documentos Clínicos Anexos
        val doc1 = DocumentoEntity(
            id = "doc-001",
            examId = exam1.id,
            fileName = "informe_tonometria_id1234.pdf",
            fileUri = null,
            uploadDate = exam1.date
        )
        val doc2 = DocumentoEntity(
            id = "doc-002",
            examId = exam2.id,
            fileName = "grafico_campo_visual_id5678.png",
            fileUri = null,
            uploadDate = exam2.date
        )

        documentoDao.insertDocumentos(listOf(doc1, doc2))
    }

    companion object {
        @Volatile
        private var INSTANCE: OftAppRepositoryImpl? = null

        /**
         * Obtiene o crea la instancia singleton de [OftAppRepositoryImpl] recibiendo los DAOs necesarios.
         */
        fun getInstance(
            examDao: ExamDao,
            pacienteDao: PacienteDao,
            consultaDao: ConsultaDao,
            documentoDao: DocumentoDao,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO
        ): OftAppRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OftAppRepositoryImpl(
                    examDao = examDao,
                    pacienteDao = pacienteDao,
                    consultaDao = consultaDao,
                    documentoDao = documentoDao,
                    ioDispatcher = ioDispatcher
                ).also { INSTANCE = it }
            }
        }

        /**
         * Método de conveniencia para construir el repositorio directamente desde la base de datos [AppDatabase].
         */
        fun fromDatabase(
            database: AppDatabase,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO
        ): OftAppRepositoryImpl {
            return getInstance(
                examDao = database.examDao(),
                pacienteDao = database.pacienteDao(),
                consultaDao = database.consultaDao(),
                documentoDao = database.documentoDao(),
                ioDispatcher = ioDispatcher
            )
        }
    }
}
