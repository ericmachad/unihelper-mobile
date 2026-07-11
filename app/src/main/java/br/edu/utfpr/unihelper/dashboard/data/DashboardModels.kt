package br.edu.utfpr.unihelper.dashboard.data

import br.edu.utfpr.unihelper.agenda.data.local.EventoEntity
import br.edu.utfpr.unihelper.agenda.data.remote.AgendaItemResponse
import java.time.LocalDate
import java.time.LocalDateTime

data class DashboardEvent(
    val id: String,
    val day: Int,
    val title: String,
    val subject: String?,
    val type: String,
    val disciplinaId: String?,
    val dataHora: String,
    val dataHoraFim: String?
)

fun AgendaItemResponse.toDashboardEvent(): DashboardEvent? {
    val date = try {
        LocalDateTime.parse(dataHora).toLocalDate()
    } catch (_: Exception) {
        try {
            LocalDate.parse(dataHora.take(10))
        } catch (_: Exception) {
            return null
        }
    }
    return DashboardEvent(
        id = id,
        day = date.dayOfMonth,
        title = titulo,
        subject = disciplinaNome,
        type = tipoEvento,
        disciplinaId = disciplinaId,
        dataHora = dataHora,
        dataHoraFim = dataHoraFim
    )
}

fun EventoEntity.toDashboardEvent(): DashboardEvent? {
    val date = try {
        LocalDateTime.parse(dataHoraInicio).toLocalDate()
    } catch (_: Exception) {
        try {
            LocalDate.parse(dataHoraInicio.take(10))
        } catch (_: Exception) {
            return null
        }
    }
    return DashboardEvent(
        id = id,
        day = date.dayOfMonth,
        title = titulo,
        subject = disciplinaNome,
        type = tipo,
        disciplinaId = disciplinaId,
        dataHora = dataHoraInicio,
        dataHoraFim = dataHoraFim
    )
}