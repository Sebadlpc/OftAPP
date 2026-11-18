package com.example.oftapp.data.remote

/**
 * Wrapper genérico para el manejo de resultados de peticiones de red y sincronización.
 */
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}
