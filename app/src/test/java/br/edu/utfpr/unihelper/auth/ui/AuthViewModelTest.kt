package br.edu.utfpr.unihelper.auth.ui

import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.auth.data.remote.RegisterResponse
import br.edu.utfpr.unihelper.auth.data.repository.AuthRepository
import br.edu.utfpr.unihelper.auth.data.repository.LoginResult
import br.edu.utfpr.unihelper.core.sync.AuthEvent
import br.edu.utfpr.unihelper.core.sync.AuthEventBus
import br.edu.utfpr.unihelper.core.ui.UiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var repository: AuthRepository

    @MockK
    private lateinit var authEventBus: AuthEventBus

    private lateinit var viewModel: AuthViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val mockEvents = MutableSharedFlow<AuthEvent>()
        every { authEventBus.events } returns mockEvents
        viewModel = AuthViewModel(repository, authEventBus)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val mockUser = AuthResponse(
        token = "jwt-test",
        refreshToken = "rt-test",
        idUsuario = "uuid",
        nomeCompleto = "João",
        apelido = "joao",
        email = "joao@utfpr.edu.br",
        curso = "CC"
    )

    @Test
    fun `login emits success with user`() = runTest(testDispatcher) {
        coEvery { repository.login(any(), any()) } returns LoginResult.Success(mockUser)

        viewModel.login("joao@utfpr.edu.br", "123456")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertEquals(mockUser, state.user)
    }

    @Test
    fun `login emits error on failure`() = runTest(testDispatcher) {
        coEvery { repository.login(any(), any()) } returns LoginResult.Error(Exception("Email ou senha inválidos"))

        val events = mutableListOf<UiEvent>()
        val collectorJob = launch {
            viewModel.uiEvent.collect { events.add(it) }
        }

        viewModel.login("joao@utfpr.edu.br", "senha_errada")
        advanceUntilIdle()
        collectorJob.cancel()

        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertNull(state.user)
        assertTrue(events.isNotEmpty())
        val errorDialog = events.first() as UiEvent.ErrorDialog
        assertEquals("Email ou senha inválidos", errorDialog.message)
    }

    @Test
    fun `login emits email not confirmed`() = runTest(testDispatcher) {
        coEvery { repository.login(any(), any()) } returns LoginResult.EmailNotConfirmed

        viewModel.login("joao@utfpr.edu.br", "123456")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertNotNull(state.pendingConfirmationEmail)
        assertEquals("joao@utfpr.edu.br", state.pendingConfirmationEmail)
    }

    @Test
    fun `register emits success with email`() = runTest(testDispatcher) {
        val registerResponse = RegisterResponse(
            mensagem = "Confirme seu email",
            email = "joao@utfpr.edu.br"
        )
        coEvery { repository.register(any(), any(), any(), any(), any()) } returns Result.success(registerResponse)

        viewModel.register("João", null, "joao@utfpr.edu.br", "123456", null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("joao@utfpr.edu.br", state.registeredEmail)
    }

    @Test
    fun `register emits error on failure`() = runTest(testDispatcher) {
        coEvery { repository.register(any(), any(), any(), any(), any()) } returns Result.failure(Exception("Email já cadastrado"))

        val events = mutableListOf<UiEvent>()
        val collectorJob = launch {
            viewModel.uiEvent.collect { events.add(it) }
        }

        viewModel.register("João", null, "existente@email.com", "123456", null)
        advanceUntilIdle()
        collectorJob.cancel()

        val state = viewModel.uiState.value
        assertNull(state.registeredEmail)
        assertTrue(events.isNotEmpty())
        val errorDialog = events.first() as UiEvent.ErrorDialog
        assertEquals("Email já cadastrado", errorDialog.message)
    }

    @Test
    fun `checkSession sets invalid when no session`() = runTest(testDispatcher) {
        every { repository.hasSession() } returns false
        every { repository.hasPendingConfirmation() } returns false

        viewModel.checkSession()

        val state = viewModel.uiState.value
        assertTrue(state.sessionChecked)
        assertFalse(state.isSessionValid)
    }

    @Test
    fun `checkSession sets valid on successful refresh`() = runTest(testDispatcher) {
        every { repository.hasSession() } returns true
        coEvery { repository.refreshSession() } returns Result.success(mockUser)

        viewModel.checkSession()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.sessionChecked)
        assertTrue(state.isSessionValid)
        assertEquals(mockUser, state.user)
    }

    @Test
    fun `checkSession logs out on refresh failure`() = runTest(testDispatcher) {
        every { repository.hasSession() } returns true
        coEvery { repository.refreshSession() } returns Result.failure(Exception("Sessão expirada"))
        coEvery { repository.logout() } just runs

        val events = mutableListOf<UiEvent>()
        val collectorJob = launch {
            viewModel.uiEvent.collect { events.add(it) }
        }

        viewModel.checkSession()
        advanceUntilIdle()
        collectorJob.cancel()

        coVerify { repository.logout() }
        coVerify { repository.refreshSession() }

        val state = viewModel.uiState.value
        assertTrue(state.sessionChecked)
        assertFalse(state.isSessionValid)
        assertTrue(events.isNotEmpty())
        val errorDialog = events.first() as UiEvent.ErrorDialog
        assertEquals("Sessão expirada", errorDialog.message)
    }

    @Test
    fun `logout resets state to defaults`() = runTest(testDispatcher) {
        coEvery { repository.logout() } just runs

        viewModel.logout()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
        assertNull(state.user)
        assertFalse(state.sessionChecked)
        assertFalse(state.isSessionValid)

        coVerify { repository.logout() }
    }

    @Test
    fun `resetState returns to defaults`() {
        viewModel.resetState()

        assertEquals(AuthUiState(), viewModel.uiState.value)
    }
}
