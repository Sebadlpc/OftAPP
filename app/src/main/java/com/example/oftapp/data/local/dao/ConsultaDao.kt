package com.example.oftapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.oftapp.data.local.entity.ConsultaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsultaDao {

    @Query("SELECT * FROM consultas ORDER BY fecha DESC")
    fun getAllConsultas(): Flow<List<ConsultaEntity>>

    @Query("SELECT * FROM consultas WHERE id = :id")
    fun getConsultaById(id: String): Flow<ConsultaEntity?>

    @Query("SELECT * FROM consultas WHERE id = :id")
    suspend fun findConsultaById(id: String): ConsultaEntity?

    @Query("SELECT * FROM consultas WHERE pacienteId = :pacienteId ORDER BY fecha DESC")
    fun getConsultasByPacienteId(pacienteId: String): Flow<List<ConsultaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsulta(consulta: ConsultaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultas(consultas: List<ConsultaEntity>): List<Long>

    @Update
    suspend fun updateConsulta(consulta: ConsultaEntity): Int

    @Delete
    suspend fun deleteConsulta(consulta: ConsultaEntity): Int

    @Query("DELETE FROM consultas WHERE id = :id")
    suspend fun deleteConsultaById(id: String): Int
}
