package com.example.oftapp.ui.viewmodel

import android.net.Uri

/**
 * Estado inmutable de la interfaz de usuario para el formulario de registro de atención médica.
 *
 * @property patientId Identificador o nombre del paciente.
 * @property branch Sucursal o sede donde se realiza la atención.
 * @property examType Tipo de examen oftalmológico.
 * @property eye Ojo evaluado (OD, OI, AO).
 * @property notes Observaciones o notas clínicas de la atención.
 * @property fileUri URI del documento o archivo adjunto seleccionado.
 * @property fileName Nombre del archivo adjunto seleccionado.
 * @property isSaving Indicador de proceso de guardado en ejecución.
 * @property isSuccess Indicador de guardado exitoso.
 * @property createdExamId Identificador del examen recién creado.
 * @property errorMessage Mensaje de error en caso de fallo o validación.
 */
data class RegistroUiState(
    val patientId: String = "",
    val branch: String = "",
    val examType: String = "",
    val eye: String = "",
    val notes: String = "",
    val fileUri: Uri? = null,
    val fileName: String? = null,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val createdExamId: String? = null,
    val errorMessage: String? = null
)
