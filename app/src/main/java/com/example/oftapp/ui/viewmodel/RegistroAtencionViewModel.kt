package com.example.oftapp.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oftapp.data.local.entity.ConsultaEntity
import com.example.oftapp.data.local.entity.DocumentoEntity
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.data.repository.OftAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

/**
 * ViewModel para el registro de atención y creación de nuevos exámenes clínicos.
 *
 * Proporciona métodos para actualizar los datos del formulario, validar los campos obligatorios
 * e insertar tanto la consulta médica como el examen asociado en Room mediante el repositorio.
 */
class RegistroAtencionViewModel(
    private val repository: OftAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onPatientIdChange(patientId: String) {
        _uiState.update { it.copy(patientId = patientId, errorMessage = null) }
    }

    fun onBranchChange(branch: String) {
        _uiState.update { it.copy(branch = branch, errorMessage = null) }
    }

    fun onExamTypeChange(examType: String) {
        _uiState.update { it.copy(examType = examType, errorMessage = null) }
    }

    fun onEyeChange(eye: String) {
        _uiState.update { it.copy(eye = eye, errorMessage = null) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes, errorMessage = null) }
    }

    fun onFileSelected(uri: Uri?, fileName: String? = null) {
        _uiState.update { it.copy(fileUri = uri, fileName = fileName, errorMessage = null) }
    }

    /**
     * Valida el formulario y registra la consulta y el examen clínico en la base de datos.
     *
     * @param doctorName Nombre del profesional responsable.
     * @param notes Observaciones clínicas adicionales (si se omite, usa la de uiState).
     */
    fun registrarAtencion(doctorName: String = "Dr. Asignado", notes: String = "") {
        val currentState = _uiState.value

        if (currentState.patientId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El ID del paciente es obligatorio") }
            return
        }
        if (currentState.branch.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Debe seleccionar una sede") }
            return
        }
        if (currentState.examType.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Debe seleccionar el tipo de examen") }
            return
        }
        if (currentState.eye.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Debe seleccionar el ojo evaluado") }
            return
        }

        val finalNotes = if (notes.isNotBlank()) notes else currentState.notes

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val consultaId = UUID.randomUUID().toString()
                val consulta = ConsultaEntity(
                    id = consultaId,
                    pacienteId = currentState.patientId,
                    fecha = Date(),
                    sucursal = currentState.branch,
                    profesionalACargo = doctorName
                )
                repository.insertConsulta(consulta)

                val exam = ExamEntity(
                    consultaId = consultaId,
                    patientName = currentState.patientId,
                    examType = currentState.examType,
                    eye = currentState.eye,
                    date = Date(),
                    status = "Pendiente",
                    branch = currentState.branch,
                    doctorName = doctorName,
                    notes = finalNotes,
                    fileUri = currentState.fileUri
                )
                repository.insertExam(exam)

                if (currentState.fileUri != null || !currentState.fileName.isNullOrBlank()) {
                    val documento = DocumentoEntity(
                        id = UUID.randomUUID().toString(),
                        examId = exam.id,
                        fileName = currentState.fileName ?: "documento_adjunto.pdf",
                        fileUri = currentState.fileUri,
                        uploadDate = exam.date
                    )
                    repository.insertDocumento(documento)
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSuccess = true,
                        createdExamId = exam.id,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSuccess = false,
                        errorMessage = e.message ?: "Error al registrar la atención"
                    )
                }
            }
        }
    }

    /**
     * Reinicia el estado del formulario después de una inserción exitosa o para descartar un mensaje de error.
     */
    fun resetState() {
        _uiState.value = RegistroUiState()
    }
}
