package br.edu.utfpr.unihelper.nota.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotaDao {

    @Query("SELECT * FROM notas WHERE disciplinaId = :disciplinaId ORDER BY criadoEm DESC")
    fun listar(disciplinaId: String): Flow<List<NotaEntity>>

    @Query("SELECT * FROM notas WHERE disciplinaId = :disciplinaId ORDER BY criadoEm DESC")
    suspend fun listarSync(disciplinaId: String): List<NotaEntity>

    @Query("SELECT * FROM notas WHERE id = :id")
    suspend fun buscarPorId(id: String): NotaEntity?

    @Query("SELECT * FROM notas WHERE syncStatus != 'SYNCED'")
    suspend fun listarPendentes(): List<NotaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(nota: NotaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(notas: List<NotaEntity>)

    @Update
    suspend fun atualizar(nota: NotaEntity)

    @Delete
    suspend fun deletar(nota: NotaEntity)

    @Query("DELETE FROM notas WHERE id = :id")
    suspend fun deletarPorId(id: String)

    @Query("DELETE FROM notas WHERE disciplinaId = :disciplinaId")
    suspend fun deletarPorDisciplina(disciplinaId: String)

    @Query("UPDATE notas SET syncStatus = :status, syncUpdatedAt = :updatedAt WHERE id = :id")
    suspend fun atualizarStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())
}