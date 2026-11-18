package com.example.oftapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.oftapp.data.local.security.EncryptedSessionManager
import com.example.oftapp.data.repository.OftAppRepository

/**
 * Factoría personalizada de ViewModels para OftAPP.
 *
 * Facilita la inyección de dependencias de [OftAppRepository], [EncryptedSessionManager]
 * y parámetros iniciales (como `examId`) hacia las clases ViewModel del proyecto.
 */
@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(
    private val repository: OftAppRepository,
    private val examId: String? = null,
    private val sessionManager: EncryptedSessionManager? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(RegistroAtencionViewModel::class.java) -> {
                RegistroAtencionViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ListadoBusquedaViewModel::class.java) -> {
                ListadoBusquedaViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DetalleExamenViewModel::class.java) -> {
                DetalleExamenViewModel(repository, examId) as T
            }
            modelClass.isAssignableFrom(HistorialClinicoViewModel::class.java) -> {
                HistorialClinicoViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository, sessionManager) as T
            }
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}

/**
 * Alias de retrocompatibilidad con ViewModelFactory.
 */
typealias ViewModelFactory = AppViewModelFactory
