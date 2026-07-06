package br.edu.utfpr.unihelper.disciplina.data.repository

import br.edu.utfpr.unihelper.disciplina.data.remote.AlterarFaltasRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.CriarDisciplinaRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.CriarHorarioRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaApi
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaResponse
import br.edu.utfpr.unihelper.disciplina.data.remote.HorarioResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class DisciplinaRepositoryTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var api: DisciplinaApi

    private lateinit var repository: DisciplinaRepository

    private val mockDisciplina = DisciplinaResponse(
        id = "uuid",
        nome = "Matemática",
        cargaHorariaTotal = 60,
        cargaHorariaSemanal = 4,
        limiteFaltas = 10,
        faltasRegistradas = 2,
        faltasCriticas = false,
        horarios = listOf(
            HorarioResponse(
                id = "horario-uuid",
                diaSemana = 2,
                horaInicio = "15:00",
                horaFim = "16:00"
            )
        )
    )

    @Before
    fun setup() {
        repository = DisciplinaRepository(api)
    }

    @Test
    fun `listar returns disciplinas`() = runTest {
        coEvery { api.listar() } returns listOf(mockDisciplina)

        val result = repository.listar()

        assertTrue(result.isSuccess)
        val disciplinas = result.getOrThrow()
        assertEquals(1, disciplinas.size)
        assertEquals("Matemática", disciplinas[0].nome)
        coVerify { api.listar() }
    }

    @Test
    fun `buscarPorId returns disciplina`() = runTest {
        coEvery { api.buscarPorId("uuid") } returns mockDisciplina

        val result = repository.buscarPorId("uuid")

        assertTrue(result.isSuccess)
        assertEquals("Matemática", result.getOrThrow().nome)
        coVerify { api.buscarPorId("uuid") }
    }

    @Test
    fun `criar returns created disciplina`() = runTest {
        val request = CriarDisciplinaRequest(
            nome = "Matemática",
            cargaHorariaTotal = 60,
            cargaHorariaSemanal = 4,
            limiteFaltas = 10,
            horarios = listOf(
                CriarHorarioRequest(diaSemana = 2, horaInicio = "15:00", horaFim = "16:00")
            )
        )
        coEvery { api.criar(request) } returns mockDisciplina

        val result = repository.criar(request)

        assertTrue(result.isSuccess)
        assertEquals("Matemática", result.getOrThrow().nome)
        coVerify { api.criar(request) }
    }

    @Test
    fun `alterarFaltas returns updated disciplina`() = runTest {
        val disciplinaAtualizada = mockDisciplina.copy(faltasRegistradas = 3)
        coEvery { api.alterarFaltas("uuid", AlterarFaltasRequest("INCREMENTAR")) } returns disciplinaAtualizada

        val result = repository.alterarFaltas("uuid", "INCREMENTAR")

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrThrow().faltasRegistradas)
        coVerify { api.alterarFaltas("uuid", AlterarFaltasRequest("INCREMENTAR")) }
    }

    @Test
    fun `listar returns failure on error`() = runTest {
        coEvery { api.listar() } throws Exception("Erro de conexão")

        val result = repository.listar()

        assertTrue(result.isFailure)
        assertEquals("Erro de conexão", result.exceptionOrNull()?.message)
    }
}
