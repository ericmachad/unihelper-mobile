package br.edu.utfpr.unihelper.auth.data.remote

import br.edu.utfpr.unihelper.core.network.ApiErrorResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = true
    }

    @Test
    fun `login request serialization`() {
        val request = LoginRequest(email = "joao@utfpr.edu.br", senha = "123456")
        val encoded = json.encodeToString(LoginRequest.serializer(), request)
        val expected = """{"email":"joao@utfpr.edu.br","senha":"123456"}"""
        assertEquals(expected, encoded)
    }

    @Test
    fun `login request deserialization`() {
        val raw = """{"email":"joao@utfpr.edu.br","senha":"123456"}"""
        val decoded = json.decodeFromString(LoginRequest.serializer(), raw)
        assertEquals("joao@utfpr.edu.br", decoded.email)
        assertEquals("123456", decoded.senha)
    }

    @Test
    fun `register request with all fields`() {
        val request = RegisterRequest(
            nomeCompleto = "João Silva",
            apelido = "joao",
            email = "joao@utfpr.edu.br",
            senha = "123456",
            curso = "Ciência da Computação"
        )
        val encoded = json.encodeToString(RegisterRequest.serializer(), request)
        val expected = """{"nomeCompleto":"João Silva","apelido":"joao","email":"joao@utfpr.edu.br","senha":"123456","curso":"Ciência da Computação"}"""
        assertEquals(expected, encoded)
    }

    @Test
    fun `register request with null optionals omitted`() {
        val request = RegisterRequest(
            nomeCompleto = "João Silva",
            apelido = null,
            email = "joao@utfpr.edu.br",
            senha = "123456",
            curso = null
        )
        val encoded = json.encodeToString(RegisterRequest.serializer(), request)
        val expected = """{"nomeCompleto":"João Silva","email":"joao@utfpr.edu.br","senha":"123456"}"""
        assertEquals(expected, encoded)
    }

    @Test
    fun `auth response deserialization`() {
        val raw = """{
            "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvQHV0ZnByLmVkdS5iciIsImlhdCI6MTc0OTAyNjUwMCwiZXhwIjoxNzQ5MDMzNzAwfQ.test",
            "refreshToken": "rt-test",
            "idUsuario": "550e8400-e29b-41d4-a716-446655440000",
            "nomeCompleto": "João Silva",
            "apelido": "joao",
            "email": "joao@utfpr.edu.br",
            "curso": "Ciência da Computação"
        }"""
        val decoded = json.decodeFromString(AuthResponse.serializer(), raw)
        assertEquals("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvQHV0ZnByLmVkdS5iciIsImlhdCI6MTc0OTAyNjUwMCwiZXhwIjoxNzQ5MDMzNzAwfQ.test", decoded.token)
        assertEquals("rt-test", decoded.refreshToken)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", decoded.idUsuario)
        assertEquals("João Silva", decoded.nomeCompleto)
        assertEquals("joao", decoded.apelido)
        assertEquals("joao@utfpr.edu.br", decoded.email)
        assertEquals("Ciência da Computação", decoded.curso)
    }

    @Test
    fun `auth response with null optionals`() {
        val raw = """{
            "token": "jwt",
            "refreshToken": "rt",
            "idUsuario": "uuid",
            "nomeCompleto": "João",
            "apelido": null,
            "email": "joao@utfpr.edu.br",
            "curso": null
        }"""
        val decoded = json.decodeFromString(AuthResponse.serializer(), raw)
        assertEquals("jwt", decoded.token)
        assertEquals("rt", decoded.refreshToken)
        assertEquals("uuid", decoded.idUsuario)
        assertEquals("João", decoded.nomeCompleto)
        assertNull(decoded.apelido)
        assertEquals("joao@utfpr.edu.br", decoded.email)
        assertNull(decoded.curso)
    }

    @Test
    fun `refresh request serialization`() {
        val request = RefreshRequest(refreshToken = "rt-test")
        val encoded = json.encodeToString(RefreshRequest.serializer(), request)
        val expected = """{"refreshToken":"rt-test"}"""
        assertEquals(expected, encoded)
    }

    @Test
    fun `api error response deserialization`() {
        val raw = """{"status":400,"message":"Email já cadastrado"}"""
        val decoded = json.decodeFromString(ApiErrorResponse.serializer(), raw)
        assertEquals(400, decoded.status)
        assertEquals("Email já cadastrado", decoded.message)
    }

    @Test
    fun `api error deserialization handles unknown fields`() {
        val raw = """{"status":401,"message":"Email ou senha inválidos","timestamp":"2025-01-01T00:00:00Z","path":"/auth/login"}"""
        val decoded = json.decodeFromString(ApiErrorResponse.serializer(), raw)
        assertEquals(401, decoded.status)
        assertEquals("Email ou senha inválidos", decoded.message)
    }
}
