package br.edu.utfpr.unihelper.notificacao.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class NotificacaoResponse(
    val id: String,
    val tipo: String,
    val titulo: String,
    val mensagem: String,
    val lida: Boolean,
    val criadaEm: String
)

@Serializable
data class NotificacaoListResponse(
    val notificacoes: List<NotificacaoResponse>,
    val total: Long,
    val totalNaoLidas: Long
)
