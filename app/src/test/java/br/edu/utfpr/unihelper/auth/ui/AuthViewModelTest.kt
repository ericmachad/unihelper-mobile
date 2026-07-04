package br.edu.utfpr.unihelper.auth.ui

import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.auth.data.repository.AuthRepository
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private lateinit var viewModel: AuthViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(repository)
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
        coEvery { repository.login(any(), any()) } returns Result.success(mockUser)

        viewModel.login("joao@utfpr.edu.br", "123456")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertEquals(mockUser, state.user)
    }

    @Test
    fun `login emits error on failure`() = runTest(testDispatcher) {
        coEvery { repository.login(any(), any()) } returns Result.failure(Exception("Email ou senha inválidos"))

        viewModel.login("joao@utfpr.edu.br", "senha_errada")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertEquals("Email ou senha inválidos", state.error)
        assertNull(state.user)
    }

    @Test
    fun `register emits success`() = runTest(testDispatcher) {
        coEvery { repository.register(any(), any(), any(), any(), any()) } returns Result.success(mockUser)

        viewModel.register("João", null, "joao@utfpr.edu.br", "123456", null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertEquals(mockUser, state.user)
    }

    @Test
    fun `register emits error on failure`() = runTest(testDispatcher) {
        coEvery { repository.register(any(), any(), any(), any(), any()) } returns Result.failure(Exception("Email já cadastrado"))

        viewModel.register("João", null, "existente@email.com", "123456", null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Email já cadastrado", state.error)
    }

    @Test
    fun `checkSession sets invalid when no session`() = runTest(testDispatcher) {
        every { repository.hasSession() } returns false

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
        every { repository.logout() } just runs

        viewModel.checkSession()
        advanceUntilIdle()

        verify { repository.logout() }
        coVerify { repository.refreshSession() }

        val state = viewModel.uiState.value
        assertTrue(state.sessionChecked)
        assertFalse(state.isSessionValid)
        assertEquals("Sessão expirada", state.error)
    }

    @Test
    fun `logout resets state to defaults`() = runTest(testDispatcher) {
        every { repository.logout() } just runs

        viewModel.logout()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
        assertNull(state.user)
        assertFalse(state.sessionChecked)
        assertFalse(state.isSessionValid)

        verify { repository.logout() }
    }

    @Test
    fun `resetState returns to defaults`() = runTest(testDispatcher) {
        viewModel.resetState()

        assertEquals(AuthUiState(), viewModel.uiState.value)
    }
}
