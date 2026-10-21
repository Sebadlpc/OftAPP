package com.example.oftapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "consultas",
    indices = [Index(value = ["pacienteId"])]
)
data class ConsultaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val pacienteId: String,
    val fecha: Date = Date(),
    val sucursal: String,
    val profesionalACargo: String
)
