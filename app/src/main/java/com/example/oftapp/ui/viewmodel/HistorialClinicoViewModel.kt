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
 * ViewModel para el historial clínico del paciente.
 *
 * Filtra y expone los exámenes por paciente para construir la línea de tiempo evolutiva de atenciones.
 */
class HistorialClinicoViewModel(
    private val repository: OftAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState(isLoading = true))
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadAllExams()
    }

    /**
     * Carga todos los exámenes y establece por defecto el primer paciente si ninguno está seleccionado.
     */
    fun loadAllExams() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.seedDatabaseIfNeeded()
            repository.getAllExams().collect { exams ->
                val currentPatient = _uiState.value.selectedPatient
                val defaultPatient = currentPatient.ifBlank {
                    exams.firstOrNull()?.patientName ?: ""
                }

                val filteredExams = if (defaultPatient.isBlank()) {
                    exams
                } else {
                    exams.filter { it.patientName.equals(defaultPatient, ignoreCase = true) }
                }

                _uiState.update {
                    it.copy(
                        selectedPatient = defaultPatient,
                        patientExams = filteredExams,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Filtra el historial para un paciente específico.
     */
    fun selectPatient(patientName: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(selectedPatient = patientName, isLoading = true) }
            if (patientName.isBlank()) {
                repository.getAllExams().collect { exams ->
                    _uiState.update { it.copy(patientExams = exams, isLoading = false) }
                }
            } else {
                repository.getExamsByPatient(patientName).collect { exams ->
                    _uiState.update {
                        it.copy(
                            selectedPatient = patientName,
                            patientExams = exams,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}
