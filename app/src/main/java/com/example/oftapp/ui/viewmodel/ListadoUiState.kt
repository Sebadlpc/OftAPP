package com.example.oftapp.ui.viewmodel

import com.example.oftapp.data.local.entity.ExamEntity

/**
 * Estado inmutable de la interfaz de usuario para el listado y búsqueda de exámenes.
 *
 * @property searchQuery Texto de búsqueda ingresado por el usuario.
 * @property selectedFilter Criterio o filtro activo aplicado sobre la lista.
 * @property exams Lista de exámenes filtrados resultado de la búsqueda.
 * @property isLoading Indicador de carga de datos.
 */
data class ListadoUiState(
    val searchQuery: String = "",
    val selectedFilter: String = "Todos",
    val exams: List<ExamEntity> = emptyList(),
    val isLoading: Boolean = false
)
