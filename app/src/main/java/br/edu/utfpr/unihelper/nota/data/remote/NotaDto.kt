package br.edu.utfpr.unihelper.nota.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class NotaRequest(
    val titulo: String,
    val conteudo: String? = null
)

@Serializable
data class NotaResponse(
    val id: String,
    val titulo: String,
    val conteudo: String? = null,
    val criadoEm: String,
    val atualizadoEm: String? = null
)
