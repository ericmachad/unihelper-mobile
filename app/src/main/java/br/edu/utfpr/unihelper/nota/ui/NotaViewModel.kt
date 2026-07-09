package br.edu.utfpr.unihelper.nota.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.nota.data.remote.NotaRequest
import br.edu.utfpr.unihelper.nota.data.remote.NotaResponse
import br.edu.utfpr.unihelper.nota.data.repository.NotaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotaUiState(
    val notas: List<NotaResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val operacaoSucesso: Boolean = false,
    val termoBusca: String = ""
)

data class NotaFormState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val sucesso: Boolean = false
)

class NotaViewModel(
    private val repository: NotaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotaUiState())
    val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(NotaFormState())
    val formState: StateFlow<NotaFormState> = _formState.asStateFlow()

    fun carregarNotas(disciplinaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.listar(disciplinaId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        notas = it, isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message, isLoading = false
                    )
                }
        }
    }

    fun criar(disciplinaId: String, titulo: String, conteudo: String?) {
        viewModelScope.launch {
            _formState.value = NotaFormState(isLoading = true)
            repository.criar(disciplinaId, NotaRequest(titulo, conteudo))
                .onSuccess {
                    _formState.value = NotaFormState(sucesso = true)
                    carregarNotas(disciplinaId)
                }
                .onFailure { _formState.value = NotaFormState(error = it.message) }
        }
    }

    fun atualizar(disciplinaId: String, id: String, titulo: String, conteudo: String?) {
        viewModelScope.launch {
            _formState.value = NotaFormState(isLoading = true)
            repository.atualizar(disciplinaId, id, NotaRequest(titulo, conteudo))
                .onSuccess {
                    _formState.value = NotaFormState(sucesso = true)
                    carregarNotas(disciplinaId)
                }
                .onFailure { _formState.value = NotaFormState(error = it.message) }
        }
    }

    fun excluir(disciplinaId: String, id: String) {
        viewModelScope.launch {
            repository.excluir(disciplinaId, id)
                .onSuccess { carregarNotas(disciplinaId) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun buscar(disciplinaId: String, termo: String) {
        _uiState.value = _uiState.value.copy(termoBusca = termo)
        if (termo.isBlank()) {
            carregarNotas(disciplinaId)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.buscarPorTitulo(disciplinaId, termo)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(notas = it, isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message, isLoading = false)
                }
        }
    }

    fun limparFormState() {
        _formState.value = NotaFormState()
    }
}
