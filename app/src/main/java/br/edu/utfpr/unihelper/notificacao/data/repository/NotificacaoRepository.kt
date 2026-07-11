package br.edu.utfpr.unihelper.notificacao.data.repository

import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.notificacao.data.local.NotificacaoDao
import br.edu.utfpr.unihelper.notificacao.data.local.NotificacaoEntity
import br.edu.utfpr.unihelper.notificacao.data.remote.NotificacaoApi
import br.edu.utfpr.unihelper.notificacao.data.remote.NotificacaoListResponse
import br.edu.utfpr.unihelper.notificacao.data.remote.NotificacaoResponse
import kotlinx.coroutines.flow.Flow

class NotificacaoRepository(
    private val api: NotificacaoApi,
    private val dao: NotificacaoDao
) {
    fun listarFlow(): Flow<List<NotificacaoEntity>> = dao.listar()

    fun contarNaoLidas(): Flow<Long> = dao.contarNaoLidas()

    suspend fun listar(apenasNaoLidas: Boolean? = null): Result<NotificacaoListResponse> {
        return safeApiCall { api.listar(apenasNaoLidas) }.onSuccess { response ->
            dao.deletarTodas()
            dao.inserirTodas(response.notificacoes.map { it.toEntity() })
        }
    }

    suspend fun marcarComoLida(id: String): Result<NotificacaoResponse> {
        dao.marcarComoLida(id)
        return safeApiCall { api.marcarComoLida(id) }
    }

    suspend fun marcarTodasComoLidas(): Result<Unit> {
        dao.marcarTodasComoLidas()
        return safeApiCall { api.marcarTodasComoLidas() }
    }
}

private fun NotificacaoResponse.toEntity() = NotificacaoEntity(
    id = id,
    tipo = tipo,
    titulo = titulo,
    mensagem = mensagem,
    lida = lida,
    criadaEm = criadaEm
)
