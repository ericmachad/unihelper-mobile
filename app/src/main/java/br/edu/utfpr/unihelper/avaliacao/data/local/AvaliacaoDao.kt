package br.edu.utfpr.unihelper.avaliacao.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AvaliacaoDao {

    @Query("SELECT * FROM avaliacoes WHERE disciplinaId = :disciplinaId ORDER BY data ASC")
    fun listarPorDisciplina(disciplinaId: String): Flow<List<AvaliacaoEntity>>

    @Query("SELECT * FROM avaliacoes WHERE id = :id")
    suspend fun buscarPorId(id: String): AvaliacaoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(avaliacao: AvaliacaoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(avaliacoes: List<AvaliacaoEntity>)

    @Update
    suspend fun atualizar(avaliacao: AvaliacaoEntity)

    @Delete
    suspend fun deletar(avaliacao: AvaliacaoEntity)

    @Query("DELETE FROM avaliacoes WHERE id = :id")
    suspend fun deletarPorId(id: String)

    @Query("UPDATE avaliacoes SET valor = :valor WHERE id = :id")
    suspend fun lancarNota(id: String, valor: Float)

    @Query("DELETE FROM avaliacoes WHERE disciplinaId = :disciplinaId")
    suspend fun deletarPorDisciplina(disciplinaId: String)

    @Query("SELECT * FROM avaliacoes WHERE syncStatus != 'SYNCED'")
    suspend fun listarPendentes(): List<AvaliacaoEntity>

    @Query("UPDATE avaliacoes SET syncStatus = :status, syncUpdatedAt = :updatedAt WHERE id = :id")
    suspend fun atualizarStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())
}
