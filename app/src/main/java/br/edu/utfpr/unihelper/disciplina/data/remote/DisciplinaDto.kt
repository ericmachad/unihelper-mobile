package br.edu.utfpr.unihelper.disciplina.data.remote

import kotlinx.serialization.Serializable

enum class DiaSemana(val valor: Int, val label: String, val abrev: String) {
    SEGUNDA(2, "Segunda", "Seg"),
    TERCA(3, "Terça", "Ter"),
    QUARTA(4, "Quarta", "Qua"),
    QUINTA(5, "Quinta", "Qui"),
    SEXTA(6, "Sexta", "Sex");

    companion object {
        fun fromInt(valor: Int): DiaSemana? = entries.find { it.valor == valor }
    }
}

@Serializable
data class HorarioResponse(
    val id: String,
    val diaSemana: Int,
    val horaInicio: String,
    val horaFim: String
)

@Serializable
data class DisciplinaResponse(
    val id: String,
    val nome: String,
    val professor: String? = null,
    val cargaHorariaTotal: Int,
    val cargaHorariaSemanal: Int,
    val limiteFaltas: Int,
    val faltasRegistradas: Int,
    val faltasCriticas: Boolean,
    val media: Float? = null,
    val horarios: List<HorarioResponse>
)

@Serializable
data class CriarHorarioRequest(
    val diaSemana: Int,
    val horaInicio: String,
    val horaFim: String
)

@Serializable
data class CriarDisciplinaRequest(
    val nome: String,
    val professor: String? = null,
    val cargaHorariaTotal: Int,
    val cargaHorariaSemanal: Int,
    val limiteFaltas: Int,
    val horarios: List<CriarHorarioRequest>
)

@Serializable
data class AlterarFaltasRequest(
    val operacao: String
)
