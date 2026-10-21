package com.example.oftapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.oftapp.data.local.converter.Converters
import com.example.oftapp.data.local.dao.ConsultaDao
import com.example.oftapp.data.local.dao.DocumentoDao
import com.example.oftapp.data.local.dao.ExamDao
import com.example.oftapp.data.local.dao.PacienteDao
import com.example.oftapp.data.local.dao.UserDao
import com.example.oftapp.data.local.entity.ConsultaEntity
import com.example.oftapp.data.local.entity.DocumentoEntity
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.data.local.entity.PacienteEntity
import com.example.oftapp.data.local.entity.UserEntity

/**
 * Base de datos principal de Room para OftAPP.
 * Gestiona el almacenamiento local y la persistencia fuera de línea de usuarios,
 * pacientes, consultas, exámenes clínicos y documentos anexos.
 */
@Database(
    entities = [
        UserEntity::class,
        PacienteEntity::class,
        ConsultaEntity::class,
        ExamEntity::class,
        DocumentoEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun examDao(): ExamDao
    abstract fun pacienteDao(): PacienteDao
    abstract fun consultaDao(): ConsultaDao
    abstract fun userDao(): UserDao
    abstract fun documentoDao(): DocumentoDao

    companion object {
        const val DATABASE_NAME = "oftapp_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
