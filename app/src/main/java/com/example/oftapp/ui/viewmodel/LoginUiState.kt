package com.example.oftapp.ui.viewmodel

/**
 * Estado inmutable de la interfaz de usuario para la pantalla de inicio de sesión.
 *
 * @property email Correo o nombre de usuario ingresado.
 * @property selectedRole Rol clínico seleccionado (Médico, Tecnólogo, Paciente, Admin).
 * @property isAuthenticated Estado de autenticación del usuario.
 * @property errorMessage Mensaje de error en caso de fallo de autenticación.
 * @property isLoading Indicador de proceso de inicio de sesión en ejecución.
 * @property userName Nombre de usuario o paciente asignado tras autenticarse.
 */
data class LoginUiState(
    val email: String = "",
    val selectedRole: String = "Tecnólogo Médico",
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val userName: String = ""
)
