package br.edu.utfpr.unihelper.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.auth.data.repository.AuthRepository
import br.edu.utfpr.unihelper.core.network.toErrorDialog
import br.edu.utfpr.unihelper.core.ui.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val token: String = "",
    val novaSenha: String = "",
    val confirmarSenha: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class ResetPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun init(token: String) {
        _uiState.value = _uiState.value.copy(token = token)
    }

    fun updateNovaSenha(senha: String) {
        _uiState.value = _uiState.value.copy(novaSenha = senha, error = null)
    }

    fun updateConfirmarSenha(senha: String) {
        _uiState.value = _uiState.value.copy(confirmarSenha = senha, error = null)
    }

    fun submit() {
        val state = _uiState.value
        when {
            state.novaSenha.isBlank() -> {
                _uiState.value = state.copy(error = "Digite a nova senha")
                return
            }
            state.novaSenha.length < 8 -> {
                _uiState.value = state.copy(error = "Mínimo 8 caracteres")
                return
            }
            state.novaSenha != state.confirmarSenha -> {
                _uiState.value = state.copy(error = "Senhas não conferem")
                return
            }
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            authRepository.resetPassword(state.token, state.novaSenha)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                }
                .onFailure {
                    _uiEvent.tryEmit(it.toErrorDialog())
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }
}
