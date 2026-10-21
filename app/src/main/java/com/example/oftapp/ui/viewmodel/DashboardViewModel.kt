package com.example.oftapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oftapp.data.repository.OftAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del Dashboard principal.
 *
 * Se encarga de exponer de manera reactiva las métricas globales (totales,
 * pendientes y validados) e historial reciente calculados a partir de los datos
 * provistos por [OftAppRepository].
 */
class DashboardViewModel(
    private val repository: OftAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * Carga y se suscribe de forma reactiva al flujo de exámenes del repositorio,
     * calculando métricas e historial de exámenes recientes.
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.seedDatabaseIfNeeded()
            repository.getAllExams().collect { exams ->
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
