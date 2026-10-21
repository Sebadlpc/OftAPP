package com.example.oftapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pacientes")
data class PacienteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val rut: String,
    val fullName: String,
    val email: String,
    val phone: String
)
