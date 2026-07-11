package br.edu.utfpr.unihelper.disciplina.data.remote

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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

object HoraMinutoSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HoraMinuto", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
    override fun deserialize(decoder: Decoder): String {
        val raw = decoder.decodeString()
        return if (raw.count { it == ':' } >= 2) raw.substringBeforeLast(":")
        else raw
    }
}

@Serializable
data class HorarioResponse(
    val id: String,
    val diaSemana: Int,
    @Serializable(with = HoraMinutoSerializer::class) val horaInicio: String,
    @Serializable(with = HoraMinutoSerializer::class) val horaFim: String
)

@Serializable
data class DisciplinaResponse(
    val id: String,
    val nome: String,
    val professor: String? = null,
    val bloco: String? = null,
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
    @Serializable(with = HoraMinutoSerializer::class) val horaInicio: String,
    @Serializable(with = HoraMinutoSerializer::class) val horaFim: String
)

@Serializable
data class CriarDisciplinaRequest(
    val nome: String,
    val professor: String? = null,
    val bloco: String? = null,
    val cargaHorariaTotal: Int,
    val cargaHorariaSemanal: Int,
    val limiteFaltas: Int,
    val horarios: List<CriarHorarioRequest>
)

@Serializable
data class AlterarFaltasRequest(
    val operacao: String
)
