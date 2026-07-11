package br.edu.utfpr.unihelper.notificacao.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.network.toErrorDialog
import br.edu.utfpr.unihelper.core.ui.UiEvent
import br.edu.utfpr.unihelper.notificacao.data.local.NotificacaoEntity
import br.edu.utfpr.unihelper.notificacao.data.remote.NotificacaoResponse
import br.edu.utfpr.unihelper.notificacao.data.repository.NotificacaoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificacaoUiState(
    val notificacoes: List<NotificacaoResponse> = emptyList(),
    val totalNaoLidas: Long = 0,
    val isLoading: Boolean = false
)

class NotificacaoViewModel(
    private val repository: NotificacaoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacaoUiState())
    val uiState: StateFlow<NotificacaoUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var notificacoesFlowJob: Job? = null

    init {
        iniciarNotificacoesFlow()
        carregarNotificacoes()
    }

    private fun iniciarNotificacoesFlow() {
        notificacoesFlowJob?.cancel()
        notificacoesFlowJob = viewModelScope.launch {
            repository.listarFlow().collect { entities ->
                _uiState.update {
                    it.copy(
                        notificacoes = entities.map { entity -> entity.toResponse() },
                        totalNaoLidas = entities.count { n -> !n.lida }.toLong(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun carregarNotificacoes(apenasNaoLidas: Boolean? = null) {
        viewModelScope.launch {
            repository.listar(apenasNaoLidas)
                .fold(
                    onSuccess = { },
                    onFailure = {
                        _uiEvent.tryEmit(UiEvent.Snackbar("Não foi possível atualizar. Dados offline exibidos."))
                    }
                )
        }
    }

    fun marcarComoLida(id: String) {
        viewModelScope.launch {
            repository.marcarComoLida(id)
                .fold(
                    onSuccess = { },
                    onFailure = { e ->
                        if (e is ApiException && e.status == 0) {
                            _uiEvent.tryEmit(UiEvent.Snackbar("Notificação marcada como lida"))
                        } else {
                            _uiEvent.tryEmit(e.toErrorDialog())
                        }
                    }
                )
        }
    }

    fun marcarTodasComoLidas() {
        viewModelScope.launch {
            repository.marcarTodasComoLidas()
                .fold(
                    onSuccess = { },
                    onFailure = { e ->
                        if (e is ApiException && e.status == 0) {
                            _uiEvent.tryEmit(UiEvent.Snackbar("Todas marcadas como lidas"))
                        } else {
                            _uiEvent.tryEmit(e.toErrorDialog())
                        }
                    }
                )
        }
    }
}

private fun NotificacaoEntity.toResponse() = NotificacaoResponse(
    id = id,
    tipo = tipo,
    titulo = titulo,
    mensagem = mensagem,
    lida = lida,
    criadaEm = criadaEm
)
