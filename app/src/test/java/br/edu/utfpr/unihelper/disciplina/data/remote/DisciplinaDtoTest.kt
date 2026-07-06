package br.edu.utfpr.unihelper.disciplina.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DisciplinaDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `disciplina response deserialization`() {
        val raw = """
        {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "nome": "Matemática",
            "cargaHorariaTotal": 60,
            "cargaHorariaSemanal": 4,
            "limiteFaltas": 10,
            "faltasRegistradas": 2,
            "faltasCriticas": false,
            "horarios": [
                {
                    "id": "uuid-1",
                    "diaSemana": 2,
                    "horaInicio": "15:00",
                    "horaFim": "16:00"
                }
            ]
        }
        """.trimIndent()

        val decoded = json.decodeFromString(DisciplinaResponse.serializer(), raw)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", decoded.id)
        assertEquals("Matemática", decoded.nome)
        assertEquals(60, decoded.cargaHorariaTotal)
        assertEquals(4, decoded.cargaHorariaSemanal)
        assertEquals(10, decoded.limiteFaltas)
        assertEquals(2, decoded.faltasRegistradas)
        assertEquals(false, decoded.faltasCriticas)
        assertEquals(1, decoded.horarios.size)
        assertEquals(2, decoded.horarios[0].diaSemana)
        assertEquals("15:00", decoded.horarios[0].horaInicio)
        assertEquals("16:00", decoded.horarios[0].horaFim)
    }

    @Test
    fun `criar disciplina request serialization`() {
        val request = CriarDisciplinaRequest(
            nome = "Matemática",
            cargaHorariaTotal = 60,
            cargaHorariaSemanal = 4,
            limiteFaltas = 10,
            horarios = listOf(
                CriarHorarioRequest(
                    diaSemana = 2,
                    horaInicio = "15:00",
                    horaFim = "16:00"
                ),
                CriarHorarioRequest(
                    diaSemana = 4,
                    horaInicio = "15:00",
                    horaFim = "16:00"
                )
            )
        )

        val encoded = json.encodeToString(CriarDisciplinaRequest.serializer(), request)
        assertTrue(encoded.contains("\"nome\":\"Matemática\""))
        assertTrue(encoded.contains("\"cargaHorariaTotal\":60"))
        assertTrue(encoded.contains("\"cargaHorariaSemanal\":4"))
        assertTrue(encoded.contains("\"limiteFaltas\":10"))
        assertTrue(encoded.contains("\"diaSemana\":2"))
        assertTrue(encoded.contains("\"horaInicio\":\"15:00\""))
    }

    @Test
    fun `alterar faltas request serialization`() {
        val request = AlterarFaltasRequest(operacao = "INCREMENTAR")
        val encoded = json.encodeToString(AlterarFaltasRequest.serializer(), request)
        assertEquals("""{"operacao":"INCREMENTAR"}""", encoded)
    }

    @Test
    fun `dia semana from int`() {
        assertEquals(DiaSemana.SEGUNDA, DiaSemana.fromInt(2))
        assertEquals(DiaSemana.TERCA, DiaSemana.fromInt(3))
        assertEquals(DiaSemana.QUARTA, DiaSemana.fromInt(4))
        assertEquals(DiaSemana.QUINTA, DiaSemana.fromInt(5))
        assertEquals(DiaSemana.SEXTA, DiaSemana.fromInt(6))
        assertEquals(null, DiaSemana.fromInt(1))
        assertEquals(null, DiaSemana.fromInt(7))
    }

    @Test
    fun `disciplina response with faltas criticas`() {
        val raw = """
        {
            "id": "uuid",
            "nome": "Física",
            "cargaHorariaTotal": 60,
            "cargaHorariaSemanal": 4,
            "limiteFaltas": 10,
            "faltasRegistradas": 8,
            "faltasCriticas": true,
            "horarios": []
        }
        """.trimIndent()

        val decoded = json.decodeFromString(DisciplinaResponse.serializer(), raw)
        assertTrue(decoded.faltasCriticas)
        assertEquals(8, decoded.faltasRegistradas)
    }

    @Test
    fun `criar horario request serialization`() {
        val request = CriarHorarioRequest(diaSemana = 3, horaInicio = "08:00", horaFim = "09:00")
        val encoded = json.encodeToString(CriarHorarioRequest.serializer(), request)
        val expected = """{"diaSemana":3,"horaInicio":"08:00","horaFim":"09:00"}"""
        assertEquals(expected, encoded)
    }
}
