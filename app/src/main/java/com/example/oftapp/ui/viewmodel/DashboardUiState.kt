package com.example.oftapp.ui.viewmodel

import com.example.oftapp.data.local.entity.ExamEntity

/**
 * Estado inmutable de la interfaz de usuario para el Dashboard principal.
 */
data class DashboardUiState(
    val totalCount: Int = 0,
    val pendingCount: Int = 0,
    val validatedCount: Int = 0,
    val recentExams: List<ExamEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val pendingSyncCount: Int = 0,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val fhirObservations: List<FhirObservationDisplayItem> = emptyList(),
    val fhirLoading: Boolean = false,
    val fhirError: String? = null
)

data class FhirObservationDisplayItem(
    val id: String,
    val title: String,
    val value: String,
    val date: String,
    val status: String
)
