package br.edu.utfpr.unihelper.disciplina.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.core.sync.AuthEvent
import br.edu.utfpr.unihelper.core.sync.AuthEventBus
import br.edu.utfpr.unihelper.disciplina.data.remote.CriarDisciplinaRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaResponse
import br.edu.utfpr.unihelper.disciplina.data.repository.DisciplinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DisciplinaUiState(
    val disciplinas: List<DisciplinaResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val operacaoSucesso: Boolean = false,
    val faltasAtualizando: Set<String> = emptySet()
)

data class FormUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val sucesso: Boolean = false
)

class DisciplinaViewModel(
    private val repository: DisciplinaRepository,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(DisciplinaUiState())
    val uiState: StateFlow<DisciplinaUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(FormUiState())
    val formState: StateFlow<FormUiState> = _formState.asStateFlow()

    private val _disciplinaEditando = MutableStateFlow<DisciplinaResponse?>(null)
    val disciplinaEditando: StateFlow<DisciplinaResponse?> = _disciplinaEditando.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteUiState>(DeleteUiState())
    val deleteState: StateFlow<DeleteUiState> = _deleteState.asStateFlow()

    init {
        viewModelScope.launch {
            authEventBus.events.collect { event ->
                when (event) {
                    is AuthEvent.LoggedOut -> resetState()
                    is AuthEvent.LoggedIn -> {
                        resetState()
                        listar()
                    }
                }
            }
        }
        listar()
    }

    fun listar(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                error = null
            )
            repository.listar()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        disciplinas = it, isLoading = false, isRefreshing = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message, isLoading = false, isRefreshing = false
                    )
                }
        }
    }

    fun carregar(id: String) {
        viewModelScope.launch {
            _formState.value = FormUiState(isLoading = true)
            repository.buscarPorId(id)
                .onSuccess {
                    _disciplinaEditando.value = it
                    _formState.value = FormUiState()
                }
                .onFailure { _formState.value = FormUiState(error = it.message) }
        }
    }

    fun criar(request: CriarDisciplinaRequest) {
        viewModelScope.launch {
            _formState.value = FormUiState(isLoading = true)
            repository.criar(request)
                .onSuccess {
                    _formState.value = FormUiState(sucesso = true)
                    listar()
                }
                .onFailure { _formState.value = FormUiState(error = it.message) }
        }
    }

    fun atualizar(id: String, request: CriarDisciplinaRequest) {
        viewModelScope.launch {
            _formState.value = FormUiState(isLoading = true)
            repository.atualizar(id, request)
                .onSuccess {
                    _formState.value = FormUiState(sucesso = true)
                    listar()
                }
                .onFailure { _formState.value = FormUiState(error = it.message) }
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch {
            _deleteState.value = DeleteUiState(isLoading = true)
            repository.excluir(id)
                .onSuccess {
                    _deleteState.value = DeleteUiState(sucesso = true)
                    listar()
                }
                .onFailure { _deleteState.value = DeleteUiState(error = it.message) }
        }
    }

    fun alterarFaltas(id: String, operacao: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                faltasAtualizando = _uiState.value.faltasAtualizando + id
            )
            repository.alterarFaltas(id, operacao)
                .onSuccess {
                    listar()
                    _uiState.value = _uiState.value.copy(
                        faltasAtualizando = _uiState.value.faltasAtualizando - id
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message,
                        faltasAtualizando = _uiState.value.faltasAtualizando - id
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = DisciplinaUiState(isLoading = true)
    }

    fun limparSucesso() {
        _uiState.value = _uiState.value.copy(operacaoSucesso = false)
    }

    fun limparDeleteState() {
        _deleteState.value = DeleteUiState()
    }

    fun limparEdicao() {
        _disciplinaEditando.value = null
    }
}

data class DeleteUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val sucesso: Boolean = false
)
