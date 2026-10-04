package com.example.oftapp.model

data class Examen(
    val id: String,
    val atencionId: String,
    val tipoExamen: String,
    val estado: String,
    val observaciones: String
)