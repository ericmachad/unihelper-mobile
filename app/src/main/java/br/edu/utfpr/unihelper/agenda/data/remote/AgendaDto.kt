package br.edu.utfpr.unihelper.agenda.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AgendaItemResponse(
    val tipoOrigem: String,
    val id: String,
    val titulo: String,
    val tipoEvento: String,
    val dataHora: String,
    val dataHoraFim: String? = null,
    val peso: Float? = null,
    val disciplinaId: String? = null,
    val disciplinaNome: String? = null
)

@Serializable
data class EventoRequest(
    val titulo: String,
    val tipo: String = "OUTRO",
    val dataHoraInicio: String,
    val dataHoraFim: String,
    val peso: Float? = null,
    val disciplinaId: String? = null
)

@Serializable
data class EventoResponse(
    val id: String,
    val titulo: String,
    val tipo: String,
    val dataHoraInicio: String,
    val dataHoraFim: String,
    val peso: Float? = null,
    val valor: Float? = null,
    val disciplinaId: String? = null,
    val disciplinaNome: String? = null
)
