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

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val sent: Boolean = false,
    val error: String? = null
)

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun submit() {
        val email = _uiState.value.email.trim().lowercase()
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Informe seu email")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.forgotPassword(email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, sent = true)
                }
                .onFailure {
                    _uiEvent.tryEmit(it.toErrorDialog())
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }
}
