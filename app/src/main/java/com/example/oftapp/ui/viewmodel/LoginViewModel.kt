package com.example.oftapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oftapp.data.repository.OftAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de inicio de sesión.
 *
 * Valida credenciales según el rol predeterminado y gestiona el estado de autenticación y asignación de rol.
 */
class LoginViewModel(
    private val repository: OftAppRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onRoleSelect(role: String) {
        _uiState.update { it.copy(selectedRole = role, errorMessage = null) }
    }

    /**
     * Valida credenciales según el usuario ingresado:
     * - "Tecnologo" / "1234" -> Rol "Tecnólogo Médico"
     * - "Medico" / "1234"    -> Rol "Médico Oftalmólogo"
     * - "Admin" / "1234"     -> Rol "Personal Administrativo"
     * - "Paciente" / "1234"  -> Rol "Paciente"
     */
    fun login(password: String) {
        val currentEmail = _uiState.value.email.trim()
        val currentPassword = password.trim()

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Por favor, ingrese usuario y contraseña",
                    isAuthenticated = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val assignedRole = when {
                currentEmail.equals("Tecnologo", ignoreCase = true) ||
                        currentEmail.equals("Tecnólogo", ignoreCase = true) -> "Tecnólogo Médico"
                currentEmail.equals("Medico", ignoreCase = true) ||
                        currentEmail.equals("Médico", ignoreCase = true) -> "Médico Oftalmólogo"
                currentEmail.equals("Admin", ignoreCase = true) -> "Personal Administrativo"
                currentEmail.equals("Paciente", ignoreCase = true) ||
                        currentEmail.equals("Juan Pérez", ignoreCase = true) ||
                        currentEmail.equals("Juan", ignoreCase = true) -> "Paciente"
                else -> null
            }

            if (assignedRole != null && currentPassword == "1234") {
                val assignedUserName = when (assignedRole) {
                    "Paciente" -> "Juan Pérez"
                    "Médico Oftalmólogo" -> "Dra. Carla Ruiz"
                    "Tecnólogo Médico" -> "Lic. Andrés Gómez"
                    "Personal Administrativo" -> "Carlos Vega"
                    else -> currentEmail
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        selectedRole = assignedRole,
                        userName = assignedUserName,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        errorMessage = "Credenciales incorrectas. Verifique usuario y contraseña."
                    )
                }
            }
        }
    }

    fun logout() {
        _uiState.update {
            LoginUiState()
        }
    }
}
