package com.example.oftapp.ui.viewmodel

/**
 * Estado inmutable de la interfaz de usuario para la pantalla de inicio de sesión.
 *
 * @property email Correo o nombre de usuario ingresado.
 * @property password Contraseña ingresada para la autenticación.
 * @property isLoading Indicador de proceso de inicio de sesión en ejecución.
 * @property isAuthenticated Estado de autenticación del usuario con Insforge.
 * @property errorMessage Mensaje de error en caso de fallo de autenticación o falta de configuración.
 * @property userName Nombre del usuario obtenido de la sesión autenticada.
 * @property selectedRole Rol clínico visible (Médico Oftalmólogo, Tecnólogo Médico, Personal Administrativo, Paciente).
 * @property userRole Código de rol asignado (MEDICO, TECNOLOGO, ADMIN, PACIENTE).
 * @property sessionToken Token de autenticación retornado por el servicio de autenticación de Insforge.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val userName: String = "",
    val selectedRole: String = "Tecnólogo Médico",
    val userRole: String = "TECNOLOGO",
    val sessionToken: String? = null
)
