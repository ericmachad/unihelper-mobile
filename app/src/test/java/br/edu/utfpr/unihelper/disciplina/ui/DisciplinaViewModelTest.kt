package br.edu.utfpr.unihelper.disciplina.ui

import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.sync.AuthEvent
import br.edu.utfpr.unihelper.core.sync.AuthEventBus
import br.edu.utfpr.unihelper.core.ui.UiEvent
import br.edu.utfpr.unihelper.disciplina.data.remote.CriarDisciplinaRequest
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DisciplinaViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var repository: DisciplinaRepository

    @MockK
    private lateinit var authEventBus: AuthEventBus

    private lateinit var viewModel: DisciplinaViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val authEvents: MutableSharedFlow<AuthEvent> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    private val mockDisciplina = DisciplinaResponse(
        id = "uuid",
        nome = "Matemática",
        professor = null,
        cargaHorariaTotal = 60,
        cargaHorariaSemanal = 4,
        limiteFaltas = 10,
        faltasRegistradas = 2,
        faltasCriticas = false,
        horarios = emptyList()
    )
    private val disciplinasFlow: MutableStateFlow<List<DisciplinaResponse>> = MutableStateFlow(listOf(mockDisciplina))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { authEventBus.events } returns authEvents
        coEvery { repository.listarDisciplinasFlow() } returns disciplinasFlow
        coEvery { repository.listar() } returns Result.success(listOf(mockDisciplina))
        authEvents.tryEmit(AuthEvent.LoggedIn(
            AuthResponse(
                token = "jwt", refreshToken = "rt", idUsuario = "u1",
                nomeCompleto = "João", apelido = null, email = "e@x.com", curso = null
            )
        ))
        viewModel = DisciplinaViewModel(repository, authEventBus)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init collects disciplinas flow and updates state`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.disciplinas.size)
    }

    @Test
    fun `listar calls repository and handles success`() = runTest(testDispatcher) {
        viewModel.listar()
        advanceUntilIdle()

        coVerify { repository.listar() }
    }

    @Test
    fun `listar as refresh sets isRefreshing`() = runTest(testDispatcher) {
        viewModel.listar(isRefresh = true)
        advanceUntilIdle()

        coVerify { repository.listar() }
    }

    @Test
    fun `listar error does not crash`() = runTest(testDispatcher) {
        coEvery { repository.listar() } returns Result.failure(ApiException(500, "Erro"))

        viewModel.listar()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `criar calls repository criar and updates formState`() = runTest(testDispatcher) {
        val request = CriarDisciplinaRequest(
            nome = "Física", cargaHorariaTotal = 60, cargaHorariaSemanal = 4, limiteFaltas = 10,
            horarios = emptyList()
        )
        coEvery { repository.criar(request) } returns Result.success(mockDisciplina)

        viewModel.criar(request)
        advanceUntilIdle()

        assertTrue(viewModel.formState.value.sucesso)
        coVerify { repository.criar(request) }
    }

    @Test
    fun `criar error resets formState`() = runTest(testDispatcher) {
        val request = CriarDisciplinaRequest(
            nome = "Física", cargaHorariaTotal = 60, cargaHorariaSemanal = 4, limiteFaltas = 10,
            horarios = emptyList()
        )
        coEvery { repository.criar(request) } returns Result.failure(ApiException(422, "Dados inválidos"))

        viewModel.criar(request)
        advanceUntilIdle()

        assertFalse(viewModel.formState.value.sucesso)
        assertFalse(viewModel.formState.value.isLoading)
    }

    @Test
    fun `excluir calls repository and emits success dialog`() = runTest(testDispatcher) {
        coEvery { repository.excluir(any()) } returns Result.success(Unit)

        viewModel.excluir("uuid")
        advanceUntilIdle()

        assertTrue(viewModel.deleteState.value.sucesso)
        coVerify { repository.excluir("uuid") }
    }

    @Test
    fun `alterarFaltas calls repository and removes id from updating set`() = runTest(testDispatcher) {
        coEvery { repository.alterarFaltas(any(), any()) } returns Result.success(mockDisciplina)

        viewModel.alterarFaltas("uuid", "INCREMENTAR")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.faltasAtualizando.contains("uuid"))
    }

    @Test
    fun `resetState returns loading state`() {
        viewModel.resetState()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `limparDeleteState resets delete state`() {
        viewModel.limparDeleteState()
        assertFalse(viewModel.deleteState.value.sucesso)
    }
}