package br.edu.utfpr.unihelper.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.auth.data.repository.AuthRepository
import br.edu.utfpr.unihelper.auth.data.repository.LoginResult
import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.network.toErrorDialog
import br.edu.utfpr.unihelper.core.sync.AuthEvent
import br.edu.utfpr.unihelper.core.sync.AuthEventBus
import br.edu.utfpr.unihelper.core.ui.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val registeredEmail: String? = null,
    val pendingConfirmationEmail: String? = null,
    val error: String? = null,
    val user: AuthResponse? = null,
    val sessionChecked: Boolean = false,
    val isSessionValid: Boolean = false
)

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val user: AuthResponse? = null,
    val success: Boolean = false
)

data class ChangePasswordUiState(
    val isSaving: Boolean = false,
    val success: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _editProfileState = MutableStateFlow(EditProfileUiState())
    val editProfileState: StateFlow<EditProfileUiState> = _editProfileState.asStateFlow()

    private val _changePasswordState = MutableStateFlow(ChangePasswordUiState())
    val changePasswordState: StateFlow<ChangePasswordUiState> = _changePasswordState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            authEventBus.events.collect { event ->
                when (event) {
                    is AuthEvent.LoggedIn -> {
                        _uiState.value = AuthUiState(
                            sessionChecked = true,
                            isSessionValid = true,
                            user = event.user
                        )
                    }
                    is AuthEvent.LoggedOut -> {
                        _uiState.value = AuthUiState()
                    }
                }
            }
        }
    }

    fun login(email: String, senha: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.login(email, senha)) {
                is LoginResult.Success -> {
                    _uiState.value = AuthUiState(isSuccess = true, user = result.auth)
                }
                is LoginResult.EmailNotConfirmed -> {
                    _uiState.value = AuthUiState(
                        pendingConfirmationEmail = email
                    )
                }
                is LoginResult.Error -> {
                    _uiEvent.tryEmit(result.exception.toErrorDialog())
                    _uiState.value = AuthUiState()
                }
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
            if (result.isSuccess) {
                val registeredEmail = result.getOrNull()?.email ?: email
                _uiState.value = AuthUiState(registeredEmail = registeredEmail)
            } else {
                result.exceptionOrNull()?.let { _uiEvent.tryEmit(it.toErrorDialog()) }
                _uiState.value = AuthUiState()
            }
        }
    }

    fun checkSession() {
        if (!authRepository.hasSession()) {
            if (authRepository.hasPendingConfirmation()) {
                _uiState.value = AuthUiState(
                    sessionChecked = true,
                    isSessionValid = false,
                    pendingConfirmationEmail = authRepository.getPendingConfirmationEmail()
                )
                return
            }
            _uiState.value = AuthUiState(sessionChecked = true, isSessionValid = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.refreshSession()
            if (result.isSuccess) {
                _uiState.value = AuthUiState(
                    sessionChecked = true,
                    isSessionValid = true,
                    user = result.getOrNull()
                )
            } else {
                val exception = result.exceptionOrNull()
                val isConnectionError = exception is ApiException && exception.status == 0

                if (isConnectionError && authRepository.hasSession()) {
                    _uiState.value = AuthUiState(
                        sessionChecked = true,
                        isSessionValid = true,
                        user = authRepository.getCachedUser()
                    )
                } else {
                    result.exceptionOrNull()?.let { _uiEvent.tryEmit(it.toErrorDialog()) }
                    authRepository.logout()
                    _uiState.value = AuthUiState(
                        sessionChecked = true,
                        isSessionValid = false
                    )
                }
            }
        }
    }

    fun carregarPerfil() {
        viewModelScope.launch {
            _editProfileState.value = EditProfileUiState(isLoading = true)
            val result = authRepository.getMe()
            _editProfileState.value = if (result.isSuccess) {
                val user = result.getOrNull()
                _uiState.value = _uiState.value.copy(user = user)
                EditProfileUiState(user = user, isLoading = false)
            } else {
                result.exceptionOrNull()?.let { _uiEvent.tryEmit(it.toErrorDialog()) }
                EditProfileUiState(isLoading = false)
            }
        }
    }

    fun atualizarPerfil(nomeCompleto: String?, apelido: String?, curso: String?) {
        viewModelScope.launch {
            _editProfileState.value = EditProfileUiState(isSaving = true)
            val result = authRepository.atualizarPerfil(nomeCompleto, apelido, curso)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _uiState.value = _uiState.value.copy(user = user)
                _editProfileState.value = EditProfileUiState(user = user, success = true)
            } else {
                _editProfileState.value = EditProfileUiState()
                result.exceptionOrNull()?.let { _uiEvent.tryEmit(it.toErrorDialog()) }
            }
        }
    }

    fun alterarSenha(senhaAtual: String, novaSenha: String) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordUiState(isSaving = true)
            val result = authRepository.alterarSenha(senhaAtual, novaSenha)
            _changePasswordState.value = if (result.isSuccess) {
                ChangePasswordUiState(success = true)
            } else {
                result.exceptionOrNull()?.let { _uiEvent.tryEmit(it.toErrorDialog()) }
                ChangePasswordUiState()
            }
        }
    }

    fun resetEditProfileState() {
        _editProfileState.value = EditProfileUiState()
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = ChangePasswordUiState()
    }

    fun enviarFcmToken() {
        viewModelScope.launch {
            authRepository.enviarFcmTokenSeExistir()
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }

    fun logout(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
            onComplete?.invoke()
        }
    }

    suspend fun logoutComApi() {
        authRepository.logoutComApi()
        _uiState.value = AuthUiState()
    }
}
