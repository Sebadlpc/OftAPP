package com.example.oftapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oftapp.BuildConfig
import com.example.oftapp.data.remote.InsforgeClientProvider
import com.example.oftapp.data.repository.OftAppRepository
import dev.insforge.auth.auth
import dev.insforge.auth.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de inicio de sesión de OftAPP.
 *
 * Integra la autenticación con el SDK de Insforge leyendo de forma segura las credenciales
 * desde BuildConfig (local.properties) y gestiona los roles clínicos (MEDICO, TECNOLOGO, ADMIN, PACIENTE).
 */
class LoginViewModel(
    private val repository: OftAppRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val sessionToken: String?
        get() = _uiState.value.sessionToken

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, errorMessage = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null) }
    }

    fun onRoleSelect(role: String) {
        _uiState.update { it.copy(selectedRole = role, errorMessage = null) }
    }

    /**
     * Autenticación con el SDK de Insforge.
     *
     * Valida que las claves de configuración existan en BuildConfig (local.properties).
     * Si no están presentes, alerta al usuario con un mensaje claro.
     * Si están configuradas, ejecuta signIn, mapea el rol clínico y almacena el token de sesión.
     */
    fun loginWithInsforge() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Por favor, ingrese correo y contraseña",
                    isAuthenticated = false
                )
            }
            return
        }

        // Validación de credenciales en BuildConfig (local.properties)
        if (BuildConfig.INSFORGE_BASE_URL.isBlank() || BuildConfig.INSFORGE_ANON_KEY.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = "Falta configurar INSFORGE_BASE_URL e INSFORGE_ANON_KEY en local.properties"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Autenticación con el cliente singleton seguro de Insforge
                val response = InsforgeClientProvider.client.auth.signIn(
                    email = email,
                    password = password
                )

                // Mapeo del rol según perfil del usuario o correo (MEDICO, TECNOLOGO, ADMIN, PACIENTE)
                val cloudRole = response.user.profile?.get("role") ?: response.user.metadata?.get("role")
                val (mappedRoleCode, displayRole, defaultName) = resolveClinicalRole(email, cloudRole)
                val cloudName = response.user.name ?: response.user.profile?.get("name")
                val assignedUserName = cloudName?.ifBlank { null } ?: defaultName

                // Almacenamiento seguro del token de sesión retornado
                val token = response.accessToken ?: response.refreshToken ?: ""

                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        userName = assignedUserName,
                        selectedRole = displayRole,
                        userRole = mappedRoleCode,
                        sessionToken = token,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                val errorDetail = when {
                    e.message?.contains("503") == true ->
                        "Servicio Insforge no disponible (503). Verifique el estado del backend."
                    e.message?.contains("Unable to resolve host") == true ->
                        "Sin conexión a internet hacia Insforge. Verifique su red."
                    e.message?.contains("Email verification", ignoreCase = true) == true ||
                            e.message?.contains("email not confirmed", ignoreCase = true) == true ->
                        "El correo no ha sido verificado. Confírmalo en tu bandeja de entrada o desactiva 'Confirm email' en la consola de Insforge."
                    else ->
                        e.localizedMessage ?: e.message ?: "Credenciales incorrectas o error en Insforge."
                }
                _uiState.update {
                    it.copy(
                        isAuthenticated = false,
                        errorMessage = errorDetail
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Valida credenciales según el usuario ingresado (soporte retrocompatible para tests):
     * - "Tecnologo" / "1234" -> Rol "Tecnólogo Médico"
     * - "Medico" / "1234"    -> Rol "Médico Oftalmólogo"
     * - "Admin" / "1234"     -> Rol "Personal Administrativo"
     * - "Paciente" / "1234"  -> Rol "Paciente"
     */
    fun login(password: String) {
        onPasswordChange(password)
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

            val clinicalRole = when {
                currentEmail.equals("Tecnologo", ignoreCase = true) ||
                        currentEmail.equals("Tecnólogo", ignoreCase = true) ->
                    Triple("TECNOLOGO", "Tecnólogo Médico", "Lic. Andrés Gómez")
                currentEmail.equals("Medico", ignoreCase = true) ||
                        currentEmail.equals("Médico", ignoreCase = true) ->
                    Triple("MEDICO", "Médico Oftalmólogo", "Dra. Carla Ruiz")
                currentEmail.equals("Admin", ignoreCase = true) ->
                    Triple("ADMIN", "Personal Administrativo", "Carlos Vega")
                currentEmail.equals("Paciente", ignoreCase = true) ||
                        currentEmail.equals("Juan Pérez", ignoreCase = true) ||
                        currentEmail.equals("Juan", ignoreCase = true) ->
                    Triple("PACIENTE", "Paciente", "Juan Pérez")
                else -> null
            }

            if (clinicalRole != null && currentPassword == "1234") {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        selectedRole = clinicalRole.second,
                        userRole = clinicalRole.first,
                        userName = clinicalRole.third,
                        sessionToken = "demo-local-token",
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

    /**
     * Mapea el rol clínico según el correo o perfil retornado por Insforge:
     * - MEDICO: "Médico Oftalmólogo"
     * - TECNOLOGO: "Tecnólogo Médico"
     * - ADMIN: "Personal Administrativo"
     * - PACIENTE: "Paciente"
     */
    private fun resolveClinicalRole(
        emailOrUsername: String,
        cloudRole: String? = null
    ): Triple<String, String, String> {
        val roleCandidate = (cloudRole ?: "").trim().uppercase()
        val text = emailOrUsername.trim().lowercase()

        val roleCode = when {
            roleCandidate == "MEDICO" || roleCandidate.contains("MEDIC") ||
                    text.contains("medico") || text.contains("médico") || text.contains("doctor") -> "MEDICO"

            roleCandidate == "TECNOLOGO" || roleCandidate.contains("TECNOL") ||
                    text.contains("tecnologo") || text.contains("tecnólogo") -> "TECNOLOGO"

            roleCandidate == "ADMIN" || roleCandidate.contains("ADMIN") ||
                    text.contains("admin") -> "ADMIN"

            roleCandidate == "PACIENTE" || roleCandidate.contains("PACIENTE") ||
                    text.contains("paciente") || text.contains("juan") -> "PACIENTE"

            else -> "TECNOLOGO"
        }

        val displayRole = when (roleCode) {
            "MEDICO" -> "Médico Oftalmólogo"
            "TECNOLOGO" -> "Tecnólogo Médico"
            "ADMIN" -> "Personal Administrativo"
            "PACIENTE" -> "Paciente"
            else -> "Tecnólogo Médico"
        }

        val defaultName = when (roleCode) {
            "PACIENTE" -> "Juan Pérez"
            "MEDICO" -> "Dra. Carla Ruiz"
            "TECNOLOGO" -> "Lic. Andrés Gómez"
            "ADMIN" -> "Carlos Vega"
            else -> emailOrUsername.substringBefore("@").replaceFirstChar { it.uppercase() }
        }

        return Triple(roleCode, displayRole, defaultName)
    }
}
