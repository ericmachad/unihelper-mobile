package br.edu.utfpr.unihelper.disciplina.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HorarioDao {

    @Query("SELECT * FROM horarios WHERE disciplinaId = :disciplinaId ORDER BY diaSemana ASC, horaInicio ASC")
    fun listarPorDisciplina(disciplinaId: String): Flow<List<HorarioEntity>>

    @Query("SELECT * FROM horarios WHERE id = :id")
    suspend fun buscarPorId(id: String): HorarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(horario: HorarioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(horarios: List<HorarioEntity>)

    @Delete
    suspend fun deletar(horario: HorarioEntity)

    @Query("DELETE FROM horarios WHERE disciplinaId = :disciplinaId")
    suspend fun deletarPorDisciplina(disciplinaId: String)

    @Query("DELETE FROM horarios WHERE id = :id")
    suspend fun deletarPorId(id: String)
}
