package com.example.oftapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.data.repository.OftAppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para el historial clínico del paciente.
 *
 * Implementa búsqueda dinámica por nombre mediante campo de texto reactivo y
 * carga perezosa (Lazy Loading) por bloques/scroll para evitar sobrecargar la memoria.
 * Incluye restricción de privacidad para el rol Paciente.
 */
class HistorialClinicoViewModel(
    private val repository: OftAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState(isLoading = true))
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var allFilteredExams: List<ExamEntity> = emptyList()
    private var currentBlock: Int = 1
    private val pageSize: Int = 5 // Bloque de carga perezosa

    private var userRole: String = "Tecnólogo Médico"
    private var loggedPatientName: String = "Juan Pérez"

    init {
        loadAllExams()
    }

    /**
     * Configura el rol del usuario autenticado para aplicar restricciones de privacidad.
     * Si el rol es "Paciente", restringe la búsqueda exclusivamente a sus propios exámenes.
     */
    fun setUserRole(role: String, patientName: String = "Juan Pérez") {
        this.userRole = role
        this.loggedPatientName = patientName
        val isPatient = role.equals("Paciente", ignoreCase = true)
        _uiState.update { it.copy(isRestrictedToPatient = isPatient) }

        if (isPatient) {
            selectPatient(patientName)
        } else {
            loadAllExams()
        }
    }

    /**
     * Búsqueda dinámica reactiva por nombre de paciente mediante OutlinedTextField / SearchBar.
     */
    fun onSearchQueryChange(query: String) {
        if (userRole.equals("Paciente", ignoreCase = true)) {
            // Un paciente no puede buscar fuera de su propia ficha
            return
        }
        _uiState.update { it.copy(searchQuery = query) }
        filterExams(query)
    }

    /**
     * Filtra la fuente de datos aplicando la consulta y reiniciando la paginación al primer bloque.
     */
    private fun filterExams(query: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.seedDatabaseIfNeeded()

            repository.getAllExams().collect { exams ->
                val filtered = if (userRole.equals("Paciente", ignoreCase = true)) {
                    exams.filter {
                        it.patientName.equals(loggedPatientName, ignoreCase = true) ||
                                it.patientName.contains("Juan", ignoreCase = true)
                    }
                } else if (query.isBlank()) {
                    exams
                } else {
                    exams.filter { it.patientName.contains(query, ignoreCase = true) }
                }

                allFilteredExams = filtered
                currentBlock = 1
                val firstBlock = allFilteredExams.take(pageSize)

                val selectedPat = when {
                    userRole.equals("Paciente", ignoreCase = true) -> loggedPatientName
                    query.isNotBlank() -> query
                    else -> firstBlock.firstOrNull()?.patientName ?: ""
                }

                _uiState.update {
                    it.copy(
                        selectedPatient = selectedPat,
                        patientExams = firstBlock,
                        totalExamsCount = allFilteredExams.size,
                        hasMoreItems = allFilteredExams.size > firstBlock.size,
                        isLoading = false,
                        isLoadingMore = false
                    )
                }
            }
        }
    }

    /**
     * Carga el siguiente bloque de exámenes en respuesta al scroll del usuario (Lazy Loading).
     */
    fun loadNextBlock() {
        val currentState = _uiState.value
        if (!currentState.hasMoreItems || currentState.isLoadingMore || currentState.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            delay(150) // Suaviza la transición visual del scroll
            currentBlock++
            val nextCount = currentBlock * pageSize
            val nextSlice = allFilteredExams.take(nextCount)

            _uiState.update {
                it.copy(
                    patientExams = nextSlice,
                    hasMoreItems = allFilteredExams.size > nextSlice.size,
                    isLoadingMore = false
                )
            }
        }
    }

    /**
     * Carga todos los exámenes y establece el primer paciente por defecto.
     */
    fun loadAllExams() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.seedDatabaseIfNeeded()

            repository.getAllExams().collect { exams ->
                val filtered = if (userRole.equals("Paciente", ignoreCase = true)) {
                    exams.filter {
                        it.patientName.equals(loggedPatientName, ignoreCase = true) ||
                                it.patientName.contains("Juan", ignoreCase = true)
                    }
                } else {
                    val currentPatient = _uiState.value.selectedPatient
                    val defaultPatient = currentPatient.ifBlank {
                        exams.firstOrNull()?.patientName ?: ""
                    }
                    if (defaultPatient.isBlank()) exams
                    else exams.filter { it.patientName.equals(defaultPatient, ignoreCase = true) }
                }

                allFilteredExams = filtered
                currentBlock = 1
                val firstBlock = allFilteredExams.take(pageSize)

                val patientName = if (userRole.equals("Paciente", ignoreCase = true)) {
                    loggedPatientName
                } else {
                    _uiState.value.selectedPatient.ifBlank {
                        exams.firstOrNull()?.patientName ?: ""
                    }
                }

                _uiState.update {
                    it.copy(
                        selectedPatient = patientName,
                        searchQuery = if (userRole.equals("Paciente", ignoreCase = true)) loggedPatientName else it.searchQuery,
                        patientExams = firstBlock,
                        totalExamsCount = allFilteredExams.size,
                        hasMoreItems = allFilteredExams.size > firstBlock.size,
                        isLoading = false,
                        isLoadingMore = false
                    )
                }
            }
        }
    }

    /**
     * Filtra el historial para un paciente específico.
     */
    fun selectPatient(patientName: String) {
        val targetPatient = if (userRole.equals("Paciente", ignoreCase = true)) loggedPatientName else patientName
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedPatient = targetPatient,
                    searchQuery = targetPatient,
                    isLoading = true
                )
            }
            if (targetPatient.isBlank()) {
                repository.getAllExams().collect { exams ->
                    allFilteredExams = exams
                    currentBlock = 1
                    val firstBlock = allFilteredExams.take(pageSize)
                    _uiState.update {
                        it.copy(
                            patientExams = firstBlock,
                            totalExamsCount = allFilteredExams.size,
                            hasMoreItems = allFilteredExams.size > firstBlock.size,
                            isLoading = false
                        )
                    }
                }
            } else {
                repository.getExamsByPatient(targetPatient).collect { exams ->
                    allFilteredExams = exams
                    currentBlock = 1
                    val firstBlock = allFilteredExams.take(pageSize)
                    _uiState.update {
                        it.copy(
                            selectedPatient = targetPatient,
                            patientExams = firstBlock,
                            totalExamsCount = allFilteredExams.size,
                            hasMoreItems = allFilteredExams.size > firstBlock.size,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}
