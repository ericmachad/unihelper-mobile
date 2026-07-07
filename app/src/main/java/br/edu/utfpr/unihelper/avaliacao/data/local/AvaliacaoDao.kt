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

    @Update
    suspend fun atualizar(avaliacao: AvaliacaoEntity)

    @Delete
    suspend fun deletar(avaliacao: AvaliacaoEntity)

    @Query("DELETE FROM avaliacoes WHERE id = :id")
    suspend fun deletarPorId(id: String)

    @Query("UPDATE avaliacoes SET valor = :valor WHERE id = :id")
    suspend fun lancarNota(id: String, valor: Float)
}
