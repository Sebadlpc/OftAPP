package com.example.oftapp.data.local.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "documentos",
    indices = [Index(value = ["examId"])]
)
data class DocumentoEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val examId: String,
    val fileName: String,
    val fileUri: Uri? = null,
    val uploadDate: Date = Date()
)
