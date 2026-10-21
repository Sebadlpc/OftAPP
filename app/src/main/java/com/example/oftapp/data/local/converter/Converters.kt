package com.example.oftapp.data.local.converter

import android.net.Uri
import androidx.room.TypeConverter
import java.util.Date

/**
 * Conversores de tipos para Room Database.
 * Permite persistir estructuras complejas como Date y Uri en SQLite.
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromString(value: String?): Uri? {
        return value?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun uriToString(uri: Uri?): String? {
        return uri?.toString()
    }
}
