package com.example.oftapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oftapp.data.repository.OftAppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de detalle de un examen clínico.
 *
 * Obtiene el detalle de un examen mediante su ID expuesto por Room y permite
 * ejecutar acciones de validación médica actualizando la base de datos.
 */
class DetalleExamenViewModel(
    private val repository: OftAppRepository,
    initialExamId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState(isLoading = true))
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    private var examJob: Job? = null

    init {
        initialExamId?.let { loadExam(it) }
    }

    /**
     * Carga el flujo de un examen específico desde Room mediante su ID.
     */
    fun loadExam(examId: String) {
        examJob?.cancel()
        examJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getExamById(examId).collect { exam ->
                _uiState.update {
                    it.copy(
                        exam = exam,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Marca el examen actual como validado en la base de datos.
     */
    fun validateExam() {
        val currentExam = _uiState.value.exam ?: return
        viewModelScope.launch {
            val updatedExam = currentExam.copy(status = "Validado")
            repository.insertExam(updatedExam)
        }
    }
}
