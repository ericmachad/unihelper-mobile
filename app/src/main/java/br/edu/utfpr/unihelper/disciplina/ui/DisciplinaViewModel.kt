package br.edu.utfpr.unihelper.disciplina.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.network.toErrorDialog
import br.edu.utfpr.unihelper.core.sync.AuthEvent
import br.edu.utfpr.unihelper.core.sync.AuthEventBus
import br.edu.utfpr.unihelper.core.ui.UiEvent
import br.edu.utfpr.unihelper.disciplina.data.remote.CriarDisciplinaRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaResponse
import br.edu.utfpr.unihelper.disciplina.data.repository.DisciplinaRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DisciplinaUiState(
    val disciplinas: List<DisciplinaResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val faltasAtualizando: Set<String> = emptySet()
)

data class FormUiState(
    val isLoading: Boolean = false,
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

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var disciplinasFlowJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            authEventBus.events.collect { event ->
                when (event) {
                    is AuthEvent.LoggedOut -> resetState()
                    is AuthEvent.LoggedIn -> {
                        resetState()
                        listar()
                        iniciarDisciplinasFlow()
                    }
                }
            }
        }
    }

    private fun iniciarDisciplinasFlow() {
        disciplinasFlowJob?.cancel()
        disciplinasFlowJob = viewModelScope.launch {
            repository.listarDisciplinasFlow().collect { disciplinas ->
                _uiState.update { it.copy(
                    disciplinas = disciplinas, isLoading = false, isRefreshing = false
                ) }
            }
        }
    }

    fun listar(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh
            ) }
            repository.listar()
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
                    _uiEvent.tryEmit(UiEvent.Snackbar("Não foi possível atualizar. Dados offline exibidos."))
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
                .onFailure {
                    _formState.value = FormUiState()
                    _uiEvent.tryEmit(it.toErrorDialog())
                }
        }
    }

    fun criar(request: CriarDisciplinaRequest) {
        viewModelScope.launch {
            _formState.value = FormUiState(isLoading = true)
            repository.criar(request)
                .onSuccess {
                    _formState.value = FormUiState(sucesso = true)
                }
                .onFailure { error ->
                    if (error is ApiException && error.status == 0) {
                        _formState.value = FormUiState(sucesso = true)
                    } else {
                        _formState.value = FormUiState()
                        _uiEvent.tryEmit(error.toErrorDialog())
                    }
                }
        }
    }

    fun atualizar(id: String, request: CriarDisciplinaRequest) {
        viewModelScope.launch {
            _formState.value = FormUiState(isLoading = true)
            repository.atualizar(id, request)
                .onSuccess {
                    _formState.value = FormUiState(sucesso = true)
                }
                .onFailure { error ->
                    if (error is ApiException && error.status == 0) {
                        _formState.value = FormUiState(sucesso = true)
                    } else {
                        _formState.value = FormUiState()
                        _uiEvent.tryEmit(error.toErrorDialog())
                    }
                }
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch {
            _deleteState.value = DeleteUiState(isLoading = true)
            repository.excluir(id)
                .onSuccess {
                    _deleteState.value = DeleteUiState(sucesso = true)
                    _uiEvent.tryEmit(UiEvent.SuccessDialog("Disciplina excluída"))
                }
                .onFailure { error ->
                    _deleteState.value = DeleteUiState()
                    if (error is ApiException && error.status == 0) {
                        _uiEvent.tryEmit(UiEvent.SuccessDialog("Disciplina excluída"))
                    } else {
                        _uiEvent.tryEmit(error.toErrorDialog())
                    }
                }
        }
    }

    fun alterarFaltas(id: String, operacao: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(faltasAtualizando = it.faltasAtualizando + id)
            }
            repository.alterarFaltas(id, operacao)
                .onSuccess {
                    _uiState.update {
                        it.copy(faltasAtualizando = it.faltasAtualizando - id)
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(faltasAtualizando = it.faltasAtualizando - id)
                    }
                    _uiEvent.tryEmit(it.toErrorDialog())
                }
        }
    }

    fun resetState() {
        _uiState.value = DisciplinaUiState(isLoading = true)
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
    val sucesso: Boolean = false
)
