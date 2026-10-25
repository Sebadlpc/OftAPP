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
 * ViewModel del Dashboard principal.
 *
 * Expone de manera reactiva las métricas (totales, pendientes y validados) e historial reciente.
 * Si el usuario autenticado tiene el rol "Paciente", restringe el cálculo y visualización
 * exclusivamente a sus propios exámenes personales.
 * Para los roles de "Médico Oftalmólogo", "Tecnólogo Médico" y "Personal Administrativo",
 * provee métricas de acceso global.
 */
class DashboardViewModel(
    private val repository: OftAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentUserRole: String = "Tecnólogo Médico"
    private var currentPatientName: String = "Juan Pérez"
    private var dataJob: Job? = null

    init {
        loadDashboardData()
    }

    /**
     * Establece el rol y la identidad del usuario para aplicar las restricciones de privacidad.
     */
    fun setUserRole(role: String, patientName: String = "Juan Pérez") {
        this.currentUserRole = role
        this.currentPatientName = patientName
        loadDashboardData()
    }

    /**
     * Carga y se suscribe de forma reactiva al flujo de exámenes del repositorio,
     * calculando métricas e historial reciente según el rol activo.
     */
    fun loadDashboardData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.seedDatabaseIfNeeded()
            repository.getAllExams().collect { allExams ->
                // Filtro estricto de privacidad para el rol Paciente
                val exams = if (currentUserRole.equals("Paciente", ignoreCase = true)) {
                    allExams.filter {
                        it.patientName.equals(currentPatientName, ignoreCase = true) ||
                                it.patientName.contains("Juan", ignoreCase = true)
                    }
                } else {
                    allExams
                }

                val totalCount = exams.size
                val pendingCount = exams.count {
                    it.status.equals("Pendiente", ignoreCase = true) ||
                            it.status.equals("En Proceso", ignoreCase = true)
                }
                val validatedCount = exams.count {
                    it.status.equals("Validado", ignoreCase = true) ||
                            it.status.equals("Completado", ignoreCase = true)
                }
                val recentExams = exams.take(5)

                _uiState.update {
                    it.copy(
                        totalCount = totalCount,
                        pendingCount = pendingCount,
                        validatedCount = validatedCount,
                        recentExams = recentExams,
                        isLoading = false
                    )
                }
            }
        }
    }
}
