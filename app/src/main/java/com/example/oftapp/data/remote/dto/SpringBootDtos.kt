package com.example.oftapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("userId") val userId: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("rut") val rut: String
)

/**
 * DTO para la sincronización y persistencia de exámenes con el microservicio Spring Boot.
 * Alineado exactamente con com.oftapp.backend.entity.ExamenEntity.
 */
data class ExamenSyncDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("pacienteId") val pacienteId: Long,
    @SerializedName("pacienteNombre") val pacienteNombre: String,
    @SerializedName("tipoExamen") val tipoExamen: String,
    @SerializedName("ojoEvaluado") val ojoEvaluado: String, // "OD", "OI", "Bifocal"
    @SerializedName("fecha") val fecha: String, // ISO-8601 (ej. "2026-11-10T10:00:00Z")
    @SerializedName("estado") val estado: String, // "PENDIENTE", "VALIDADO", "SINCRONIZADO"
    @SerializedName("doctor") val doctor: String,
    @SerializedName("notas") val notas: String? = null,
    @SerializedName("documentoUrl") val documentoUrl: String? = null
)

typealias SpringExamenDto = ExamenSyncDto

data class ActualizarEstadoDto(
    @SerializedName("estado") val estado: String = "VALIDADO",
    @SerializedName("notas") val notas: String? = null
)

data class SpringPacienteDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("rut") val rut: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("telefono") val telefono: String
)
