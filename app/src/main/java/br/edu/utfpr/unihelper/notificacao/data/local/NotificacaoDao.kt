package br.edu.utfpr.unihelper.notificacao.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacaoDao {

    @Query("SELECT * FROM notificacoes ORDER BY criadaEm DESC")
    fun listar(): Flow<List<NotificacaoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(notificacao: NotificacaoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(notificacoes: List<NotificacaoEntity>)

    @Query("DELETE FROM notificacoes")
    suspend fun deletarTodas()

    @Query("UPDATE notificacoes SET lida = 1 WHERE id = :id")
    suspend fun marcarComoLida(id: String)

    @Query("UPDATE notificacoes SET lida = 1")
    suspend fun marcarTodasComoLidas()

    @Query("SELECT COUNT(*) FROM notificacoes WHERE lida = 0")
    fun contarNaoLidas(): Flow<Long>
}
