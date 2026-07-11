package br.edu.utfpr.unihelper.core.local

import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.core.sync.AuthEvent
import br.edu.utfpr.unihelper.core.sync.AuthEventBus

class SessionManager(
    private val tokenStorage: TokenStorage,
    private val database: AppDatabase,
    private val mediaConfig: MediaConfig,
    private val authEventBus: AuthEventBus
) {
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

    fun persistAuth(response: AuthResponse) {
        tokenStorage.saveToken(response.token)
        tokenStorage.saveRefreshToken(response.refreshToken)
        tokenStorage.saveIdUsuario(response.idUsuario)
        tokenStorage.saveNomeCompleto(response.nomeCompleto)
        tokenStorage.saveApelido(response.apelido)
        tokenStorage.saveEmail(response.email)
        tokenStorage.saveCurso(response.curso)
        authEventBus.emit(AuthEvent.LoggedIn(response))
    }

    suspend fun clearSession() {
        tokenStorage.clearAll()
        database.limparTudo()
        mediaConfig.clear()
        authEventBus.emit(AuthEvent.LoggedOut)
    }

    fun getFcmToken(): String? = tokenStorage.getFcmToken()
}