package br.edu.utfpr.unihelper.auth.data.repository

import br.edu.utfpr.unihelper.auth.data.remote.AlterarSenhaRequest
import br.edu.utfpr.unihelper.auth.data.remote.AtualizarPerfilRequest
import br.edu.utfpr.unihelper.auth.data.remote.AuthApi
import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.auth.data.remote.ConfirmEmailRequest
import br.edu.utfpr.unihelper.auth.data.remote.ForgotPasswordRequest
import br.edu.utfpr.unihelper.auth.data.remote.LoginRequest
import br.edu.utfpr.unihelper.auth.data.remote.RefreshRequest
import br.edu.utfpr.unihelper.auth.data.remote.RegisterRequest
import br.edu.utfpr.unihelper.auth.data.remote.RegisterResponse
import br.edu.utfpr.unihelper.auth.data.remote.ResendConfirmationRequest
import br.edu.utfpr.unihelper.auth.data.remote.ResetPasswordRequest
import br.edu.utfpr.unihelper.core.local.SessionManager
import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.dispositivo.data.repository.DispositivoRepository
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit

sealed class LoginResult {
    data class Success(val auth: AuthResponse) : LoginResult()
    data object EmailNotConfirmed : LoginResult()
    data class Error(val exception: Throwable) : LoginResult()
}

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager,
    private val dispositivoRepository: DispositivoRepository
) {
    suspend fun login(email: String, senha: String): LoginResult {
        val result = safeApiCall {
            authApi.login(LoginRequest(email, senha))
        }
        return result.fold(
            onSuccess = { auth ->
                sessionManager.persistAuth(auth)
                sessionManager.clearPendingConfirmation()
                enviarFcmTokenSeExistir()
                LoginResult.Success(auth)
            },
            onFailure = { throwable ->
                val apiEx = throwable as? ApiException
                if (apiEx != null && apiEx.status == 403) {
                    sessionManager.persistPendingConfirmation(email)
                    LoginResult.EmailNotConfirmed
                } else {
                    LoginResult.Error(throwable)
                }
            }
        )
    }

    suspend fun register(
        nomeCompleto: String,
        apelido: String?,
        email: String,
        senha: String,
        curso: String?
    ): Result<RegisterResponse> = safeApiCall {
        authApi.register(RegisterRequest(nomeCompleto, apelido, email, senha, curso))
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
            enviarFcmTokenSeExistir()
            response
        }
    }

    fun hasSession(): Boolean = sessionManager.hasSession()

    fun getCachedUser(): AuthResponse? = sessionManager.getCachedUser()

    fun hasPendingConfirmation(): Boolean = sessionManager.hasPendingConfirmation()

    fun getPendingConfirmationEmail(): String? = sessionManager.getPendingConfirmationEmail()

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

    suspend fun confirmEmail(email: String, codigo: String): Result<AuthResponse> = safeApiCall {
        val response = authApi.confirmEmail(ConfirmEmailRequest(email, codigo))
        sessionManager.persistAuth(response)
        sessionManager.clearPendingConfirmation()
        enviarFcmTokenSeExistir()
        response
    }

    suspend fun resendConfirmation(email: String): Result<RegisterResponse> = safeApiCall {
        authApi.resendConfirmation(ResendConfirmationRequest(email))
    }

    suspend fun forgotPassword(email: String): Result<RegisterResponse> = safeApiCall {
        authApi.forgotPassword(ForgotPasswordRequest(email))
    }

    suspend fun resetPassword(token: String, novaSenha: String): Result<AuthResponse> = safeApiCall {
        val response = authApi.resetPassword(ResetPasswordRequest(token, novaSenha))
        sessionManager.persistAuth(response)
        enviarFcmTokenSeExistir()
        response
    }

    suspend fun enviarFcmTokenSeExistir() {
        val tokenSalvo = sessionManager.getFcmToken()
        if (tokenSalvo != null) {
            runCatching { dispositivoRepository.registrarToken(tokenSalvo) }
            return
        }
        try {
            val token = com.google.android.gms.tasks.Tasks.await(
                FirebaseMessaging.getInstance().token,
                30, TimeUnit.SECONDS
            )
            sessionManager.saveFcmToken(token)
            dispositivoRepository.registrarToken(token)
        } catch (_: Exception) {
        }
    }
}