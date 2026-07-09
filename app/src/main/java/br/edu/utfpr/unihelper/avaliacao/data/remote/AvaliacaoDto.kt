package br.edu.utfpr.unihelper.avaliacao.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class EventoResponse(
    val id: String,
    val titulo: String,
    val tipo: String = "OUTRO",
    val dataHoraInicio: String,
    val dataHoraFim: String,
    val peso: Float? = null,
    val valor: Float? = null,
    val disciplinaId: String? = null,
    val disciplinaNome: String? = null
)

@Serializable
data class CriarEventoRequest(
    val titulo: String,
    val tipo: String? = null,
    val dataHoraInicio: String,
    val dataHoraFim: String,
    val valor: Float? = null,
    val peso: Float? = null,
    val disciplinaId: String? = null
)

@Serializable
data class LancarNotaRequest(
    val valor: Float
)

@Serializable
data class MediaResponse(
    val media: Float? = null,
    val notaMinimaNecessaria: Float? = null,
    val statusAprovacao: String = "INDEFINIDO",
    val totalPesos: Float = 0f,
    val pesosComNota: Float = 0f,
    val pesosRestantes: Float = 0f
)
