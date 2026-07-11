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

data class ConfirmEmailUiState(
    val email: String = "",
    val codigo: String = "",
    val isLoading: Boolean = false,
    val reenviado: Boolean = false,
    val loggedIn: Boolean = false,
    val error: String? = null
)

class ConfirmEmailViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmEmailUiState())
    val uiState: StateFlow<ConfirmEmailUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun init(email: String) {
        _uiState.value = ConfirmEmailUiState(email = email)
    }

    fun onCodigoChanged(codigo: String) {
        if (codigo.length <= 6 && codigo.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(codigo = codigo, error = null)
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.codigo.length != 6) {
            _uiState.value = state.copy(error = "Digite o código de 6 dígitos")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            authRepository.confirmEmail(state.email, state.codigo)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, loggedIn = true)
                }
                .onFailure {
                    _uiEvent.tryEmit(it.toErrorDialog())
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun resendConfirmation(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.resendConfirmation(email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, reenviado = true)
                }
                .onFailure {
                    _uiEvent.tryEmit(it.toErrorDialog())
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }
}
