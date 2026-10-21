package com.example.oftapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.oftapp.data.local.entity.DocumentoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {

    @Query("SELECT * FROM documentos ORDER BY uploadDate DESC")
    fun getAllDocumentos(): Flow<List<DocumentoEntity>>

    @Query("SELECT * FROM documentos WHERE id = :id")
    fun getDocumentoById(id: String): Flow<DocumentoEntity?>

    @Query("SELECT * FROM documentos WHERE id = :id")
    suspend fun findDocumentoById(id: String): DocumentoEntity?

    @Query("SELECT * FROM documentos WHERE examId = :examId ORDER BY uploadDate DESC")
    fun getDocumentosByExamId(examId: String): Flow<List<DocumentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumento(documento: DocumentoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentos(documentos: List<DocumentoEntity>): List<Long>

    @Update
    suspend fun updateDocumento(documento: DocumentoEntity): Int

    @Delete
    suspend fun deleteDocumento(documento: DocumentoEntity): Int

    @Query("DELETE FROM documentos WHERE id = :id")
    suspend fun deleteDocumentoById(id: String): Int
}
