package br.edu.utfpr.unihelper.agenda.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {

    @Query("SELECT * FROM eventos WHERE dataHoraInicio >= :dataInicio AND dataHoraInicio <= :dataFim ORDER BY dataHoraInicio ASC")
    fun listar(dataInicio: String, dataFim: String): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE disciplinaId = :disciplinaId ORDER BY dataHoraInicio ASC")
    fun listarPorDisciplina(disciplinaId: String): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE id = :id")
    suspend fun buscarPorId(id: String): EventoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(evento: EventoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(eventos: List<EventoEntity>)

    @Update
    suspend fun atualizar(evento: EventoEntity)

    @Delete
    suspend fun deletar(evento: EventoEntity)

    @Query("DELETE FROM eventos WHERE id = :id")
    suspend fun deletarPorId(id: String)

    @Query("DELETE FROM eventos WHERE dataHoraInicio >= :dataInicio AND dataHoraInicio <= :dataFim")
    suspend fun deletarPorPeriodo(dataInicio: String, dataFim: String)

    @Query("DELETE FROM eventos WHERE disciplinaId = :disciplinaId")
    suspend fun deletarPorDisciplina(disciplinaId: String)

    @Query("SELECT * FROM eventos WHERE syncStatus != 'SYNCED'")
    suspend fun listarPendentes(): List<EventoEntity>

    @Query("UPDATE eventos SET syncStatus = :status, syncUpdatedAt = :updatedAt WHERE id = :id")
    suspend fun atualizarStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())
}
