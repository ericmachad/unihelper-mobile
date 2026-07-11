package br.edu.utfpr.unihelper.nota.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.network.toErrorDialog
import br.edu.utfpr.unihelper.core.ui.UiEvent
import br.edu.utfpr.unihelper.nota.data.remote.NotaRequest
import br.edu.utfpr.unihelper.nota.data.remote.NotaResponse
import br.edu.utfpr.unihelper.nota.data.repository.NotaRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotaUiState(
    val notas: List<NotaResponse> = emptyList(),
    val isLoading: Boolean = false,
    val termoBusca: String = ""
)

class NotaViewModel(
    private val repository: NotaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotaUiState())
    val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var flowJob: kotlinx.coroutines.Job? = null
    private var currentDisciplinaId: String? = null

    fun carregarNotas(disciplinaId: String) {
        currentDisciplinaId = disciplinaId
        flowJob?.cancel()
        flowJob = viewModelScope.launch {
            repository.listarFlow(disciplinaId).collect { notas ->
                _uiState.update { it.copy(notas = notas, isLoading = false) }
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.listar(disciplinaId)
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.tryEmit(UiEvent.Snackbar("Não foi possível carregar anotações. Dados offline exibidos."))
                }
        }
    }

    fun criar(disciplinaId: String, titulo: String, conteudo: String?) {
        viewModelScope.launch {
            repository.criar(disciplinaId, NotaRequest(titulo, conteudo))
                .onSuccess {
                    _uiEvent.tryEmit(UiEvent.SuccessDialog("Anotação criada"))
                }
                .onFailure { error ->
                    if (error is ApiException && error.status == 0) {
                        _uiEvent.tryEmit(UiEvent.SuccessDialog("Anotação criada"))
                    } else {
                        _uiEvent.tryEmit(error.toErrorDialog())
                    }
                }
        }
    }

    fun atualizar(disciplinaId: String, id: String, titulo: String, conteudo: String?) {
        viewModelScope.launch {
            repository.atualizar(disciplinaId, id, NotaRequest(titulo, conteudo))
                .onSuccess {
                    _uiEvent.tryEmit(UiEvent.SuccessDialog("Anotação atualizada"))
                }
                .onFailure { error ->
                    if (error is ApiException && error.status == 0) {
                        _uiEvent.tryEmit(UiEvent.SuccessDialog("Anotação atualizada"))
                    } else {
                        _uiEvent.tryEmit(error.toErrorDialog())
                    }
                }
        }
    }

    fun excluir(disciplinaId: String, id: String) {
        viewModelScope.launch {
            repository.excluir(disciplinaId, id)
                .onSuccess {
                    _uiEvent.tryEmit(UiEvent.SuccessDialog("Anotação excluída"))
                }
                .onFailure { error ->
                    if (error is ApiException && error.status == 0) {
                        _uiEvent.tryEmit(UiEvent.SuccessDialog("Anotação excluída"))
                    } else {
                        _uiEvent.tryEmit(error.toErrorDialog())
                    }
                }
        }
    }

    fun buscar(disciplinaId: String, termo: String) {
        _uiState.update { it.copy(termoBusca = termo) }
        if (termo.isBlank()) {
            carregarNotas(disciplinaId)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.buscarPorTitulo(disciplinaId, termo)
                .onSuccess { notasResponse ->
                    flowJob?.cancel()
                    _uiState.update { it.copy(notas = notasResponse, isLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.tryEmit(it.toErrorDialog())
                }
        }
    }
}