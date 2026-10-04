package com.example.oftapp.model

import java.util.Date

data class Consulta(
    val id: String,
    val pacienteId: String,
    val fecha: Date,
    val sucursal: String,
    val profesionalACargo: String
)