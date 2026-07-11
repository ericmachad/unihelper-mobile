package br.edu.utfpr.unihelper.agenda.ui

import br.edu.utfpr.unihelper.agenda.data.local.EventoEntity
import br.edu.utfpr.unihelper.agenda.data.remote.AgendaItemResponse
import br.edu.utfpr.unihelper.agenda.data.remote.EventoRequest
import br.edu.utfpr.unihelper.agenda.data.remote.EventoResponse
import br.edu.utfpr.unihelper.agenda.data.repository.AgendaRepository
import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.sync.AuthEvent
import br.edu.utfpr.unihelper.core.sync.AuthEventBus
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaResponse
import br.edu.utfpr.unihelper.disciplina.data.repository.DisciplinaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var repository: AgendaRepository

    @MockK
    private lateinit var disciplinaRepository: DisciplinaRepository

    @MockK
    private lateinit var authEventBus: AuthEventBus

    private lateinit var viewModel: AgendaViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val authEvents: MutableSharedFlow<AuthEvent> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    private val mockEventoEntity = EventoEntity(
        id = "ev-1",
        titulo = "Prova",
        tipo = "PROVA",
        dataHoraInicio = "2026-07-10T10:00:00",
        dataHoraFim = "2026-07-10T12:00:00",
        disciplinaId = "disc-1",
        disciplinaNome = "Matemática"
    )

    private val mockEventoResponse = EventoResponse(
        id = "ev-1",
        titulo = "Prova",
        tipo = "PROVA",
        dataHoraInicio = "2026-07-10T10:00:00",
        dataHoraFim = "2026-07-10T12:00:00",
        disciplinaId = "disc-1",
        disciplinaNome = "Matemática"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { authEventBus.events } returns authEvents
        authEvents.tryEmit(
            AuthEvent.LoggedIn(
                AuthResponse(
                    token = "jwt", refreshToken = "rt", idUsuario = "u1",
                    nomeCompleto = "João", apelido = null, email = "e@x.com", curso = null
                )
            )
        )
        coEvery { repository.listarFlow(any(), any()) } returns flowOf(listOf(mockEventoEntity))
        coEvery { repository.listar(any(), any()) } returns Result.success(
            listOf(
                AgendaItemResponse(
                    tipoOrigem = "EVENTO", id = "ev-1", titulo = "Prova",
                    tipoEvento = "PROVA", dataHora = "2026-07-10T10:00:00",
                    dataHoraFim = "2026-07-10T12:00:00", peso = null,
                    disciplinaId = "disc-1", disciplinaNome = "Matemática"
                )
            )
        )
        coEvery { disciplinaRepository.listarDisciplinasFlow() } returns flowOf(emptyList<DisciplinaResponse>())
        viewModel = AgendaViewModel(repository, disciplinaRepository, authEventBus)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init collects event flow and maps to agenda items`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.itens.size)
        assertEquals("Prova", state.itens[0].titulo)
        assertEquals("PROVA", state.itens[0].tipoEvento)
    }

    @Test
    fun `abrirCriarEvento opens bottom sheet`() {
        viewModel.abrirCriarEvento()
        assertTrue(viewModel.uiState.value.showEventoBottomSheet)
        assertEquals(null, viewModel.uiState.value.eventoParaEditar)
    }

    @Test
    fun `fecharEventoBottomSheet hides sheet`() {
        viewModel.abrirCriarEvento()
        viewModel.fecharEventoBottomSheet()
        assertFalse(viewModel.uiState.value.showEventoBottomSheet)
    }

    @Test
    fun `salvarEvento creates when not editing`() = runTest(testDispatcher) {
        coEvery { repository.criarEvento(any()) } returns Result.success(mockEventoResponse)

        viewModel.salvarEvento("Prova", "PROVA", "2026-08-10T10:00:00", "2026-08-10T12:00:00", null, "disc-1")
        advanceUntilIdle()

        coVerify { repository.criarEvento(any()) }
        assertFalse(viewModel.uiState.value.showEventoBottomSheet)
    }

    @Test
    fun `salvarEvento updates when editing`() = runTest(testDispatcher) {
        val agendaItem = AgendaItemResponse(
            tipoOrigem = "EVENTO", id = "ev-1", titulo = "Prova",
            tipoEvento = "PROVA", dataHora = "2026-08-10T10:00:00",
            dataHoraFim = "2026-08-10T12:00:00", peso = null,
            disciplinaId = "disc-1", disciplinaNome = "Matemática"
        )
        viewModel.abrirEditarEvento(agendaItem)
        coEvery { repository.atualizarEvento(any(), any()) } returns Result.success(mockEventoResponse)

        viewModel.salvarEvento("Prova Editada", "PROVA", "2026-08-10T10:00:00", "2026-08-10T12:00:00", null, "disc-1")
        advanceUntilIdle()

        coVerify { repository.atualizarEvento("ev-1", any()) }
        assertFalse(viewModel.uiState.value.showEventoBottomSheet)
    }

    @Test
    fun `excluirEvento calls repository and closes confirm`() = runTest(testDispatcher) {
        viewModel.confirmarExcluir("ev-1")
        coEvery { repository.excluirEvento(any()) } returns Result.success(Unit)

        viewModel.excluirEvento()
        advanceUntilIdle()

        coVerify { repository.excluirEvento("ev-1") }
        assertFalse(viewModel.uiState.value.showExcluirConfirm)
    }

    @Test
    fun `confirmarExcluir sets showExcluirConfirm`() {
        viewModel.confirmarExcluir("ev-123")
        assertTrue(viewModel.uiState.value.showExcluirConfirm)
        assertEquals("ev-123", viewModel.uiState.value.eventoExcluirId)
    }

    @Test
    fun `cancelarExcluir hides confirm dialog`() {
        viewModel.confirmarExcluir("ev-1")
        viewModel.cancelarExcluir()
        assertFalse(viewModel.uiState.value.showExcluirConfirm)
        assertEquals(null, viewModel.uiState.value.eventoExcluirId)
    }

    @Test
    fun `resetState returns to loading`() {
        viewModel.resetState()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `salvarEvento handles API error`() = runTest(testDispatcher) {
        coEvery { repository.criarEvento(any()) } returns Result.failure(ApiException(500, "Erro"))

        viewModel.salvarEvento("Prova", "PROVA", "2026-08-10T10:00:00", "2026-08-10T12:00:00", null, "disc-1")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showEventoBottomSheet)
    }
}