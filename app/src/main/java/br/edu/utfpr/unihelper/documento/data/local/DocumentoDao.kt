package br.edu.utfpr.unihelper.documento.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {

    @Query("SELECT * FROM documentos WHERE disciplinaId = :disciplinaId ORDER BY criadoEm DESC")
    fun listar(disciplinaId: String): Flow<List<DocumentoEntity>>

    @Query("SELECT * FROM documentos WHERE disciplinaId = :disciplinaId ORDER BY criadoEm DESC")
    suspend fun listarSync(disciplinaId: String): List<DocumentoEntity>

    @Query("SELECT * FROM documentos WHERE id = :id")
    suspend fun buscarPorId(id: String): DocumentoEntity?

    @Query("SELECT * FROM documentos WHERE syncStatus != 'SYNCED'")
    suspend fun listarPendentes(): List<DocumentoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(documento: DocumentoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(documentos: List<DocumentoEntity>)

    @Delete
    suspend fun deletar(documento: DocumentoEntity)

    @Query("DELETE FROM documentos WHERE id = :id")
    suspend fun deletarPorId(id: String)

    @Query("DELETE FROM documentos WHERE disciplinaId = :disciplinaId")
    suspend fun deletarPorDisciplina(disciplinaId: String)

    @Query("UPDATE documentos SET syncStatus = :status, syncUpdatedAt = :updatedAt WHERE id = :id")
    suspend fun atualizarStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())
}
