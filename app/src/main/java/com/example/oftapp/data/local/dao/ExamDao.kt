package com.example.oftapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.oftapp.data.local.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {

    @Query("SELECT * FROM exams ORDER BY date DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :id")
    fun getExamById(id: String): Flow<ExamEntity?>

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun findExamById(id: String): ExamEntity?

    @Query("""
        SELECT * FROM exams 
        WHERE patientName LIKE '%' || :query || '%' 
           OR examType LIKE '%' || :query || '%' 
           OR id LIKE '%' || :query || '%'
           OR branch LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)
    fun searchExams(query: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE patientName = :patientName ORDER BY date DESC")
    fun getExamsByPatientName(patientName: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE consultaId = :consultaId ORDER BY date DESC")
    fun getExamsByConsultaId(consultaId: String): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>): List<Long>

    @Update
    suspend fun updateExam(exam: ExamEntity): Int

    @Delete
    suspend fun deleteExam(exam: ExamEntity): Int

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: String): Int
}
