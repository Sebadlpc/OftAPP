package com.example.oftapp.data.local.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "exams",
    indices = [
        Index(value = ["consultaId"]),
        Index(value = ["patientName"])
    ]
)
data class ExamEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val consultaId: String? = null,
    val patientName: String,
    val examType: String,
    val eye: String,
    val date: Date = Date(),
    val status: String,
    val branch: String,
    val doctorName: String,
    val notes: String = "",
    val fileUri: Uri? = null
)
