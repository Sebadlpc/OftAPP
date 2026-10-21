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
 * Gestiona la selección del rol del usuario, las credenciales ingresadas y la lógica
 * de autenticación ficticia del sistema.
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
     * Realiza el proceso de inicio de sesión ficticio validando las credenciales.
     */
    fun login(password: String) {
        val currentEmail = _uiState.value.email
        if (currentEmail.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Por favor, ingrese usuario/correo y contraseña",
                    isAuthenticated = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // Simulación de autenticación exitosa
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null
                )
            }
        }
    }

    fun logout() {
        _uiState.update {
            LoginUiState()
        }
    }
}
