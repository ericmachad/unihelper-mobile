package br.edu.utfpr.unihelper.avaliacao.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AvaliacaoResponse(
    val id: String,
    val descricao: String,
    val peso: Float,
    val data: String,
    val valor: Float? = null,
    val disciplinaId: String
)

@Serializable
data class CriarAvaliacaoRequest(
    val descricao: String,
    val peso: Float,
    val data: String,
    val valor: Float? = null
)

@Serializable
data class LancarNotaRequest(
    val valor: Float
)
