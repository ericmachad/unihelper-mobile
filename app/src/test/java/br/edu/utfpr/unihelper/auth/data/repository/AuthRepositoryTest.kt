package br.edu.utfpr.unihelper.auth.data.repository

import br.edu.utfpr.unihelper.auth.data.remote.AuthApi
import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.auth.data.remote.RefreshRequest
import br.edu.utfpr.unihelper.core.local.TokenStorage
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
    private lateinit var tokenStorage: TokenStorage

    @MockK
    private lateinit var dispositivoRepository: DispositivoRepository

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
        repository = AuthRepository(authApi, tokenStorage, dispositivoRepository)
        every { tokenStorage.saveToken(any()) } just runs
        every { tokenStorage.saveRefreshToken(any()) } just runs
        every { tokenStorage.saveIdUsuario(any()) } just runs
        every { tokenStorage.saveNomeCompleto(any()) } just runs
        every { tokenStorage.saveApelido(any()) } just runs
        every { tokenStorage.saveEmail(any()) } just runs
        every { tokenStorage.saveCurso(any()) } just runs
        every { tokenStorage.getFcmToken() } returns null
    }

    @Test
    fun `login calls API and persists all auth data`() = runTest {
        coEvery { authApi.login(any()) } returns mockResponse

        val result = repository.login("joao@utfpr.edu.br", "123456")

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
        coVerify { authApi.login(any()) }
        verify { tokenStorage.saveToken("jwt-test") }
        verify { tokenStorage.saveRefreshToken("rt-test") }
        verify { tokenStorage.saveIdUsuario("550e8400-e29b-41d4-a716-446655440000") }
        verify { tokenStorage.saveNomeCompleto("João Silva") }
        verify { tokenStorage.saveApelido("joao") }
        verify { tokenStorage.saveEmail("joao@utfpr.edu.br") }
        verify { tokenStorage.saveCurso("Ciência da Computação") }
    }

    @Test
    fun `login returns failure on API exception`() = runTest {
        coEvery { authApi.login(any()) } throws Exception("Erro interno do servidor")

        val result = repository.login("joao@utfpr.edu.br", "123456")

        assertTrue(result.isFailure)
        assertEquals("Erro interno do servidor", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register calls API with correct parameters`() = runTest {
        coEvery { authApi.register(any()) } returns mockResponse

        val result = repository.register(
            nomeCompleto = "João Silva",
            apelido = "joao",
            email = "joao@utfpr.edu.br",
            senha = "123456",
            curso = "Ciência da Computação"
        )

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
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
    fun `register handles nullable optional fields`() = runTest {
        coEvery { authApi.register(any()) } returns mockResponse.copy(apelido = null, curso = null)

        val result = repository.register(
            nomeCompleto = "João",
            apelido = null,
            email = "joao@utfpr.edu.br",
            senha = "123456",
            curso = null
        )

        assertTrue(result.isSuccess)
        coVerify {
            authApi.register(withArg {
                assertNull(it.apelido)
                assertNull(it.curso)
            })
        }
        verify { tokenStorage.saveApelido(null) }
        verify { tokenStorage.saveCurso(null) }
    }

    @Test
    fun `refreshSession calls refresh with stored token`() = runTest {
        every { tokenStorage.getRefreshToken() } returns "rt-stored"
        coEvery { authApi.refresh(any()) } returns mockResponse

        val result = repository.refreshSession()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
        coVerify { authApi.refresh(RefreshRequest("rt-stored")) }
        verify { tokenStorage.saveToken("jwt-test") }
        verify { tokenStorage.saveRefreshToken("rt-test") }
    }

    @Test
    fun `refreshSession fails when no refreshToken stored`() = runTest {
        every { tokenStorage.getRefreshToken() } returns null

        val result = repository.refreshSession()

        assertTrue(result.isFailure)
        assertEquals("Sessão não encontrada", result.exceptionOrNull()?.message)
    }

    @Test
    fun `hasSession delegates to tokenStorage`() {
        every { tokenStorage.hasSession() } returns true
        assertTrue(repository.hasSession())

        every { tokenStorage.hasSession() } returns false
        assertFalse(repository.hasSession())
    }

    @Test
    fun `getCachedUser returns user when all data present`() {
        every { tokenStorage.getIdUsuario() } returns "uuid"
        every { tokenStorage.getToken() } returns "jwt"
        every { tokenStorage.getRefreshToken() } returns "rt"
        every { tokenStorage.getNomeCompleto() } returns "João"
        every { tokenStorage.getApelido() } returns "joao"
        every { tokenStorage.getEmail() } returns "joao@utfpr.edu.br"
        every { tokenStorage.getCurso() } returns "CC"

        val user = repository.getCachedUser()

        assertNotNull(user)
        assertEquals("uuid", user?.idUsuario)
        assertEquals("João", user?.nomeCompleto)
        assertEquals("joao", user?.apelido)
        assertEquals("CC", user?.curso)
    }

    @Test
    fun `getCachedUser returns null when no id stored`() {
        every { tokenStorage.getIdUsuario() } returns null

        val user = repository.getCachedUser()

        assertNull(user)
    }

    @Test
    fun `logout clears all storage`() {
        every { tokenStorage.clearAll() } just runs

        repository.logout()

        verify { tokenStorage.clearAll() }
    }
}
