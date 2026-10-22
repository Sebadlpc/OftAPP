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
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel para el listado y búsqueda reactiva de exámenes médicos.
 *
 * Maneja la combinación reactiva de consultas de texto y filtros dinámicos (tipo de examen,
 * estado o sede) sobre la fuente de datos Room.
 */
class ListadoBusquedaViewModel(
    private val repository: OftAppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow("Todos")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ListadoUiState> = _searchQuery
        .flatMapLatest { query ->
            repository.searchExams(query)
        }
        .combine(_selectedFilter) { exams, filter ->
            val filteredExams = when {
                filter.equals("Todos", ignoreCase = true) || filter.isBlank() -> {
                    exams
                }
                filter.equals("Pendientes", ignoreCase = true) -> {
                    exams.filter {
                        it.status.equals("Pendiente", ignoreCase = true) ||
                                it.status.equals("En Proceso", ignoreCase = true) ||
                                it.status.equals("Pendientes", ignoreCase = true)
                    }
                }
                filter.equals("Validados", ignoreCase = true) -> {
                    exams.filter {
                        it.status.equals("Validado", ignoreCase = true) ||
                                it.status.equals("Completado", ignoreCase = true) ||
                                it.status.equals("Validados", ignoreCase = true)
                    }
                }
                else -> {
                    exams.filter { exam ->
                        exam.examType.equals(filter, ignoreCase = true) ||
                                exam.status.equals(filter, ignoreCase = true) ||
                                exam.branch.contains(filter, ignoreCase = true)
                    }
                }
            }
            ListadoUiState(
                searchQuery = _searchQuery.value,
                selectedFilter = filter,
                exams = filteredExams,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListadoUiState(isLoading = true)
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelect(filter: String) {
        _selectedFilter.value = filter
    }
}
