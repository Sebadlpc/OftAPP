package com.example.oftapp.ui.viewmodel

import com.example.oftapp.data.local.entity.ExamEntity

/**
 * Estado inmutable de la interfaz de usuario para la línea de tiempo del historial clínico.
 *
 * @property selectedPatient Paciente seleccionado para visualizar su historial.
 * @property patientExams Lista de exámenes del paciente seleccionado.
 * @property isLoading Indicador de carga de datos.
 */
data class HistorialUiState(
    val selectedPatient: String = "",
    val patientExams: List<ExamEntity> = emptyList(),
    val isLoading: Boolean = false
)
