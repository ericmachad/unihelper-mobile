package br.edu.utfpr.unihelper.auth.data.repository

import br.edu.utfpr.unihelper.auth.data.remote.AlterarSenhaRequest
import br.edu.utfpr.unihelper.auth.data.remote.AtualizarPerfilRequest
import br.edu.utfpr.unihelper.auth.data.remote.AuthApi
import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.auth.data.remote.LoginRequest
import br.edu.utfpr.unihelper.auth.data.remote.RefreshRequest
import br.edu.utfpr.unihelper.auth.data.remote.RegisterRequest
import br.edu.utfpr.unihelper.core.local.SessionManager
import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.dispositivo.data.repository.DispositivoRepository

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager,
    private val dispositivoRepository: DispositivoRepository
) {
    suspend fun login(email: String, senha: String): Result<AuthResponse> = safeApiCall {
        val response = authApi.login(LoginRequest(email, senha))
        sessionManager.persistAuth(response)
        enviarFcmTokenSeExistir()
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
        sessionManager.persistAuth(response)
        enviarFcmTokenSeExistir()
        response
    }

    suspend fun refreshSession(): Result<AuthResponse> {
        val refreshToken = sessionManager.run {
            getCachedUser()?.refreshToken ?: return Result.failure(
                Exception("Sessão não encontrada")
            )
        }
        return safeApiCall {
            val response = authApi.refresh(RefreshRequest(refreshToken))
            sessionManager.persistAuth(response)
            response
        }
    }

    fun hasSession(): Boolean = sessionManager.hasSession()

    fun getCachedUser(): AuthResponse? = sessionManager.getCachedUser()

    suspend fun logout() {
        runCatching { dispositivoRepository.removerToken() }
        sessionManager.clearSession()
    }

    suspend fun logoutComApi(): Result<Unit> = safeApiCall {
        runCatching { dispositivoRepository.removerToken() }
        authApi.logout()
        sessionManager.clearSession()
    }

    suspend fun getMe(): Result<AuthResponse> = safeApiCall {
        val response = authApi.me()
        sessionManager.persistAuth(response)
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
        sessionManager.persistAuth(response)
        response
    }

    suspend fun alterarSenha(
        senhaAtual: String,
        novaSenha: String
    ): Result<Unit> = safeApiCall {
        authApi.alterarSenha(AlterarSenhaRequest(senhaAtual, novaSenha))
    }

    suspend fun enviarFcmTokenSeExistir() {
        val fcmToken = sessionManager.getFcmToken() ?: return
        runCatching { dispositivoRepository.registrarToken(fcmToken) }
    }
}