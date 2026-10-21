package com.example.oftapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.oftapp.data.local.entity.PacienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {

    @Query("SELECT * FROM pacientes ORDER BY fullName ASC")
    fun getAllPacientes(): Flow<List<PacienteEntity>>

    @Query("SELECT * FROM pacientes WHERE id = :id")
    fun getPacienteById(id: String): Flow<PacienteEntity?>

    @Query("SELECT * FROM pacientes WHERE id = :id")
    suspend fun findPacienteById(id: String): PacienteEntity?

    @Query("SELECT * FROM pacientes WHERE rut = :rut LIMIT 1")
    fun getPacienteByRut(rut: String): Flow<PacienteEntity?>

    @Query("""
        SELECT * FROM pacientes 
        WHERE fullName LIKE '%' || :query || '%' 
           OR rut LIKE '%' || :query || '%' 
           OR email LIKE '%' || :query || '%'
        ORDER BY fullName ASC
    """)
    fun searchPacientes(query: String): Flow<List<PacienteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaciente(paciente: PacienteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPacientes(pacientes: List<PacienteEntity>): List<Long>

    @Update
    suspend fun updatePaciente(paciente: PacienteEntity): Int

    @Delete
    suspend fun deletePaciente(paciente: PacienteEntity): Int

    @Query("DELETE FROM pacientes WHERE id = :id")
    suspend fun deletePacienteById(id: String): Int
}
