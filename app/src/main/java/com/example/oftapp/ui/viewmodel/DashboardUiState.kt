package com.example.oftapp.ui.viewmodel

import com.example.oftapp.data.local.entity.ExamEntity

/**
 * Estado inmutable de la interfaz de usuario para el Dashboard principal.
 *
 * @property totalCount Cantidad total de exámenes registrados.
 * @property pendingCount Cantidad de exámenes pendientes de validación o revisión.
 * @property validatedCount Cantidad de exámenes validados o completados.
 * @property recentExams Lista de exámenes más recientes.
 * @property isLoading Indicador de carga de datos.
 */
data class DashboardUiState(
    val totalCount: Int = 0,
    val pendingCount: Int = 0,
    val validatedCount: Int = 0,
    val recentExams: List<ExamEntity> = emptyList(),
    val isLoading: Boolean = false
)
