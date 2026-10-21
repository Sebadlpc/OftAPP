package com.example.oftapp.ui.viewmodel

/**
 * Estado inmutable de la interfaz de usuario para el formulario de registro de atención médica.
 *
 * @property patientId Identificador o nombre del paciente.
 * @property branch Sucursal o sede donde se realiza la atención.
 * @property examType Tipo de examen oftalmológico.
 * @property eye Ojo evaluado (OD, OI, AO).
 * @property isSaving Indicador de proceso de guardado en ejecución.
 * @property isSuccess Indicador de guardado exitoso.
 * @property errorMessage Mensaje de error en caso de fallo o validación.
 */
data class RegistroUiState(
    val patientId: String = "",
    val branch: String = "",
    val examType: String = "",
    val eye: String = "",
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
