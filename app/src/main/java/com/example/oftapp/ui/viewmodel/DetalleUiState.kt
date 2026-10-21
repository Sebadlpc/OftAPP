package com.example.oftapp.ui.viewmodel

import com.example.oftapp.data.local.entity.ExamEntity

/**
 * Estado inmutable de la interfaz de usuario para el detalle de un examen clínico.
 *
 * @property exam Entidad del examen seleccionado o null si no se ha cargado.
 * @property isLoading Indicador de carga de datos.
 */
data class DetalleUiState(
    val exam: ExamEntity? = null,
    val isLoading: Boolean = false
)
