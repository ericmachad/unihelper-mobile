package br.edu.utfpr.unihelper.auth.data.repository

import br.edu.utfpr.unihelper.auth.data.remote.AuthApi
import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.auth.data.remote.RefreshRequest
import br.edu.utfpr.unihelper.auth.data.remote.RegisterResponse
import br.edu.utfpr.unihelper.core.local.SessionManager
import br.edu.utfpr.unihelper.dispositivo.data.repository.DispositivoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthRepositoryTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var authApi: AuthApi

    @MockK
    private lateinit var dispositivoRepository: DispositivoRepository

    @MockK
    private lateinit var sessionManager: SessionManager

    private lateinit var repository: AuthRepository

    private val mockResponse = AuthResponse(
        token = "jwt-test",
        refreshToken = "rt-test",
        idUsuario = "550e8400-e29b-41d4-a716-446655440000",
        nomeCompleto = "João Silva",
        apelido = "joao",
        email = "joao@utfpr.edu.br",
        curso = "Ciência da Computação"
    )

    @org.junit.Before
    fun setup() {
        repository = AuthRepository(authApi, sessionManager, dispositivoRepository)
        every { sessionManager.persistAuth(any()) } just runs
        every { sessionManager.clearPendingConfirmation() } just runs
        every { sessionManager.getFcmToken() } returns null
        coEvery { dispositivoRepository.removerToken() } returns Result.success(Unit)
    }

    @Test
    fun `login calls API and persists all auth data`() = runTest {
        coEvery { authApi.login(any()) } returns mockResponse

        val result = repository.login("joao@utfpr.edu.br", "123456")

        assertTrue(result is LoginResult.Success)
        assertEquals(mockResponse, (result as LoginResult.Success).auth)
        coVerify { authApi.login(any()) }
        verify { sessionManager.persistAuth(mockResponse) }
    }

    @Test
    fun `login returns error on API exception`() = runTest {
        coEvery { authApi.login(any()) } throws Exception("Erro interno do servidor")

        val result = repository.login("joao@utfpr.edu.br", "123456")

        assertTrue(result is LoginResult.Error)
        assertEquals("Erro interno do servidor", (result as LoginResult.Error).exception.message)
    }

    @Test
    fun `register calls API with correct parameters`() = runTest {
        val registerResponse = RegisterResponse(
            mensagem = "Confirme seu email",
            email = "joao@utfpr.edu.br"
        )
        coEvery { authApi.register(any()) } returns registerResponse

        val result = repository.register(
            nomeCompleto = "João Silva",
            apelido = "joao",
            email = "joao@utfpr.edu.br",
            senha = "123456",
            curso = "Ciência da Computação"
        )

        assertTrue(result.isSuccess)
        assertEquals(registerResponse, result.getOrNull())
        coVerify {
            authApi.register(withArg {
                assertEquals("João Silva", it.nomeCompleto)
                assertEquals("joao", it.apelido)
                assertEquals("joao@utfpr.edu.br", it.email)
                assertEquals("123456", it.senha)
                assertEquals("Ciência da Computação", it.curso)
            })
        }
    }

    @Test
    fun `refreshSession calls refresh with stored token`() = runTest {
        every { sessionManager.getCachedUser() } returns mockResponse
        coEvery { authApi.refresh(any()) } returns mockResponse

        val result = repository.refreshSession()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
        coVerify { authApi.refresh(RefreshRequest("rt-test")) }
        verify { sessionManager.persistAuth(mockResponse) }
    }

    @Test
    fun `refreshSession fails when no cached user`() = runTest {
        every { sessionManager.getCachedUser() } returns null

        val result = repository.refreshSession()

        assertTrue(result.isFailure)
        assertEquals("Sessão não encontrada", result.exceptionOrNull()?.message)
    }

    @Test
    fun `hasSession delegates to sessionManager`() {
        every { sessionManager.hasSession() } returns true
        assertTrue(repository.hasSession())

        every { sessionManager.hasSession() } returns false
        assertFalse(repository.hasSession())
    }

    @Test
    fun `getCachedUser returns user when all data present`() {
        every { sessionManager.getCachedUser() } returns mockResponse

        val user = repository.getCachedUser()

        assertNotNull(user)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", user?.idUsuario)
        assertEquals("João Silva", user?.nomeCompleto)
        assertEquals("joao", user?.apelido)
        assertEquals("Ciência da Computação", user?.curso)
    }

    @Test
    fun `getCachedUser returns null when no session`() {
        every { sessionManager.getCachedUser() } returns null

        val user = repository.getCachedUser()

        assertNull(user)
    }

    @Test
    fun `logout clears session and removes FCM token`() = runTest {
        coEvery { sessionManager.clearSession() } just runs

        repository.logout()

        coVerify { dispositivoRepository.removerToken() }
        coVerify { sessionManager.clearSession() }
    }
}