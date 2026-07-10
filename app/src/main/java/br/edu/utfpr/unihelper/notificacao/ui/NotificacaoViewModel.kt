package br.edu.utfpr.unihelper.notificacao.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.notificacao.data.remote.NotificacaoResponse
import br.edu.utfpr.unihelper.notificacao.data.repository.NotificacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificacaoUiState(
    val notificacoes: List<NotificacaoResponse> = emptyList(),
    val totalNaoLidas: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificacaoViewModel(
    private val repository: NotificacaoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacaoUiState())
    val uiState: StateFlow<NotificacaoUiState> = _uiState.asStateFlow()

    init {
        carregarNotificacoes()
    }

    fun carregarNotificacoes(apenasNaoLidas: Boolean? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.listar(apenasNaoLidas)
                .fold(
                    onSuccess = { response ->
                        _uiState.update {
                            it.copy(
                                notificacoes = response.notificacoes,
                                totalNaoLidas = response.totalNaoLidas,
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isLoading = false, error = e.message ?: "Erro ao carregar notificacoes")
                        }
                    }
                )
        }
    }

    fun marcarComoLida(id: String) {
        viewModelScope.launch {
            repository.marcarComoLida(id)
                .fold(
                    onSuccess = {
                        carregarNotificacoes()
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(error = e.message ?: "Erro ao marcar como lida")
                        }
                    }
                )
        }
    }

    fun marcarTodasComoLidas() {
        viewModelScope.launch {
            repository.marcarTodasComoLidas()
                .fold(
                    onSuccess = {
                        carregarNotificacoes()
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(error = e.message ?: "Erro ao marcar todas como lidas")
                        }
                    }
                )
        }
    }

    fun limparError() {
        _uiState.update { it.copy(error = null) }
    }
}
