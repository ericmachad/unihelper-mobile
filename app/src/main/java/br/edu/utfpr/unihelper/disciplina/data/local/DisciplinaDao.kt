package br.edu.utfpr.unihelper.disciplina.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DisciplinaDao {

    @Query("SELECT * FROM disciplinas WHERE syncStatus != 'PENDING_DELETE' ORDER BY nome ASC")
    fun listar(): Flow<List<DisciplinaEntity>>

    @Transaction
    @Query("SELECT * FROM disciplinas WHERE syncStatus != 'PENDING_DELETE' ORDER BY nome ASC")
    fun listarComHorarios(): Flow<List<DisciplinaComHorarios>>

    @Query("SELECT * FROM disciplinas WHERE id = :id")
    suspend fun buscarPorId(id: String): DisciplinaEntity?

    @Transaction
    @Query("SELECT * FROM disciplinas WHERE id = :id")
    suspend fun buscarComHorarios(id: String): DisciplinaComHorarios?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(disciplina: DisciplinaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(disciplinas: List<DisciplinaEntity>)

    @Update
    suspend fun atualizar(disciplina: DisciplinaEntity)

    @Delete
    suspend fun deletar(disciplina: DisciplinaEntity)

    @Query("DELETE FROM disciplinas WHERE id = :id")
    suspend fun deletarPorId(id: String)

    @Query("SELECT * FROM disciplinas WHERE syncStatus != 'SYNCED'")
    suspend fun listarPendentes(): List<DisciplinaEntity>

    @Query("UPDATE disciplinas SET syncStatus = :status, syncUpdatedAt = :updatedAt WHERE id = :id")
    suspend fun atualizarStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE disciplinas SET faltasRegistradas = :faltas, faltasCriticas = :criticas, syncStatus = :syncStatus, syncUpdatedAt = :updatedAt WHERE id = :id")
    suspend fun atualizarFaltas(
        id: String,
        faltas: Int,
        criticas: Boolean,
        syncStatus: String = "SYNCED",
        updatedAt: Long = System.currentTimeMillis()
    )

    @Transaction
    suspend fun substituirPorServerResponse(localId: String, serverEntity: DisciplinaEntity) {
        deletarPorId(localId)
        inserir(serverEntity)
    }
}
