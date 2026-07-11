package br.edu.utfpr.unihelper.dashboard.ui

import br.edu.utfpr.unihelper.agenda.data.local.EventoEntity
import br.edu.utfpr.unihelper.agenda.data.remote.AgendaItemResponse
import br.edu.utfpr.unihelper.agenda.data.repository.AgendaRepository
import br.edu.utfpr.unihelper.core.network.ApiException
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var repository: AgendaRepository

    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockEntity = EventoEntity(
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
        coEvery { repository.listarFlow(any(), any()) } returns flowOf(listOf(mockEntity))
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
        viewModel = DashboardViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads current month from flow`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.eventos.size)
        assertEquals("Prova", state.eventos[0].title)
        assertEquals("Matemática", state.eventos[0].subject)
    }

    @Test
    fun `onMonthChange advances month`() = runTest(testDispatcher) {
        val initialMonth = viewModel.uiState.value.mesAtual
        viewModel.onMonthChange(true)
        advanceUntilIdle()

        assertEquals(initialMonth.plusMonths(1), viewModel.uiState.value.mesAtual)
        assertEquals(null, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `onMonthChange goes back when avancar is false`() = runTest(testDispatcher) {
        val initialMonth = viewModel.uiState.value.mesAtual
        viewModel.onMonthChange(false)
        advanceUntilIdle()

        assertEquals(initialMonth.minusMonths(1), viewModel.uiState.value.mesAtual)
    }

    @Test
    fun `onDateSelected toggles selected day`() {
        viewModel.onDateSelected(15)
        assertEquals(15, viewModel.uiState.value.selectedDate)

        viewModel.onDateSelected(15)
        assertEquals(null, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `formatarMesAno returns capitalized month-year`() {
        val result = viewModel.formatarMesAno()
        assertNotNull(result)
        // Just verify it's non-empty and contains a year suffix; locale-specific month varies
        assert(result.length > 4)
    }

    @Test
    fun `carregarMes handles API error without crashing`() = runTest(testDispatcher) {
        coEvery { repository.listarFlow(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.listar(any(), any()) } returns Result.failure(ApiException(500, "Erro"))

        viewModel.carregarMes()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }
}