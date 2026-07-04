package br.edu.utfpr.unihelper.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.auth.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val user: AuthResponse? = null,
    val sessionChecked: Boolean = false,
    val isSessionValid: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, senha: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.login(email, senha)
            _uiState.value = if (result.isSuccess) {
                AuthUiState(isSuccess = true, user = result.getOrNull())
            } else {
                AuthUiState(error = result.exceptionOrNull()?.message ?: "Erro ao fazer login")
            }
        }
    }

    fun register(
        nomeCompleto: String,
        apelido: String?,
        email: String,
        senha: String,
        curso: String?
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.register(nomeCompleto, apelido, email, senha, curso)
            _uiState.value = if (result.isSuccess) {
                AuthUiState(isSuccess = true, user = result.getOrNull())
            } else {
                AuthUiState(error = result.exceptionOrNull()?.message ?: "Erro ao cadastrar")
            }
        }
    }

    fun checkSession() {
        if (!authRepository.hasSession()) {
            _uiState.value = AuthUiState(sessionChecked = true, isSessionValid = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.refreshSession()
            _uiState.value = if (result.isSuccess) {
                AuthUiState(
                    sessionChecked = true,
                    isSessionValid = true,
                    user = result.getOrNull()
                )
            } else {
                authRepository.logout()
                AuthUiState(
                    sessionChecked = true,
                    isSessionValid = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }
}
