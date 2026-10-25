package com.example.oftapp.ui.viewmodel

import com.example.oftapp.data.local.entity.ExamEntity

/**
 * Estado inmutable de la interfaz de usuario para la línea de tiempo del historial clínico.
 *
 * Soporta búsqueda dinámica por nombre de paciente y carga perezosa por bloques (Lazy Loading / Pagination).
 *
 * @property selectedPatient Paciente seleccionado o visualizado.
 * @property searchQuery Texto de búsqueda ingresado en el buscador dinámico.
 * @property patientExams Subconjunto de exámenes actualmente cargados y renderizados en la LazyColumn.
 * @property isLoading Indicador de carga inicial de datos.
 * @property isLoadingMore Indicador de carga de bloques subsiguientes durante el scroll.
 * @property hasMoreItems Indica si existen más registros pendientes de cargar en el historial.
 * @property totalExamsCount Conteo total de exámenes disponibles para el filtro actual.
 * @property isRestrictedToPatient Indica si la vista está restringida al rol Paciente.
 */
data class HistorialUiState(
    val selectedPatient: String = "",
    val searchQuery: String = "",
    val patientExams: List<ExamEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreItems: Boolean = false,
    val totalExamsCount: Int = 0,
    val isRestrictedToPatient: Boolean = false
)
