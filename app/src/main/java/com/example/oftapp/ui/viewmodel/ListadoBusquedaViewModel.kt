package com.example.oftapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oftapp.data.repository.OftAppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Criterios combinados para búsqueda y filtrado de exámenes con soporte de control de acceso por rol.
 */
private data class RoleSearchCriteria(
    val query: String,
    val filter: String,
    val role: String,
    val patient: String
)

/**
 * ViewModel para el listado y búsqueda reactiva de exámenes médicos.
 *
 * Aplica reglas de privacidad: si el usuario autenticado tiene el rol "Paciente",
 * restringe los resultados de búsqueda exclusivamente a sus propios exámenes personales.
 * Para los roles facultativos ("Médico", "Tecnólogo", "Admin") mantiene acceso global.
 */
class ListadoBusquedaViewModel(
    private val repository: OftAppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow("Todos")
    private val _userRole = MutableStateFlow("Tecnólogo Médico")
    private val _patientName = MutableStateFlow("Juan Pérez")

    /**
     * Establece el rol y el paciente para aplicar la restricción de visibilidad.
     */
    fun setUserRole(role: String, patientName: String = "Juan Pérez") {
        _userRole.value = role
        _patientName.value = patientName
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ListadoUiState> = combine(
        _searchQuery,
        _selectedFilter,
        _userRole,
        _patientName
    ) { query, filter, role, patient ->
        RoleSearchCriteria(query, filter, role, patient)
    }.flatMapLatest { criteria ->
        repository.searchExams(criteria.query).map { exams ->
            // Restricción por rol de usuario
            val roleFilteredExams = if (criteria.role.equals("Paciente", ignoreCase = true)) {
                exams.filter {
                    it.patientName.equals(criteria.patient, ignoreCase = true) ||
                            it.patientName.contains("Juan", ignoreCase = true)
                }
            } else {
                exams
            }

            val filteredExams = when {
                criteria.filter.equals("Todos", ignoreCase = true) || criteria.filter.isBlank() -> {
                    roleFilteredExams
                }
                criteria.filter.equals("Pendientes", ignoreCase = true) -> {
                    roleFilteredExams.filter {
                        it.status.equals("Pendiente", ignoreCase = true) ||
                                it.status.equals("En Proceso", ignoreCase = true) ||
                                it.status.equals("Pendientes", ignoreCase = true)
                    }
                }
                criteria.filter.equals("Validados", ignoreCase = true) -> {
                    roleFilteredExams.filter {
                        it.status.equals("Validado", ignoreCase = true) ||
                                it.status.equals("Completado", ignoreCase = true) ||
                                it.status.equals("Validados", ignoreCase = true)
                    }
                }
                else -> {
                    roleFilteredExams.filter { exam ->
                        exam.examType.equals(criteria.filter, ignoreCase = true) ||
                                exam.status.equals(criteria.filter, ignoreCase = true) ||
                                exam.branch.contains(criteria.filter, ignoreCase = true)
                    }
                }
            }
            ListadoUiState(
                searchQuery = criteria.query,
                selectedFilter = criteria.filter,
                exams = filteredExams,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListadoUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchQueryChanged(query: String) {
        onSearchQueryChange(query)
    }

    fun onFilterSelect(filter: String) {
        _selectedFilter.value = filter
    }
}
