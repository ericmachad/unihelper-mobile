package br.edu.utfpr.unihelper.auth.data.repository

import br.edu.utfpr.unihelper.auth.data.remote.AlterarSenhaRequest
import br.edu.utfpr.unihelper.auth.data.remote.AtualizarPerfilRequest
import br.edu.utfpr.unihelper.auth.data.remote.AuthApi
import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.auth.data.remote.LoginRequest
import br.edu.utfpr.unihelper.auth.data.remote.RefreshRequest
import br.edu.utfpr.unihelper.auth.data.remote.RegisterRequest
import br.edu.utfpr.unihelper.core.local.TokenStorage
import br.edu.utfpr.unihelper.core.network.safeApiCall

class   AuthRepository(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) {
    suspend fun login(email: String, senha: String): Result<AuthResponse> = safeApiCall {
        val response = authApi.login(LoginRequest(email, senha))
        persistAuth(response)
        response
    }

    suspend fun register(
        nomeCompleto: String,
        apelido: String?,
        email: String,
        senha: String,
        curso: String?
    ): Result<AuthResponse> = safeApiCall {
        val response = authApi.register(
            RegisterRequest(nomeCompleto, apelido, email, senha, curso)
        )
        persistAuth(response)
        response
    }

    suspend fun refreshSession(): Result<AuthResponse> {
        val refreshToken = tokenStorage.getRefreshToken() ?: return Result.failure(
            Exception("Sessão não encontrada")
        )
        return safeApiCall {
            val response = authApi.refresh(RefreshRequest(refreshToken))
            persistAuth(response)
            response
        }
    }

    fun hasSession(): Boolean = tokenStorage.hasSession()

    fun getCachedUser(): AuthResponse? {
        val id = tokenStorage.getIdUsuario() ?: return null
        return AuthResponse(
            token = tokenStorage.getToken() ?: return null,
            refreshToken = tokenStorage.getRefreshToken() ?: return null,
            idUsuario = id,
            nomeCompleto = tokenStorage.getNomeCompleto() ?: return null,
            apelido = tokenStorage.getApelido(),
            email = tokenStorage.getEmail() ?: return null,
            curso = tokenStorage.getCurso()
        )
    }

    fun logout() {
        tokenStorage.clearAll()
    }

    suspend fun logoutComApi(): Result<Unit> = safeApiCall {
        authApi.logout()
        tokenStorage.clearAll()
    }

    suspend fun getMe(): Result<AuthResponse> = safeApiCall {
        val response = authApi.me()
        persistAuth(response)
        response
    }

    suspend fun atualizarPerfil(
        nomeCompleto: String?,
        apelido: String?,
        curso: String?
    ): Result<AuthResponse> = safeApiCall {
        val response = authApi.atualizarPerfil(
            AtualizarPerfilRequest(nomeCompleto, apelido, curso)
        )
        persistAuth(response)
        response
    }

    suspend fun alterarSenha(
        senhaAtual: String,
        novaSenha: String
    ): Result<Unit> = safeApiCall {
        authApi.alterarSenha(AlterarSenhaRequest(senhaAtual, novaSenha))
    }

    private fun persistAuth(response: AuthResponse) {
        tokenStorage.saveToken(response.token)
        tokenStorage.saveRefreshToken(response.refreshToken)
        tokenStorage.saveIdUsuario(response.idUsuario)
        tokenStorage.saveNomeCompleto(response.nomeCompleto)
        tokenStorage.saveApelido(response.apelido)
        tokenStorage.saveEmail(response.email)
        tokenStorage.saveCurso(response.curso)
    }
}
