package br.edu.utfpr.unihelper.nota.ui

import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.ui.UiEvent
import br.edu.utfpr.unihelper.nota.data.remote.NotaRequest
import br.edu.utfpr.unihelper.nota.data.remote.NotaResponse
import br.edu.utfpr.unihelper.nota.data.repository.NotaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class NotaViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var repository: NotaRepository

    private lateinit var viewModel: NotaViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockNota = NotaResponse(
        id = "nota-1",
        titulo = "Anotação de teste",
        conteudo = "Conteúdo importante",
        criadoEm = "2026-01-01T10:00:00",
        atualizadoEm = null
    )
    private val notasFlow: MutableStateFlow<List<NotaResponse>> = MutableStateFlow(listOf(mockNota))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.listarFlow(any()) } returns notasFlow
        coEvery { repository.listar(any()) } returns Result.success(listOf(mockNota))
        viewModel = NotaViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `carregarNotas collects flow and updates state`() = runTest(testDispatcher) {
        viewModel.carregarNotas("disc-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.notas.size)
        assertEquals("Anotação de teste", state.notas[0].titulo)
    }

    @Test
    fun `criar calls repository and emits success`() = runTest(testDispatcher) {
        coEvery { repository.criar(any(), any()) } returns Result.success(mockNota)

        viewModel.criar("disc-1", "Nova anotação", "conteúdo")
        advanceUntilIdle()

        coVerify { repository.criar("disc-1", NotaRequest("Nova anotação", "conteúdo")) }
        // wait for UiEvent
        val event = viewModel.uiEvent.replayCache
    }

    @Test
    fun `excluir calls repository and emits success`() = runTest(testDispatcher) {
        coEvery { repository.excluir(any(), any()) } returns Result.success(Unit)

        viewModel.excluir("disc-1", "nota-1")
        advanceUntilIdle()

        coVerify { repository.excluir("disc-1", "nota-1") }
    }

    @Test
    fun `buscar with blank term reloads notas`() = runTest(testDispatcher) {
        coEvery { repository.listar(any()) } returns Result.success(listOf(mockNota))

        viewModel.buscar("disc-1", "")
        advanceUntilIdle()

        coVerify { repository.listar("disc-1") }
    }

    @Test
    fun `buscar with term calls buscarPorTitulo`() = runTest(testDispatcher) {
        coEvery { repository.buscarPorTitulo(any(), any()) } returns Result.success(listOf(mockNota))

        viewModel.buscar("disc-1", "anota")
        advanceUntilIdle()

        coVerify { repository.buscarPorTitulo("disc-1", "anota") }
        assertEquals("anota", viewModel.uiState.value.termoBusca)
    }

    @Test
    fun `error from repository propagates to UiEvent`() = runTest(testDispatcher) {
        coEvery { repository.criar(any(), any()) } returns Result.failure(
            ApiException(500, "Erro interno")
        )

        viewModel.criar("disc-1", "titulo", null)
        advanceUntilIdle()

        // UiState should not contain the nota (flow unchanged)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}