package br.edu.utfpr.unihelper.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.auth.data.repository.AuthRepository
import br.edu.utfpr.unihelper.core.sync.AuthEvent
import br.edu.utfpr.unihelper.core.sync.AuthEventBus
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

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val user: AuthResponse? = null,
    val error: String? = null,
    val success: Boolean = false
)

data class ChangePasswordUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
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

    fun carregarPerfil() {
        viewModelScope.launch {
            _editProfileState.value = EditProfileUiState(isLoading = true)
            val result = authRepository.getMe()
            _editProfileState.value = if (result.isSuccess) {
                val user = result.getOrNull()
                _uiState.value = _uiState.value.copy(user = user)
                EditProfileUiState(user = user, isLoading = false)
            } else {
                EditProfileUiState(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Erro ao carregar perfil"
                )
            }
        }
    }

    fun atualizarPerfil(nomeCompleto: String?, apelido: String?, curso: String?) {
        viewModelScope.launch {
            _editProfileState.value = EditProfileUiState(isSaving = true)
            val result = authRepository.atualizarPerfil(nomeCompleto, apelido, curso)
            _editProfileState.value = if (result.isSuccess) {
                val user = result.getOrNull()
                _uiState.value = _uiState.value.copy(user = user)
                EditProfileUiState(user = user, success = true)
            } else {
                EditProfileUiState(error = result.exceptionOrNull()?.message ?: "Erro ao salvar")
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
                ChangePasswordUiState(error = result.exceptionOrNull()?.message ?: "Erro ao alterar senha")
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
