package br.edu.utfpr.unihelper.notificacao.data.repository

import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.notificacao.data.remote.NotificacaoApi
import br.edu.utfpr.unihelper.notificacao.data.remote.NotificacaoListResponse
import br.edu.utfpr.unihelper.notificacao.data.remote.NotificacaoResponse

class NotificacaoRepository(private val api: NotificacaoApi) {

    suspend fun listar(apenasNaoLidas: Boolean? = null): Result<NotificacaoListResponse> {
        return safeApiCall { api.listar(apenasNaoLidas) }
    }

    suspend fun marcarComoLida(id: String): Result<NotificacaoResponse> {
        return safeApiCall { api.marcarComoLida(id) }
    }

    suspend fun marcarTodasComoLidas(): Result<Unit> {
        return safeApiCall { api.marcarTodasComoLidas() }.map { }
    }
}
