package br.edu.utfpr.unihelper.agenda.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.agenda.data.local.EventoEntity
import br.edu.utfpr.unihelper.agenda.data.remote.EventoRequest
import br.edu.utfpr.unihelper.agenda.data.remote.MediaResponse
import br.edu.utfpr.unihelper.agenda.data.repository.AgendaRepository
import br.edu.utfpr.unihelper.core.local.MediaConfig
import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.network.toErrorDialog
import br.edu.utfpr.unihelper.core.ui.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BlocoEventosUiState(
    val eventos: List<EventoEntity> = emptyList(),
    val media: Float? = null,
    val notaMinimaNecessaria: Float? = null,
    val statusAprovacao: StatusAprovacao = StatusAprovacao.INDEFINIDO,
    val mediaMinima: Float = MediaConfig.DEFAULT_MEDIA_MINIMA,
    val isLoading: Boolean = true,
    val eventoParaNota: EventoEntity? = null,
    val eventoParaEdicao: EventoEntity? = null,
    val eventoParaDeletar: EventoEntity? = null,
    val showBottomSheet: Boolean = false,
    val showDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showConfigMediaDialog: Boolean = false
)

enum class StatusAprovacao {
    APROVADO,
    RECUPERACAO,
    REPROVADO,
    INDEFINIDO
}

class EventosDisciplinaViewModel(
    private val repository: AgendaRepository,
    private val mediaConfig: MediaConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlocoEventosUiState())
    val uiState: StateFlow<BlocoEventosUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun carregar(disciplinaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.syncDoBackend(disciplinaId)

            mediaConfig.mediaMinima.collect { mediaMinima ->
                _uiState.update { it.copy(mediaMinima = mediaMinima) }
            }
        }

        viewModelScope.launch {
            repository.listarPorDisciplinaFlow(disciplinaId).collect { eventos ->
                val mediaMinima = _uiState.value.mediaMinima
                val stats = calcularEstatisticas(eventos, mediaMinima)
                _uiState.update {
                    it.copy(
                        eventos = eventos,
                        isLoading = false,
                        media = stats.media,
                        notaMinimaNecessaria = stats.notaMinimaNecessaria,
                        statusAprovacao = stats.statusAprovacao
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.calcularMedia(disciplinaId, _uiState.value.mediaMinima)
                .onSuccess { mediaResponse ->
                    val status = when (mediaResponse.statusAprovacao) {
                        "APROVADO" -> StatusAprovacao.APROVADO
                        "RECUPERACAO" -> StatusAprovacao.RECUPERACAO
                        "REPROVADO" -> StatusAprovacao.REPROVADO
                        else -> StatusAprovacao.INDEFINIDO
                    }
                    _uiState.update {
                        it.copy(
                            media = mediaResponse.media ?: it.media,
                            notaMinimaNecessaria = mediaResponse.notaMinimaNecessaria,
                            statusAprovacao = status,
                            isLoading = false
                        )
                    }
                }
                .onFailure { _uiEvent.tryEmit(it.toErrorDialog()) }
        }
    }

    fun abrirBottomSheetNota(evento: EventoEntity) {
        _uiState.update { it.copy(eventoParaNota = evento, showBottomSheet = true) }
    }

    fun fecharBottomSheet() {
        _uiState.update { it.copy(eventoParaNota = null, showBottomSheet = false) }
    }

    fun lancarNota(valor: Float) {
        val evento = _uiState.value.eventoParaNota ?: return
        viewModelScope.launch {
            repository.lancarNota(evento.id, valor)
                .onSuccess {
                    fecharBottomSheet()
                    _uiEvent.tryEmit(UiEvent.SuccessDialog("Nota lançada com sucesso"))
                }
                .onFailure { error ->
                    val salvoOffline = error is ApiException && (error.status == 0 || error.status >= 500)
                    if (salvoOffline) {
                        fecharBottomSheet()
                        _uiEvent.tryEmit(UiEvent.SuccessDialog("Nota lançada com sucesso"))
                    } else {
                        _uiEvent.tryEmit(error.toErrorDialog())
                    }
                }
        }
    }

    fun abrirDialogCriar() {
        _uiState.update { it.copy(eventoParaEdicao = null, showDialog = true) }
    }

    fun abrirDialogEditar(evento: EventoEntity) {
        _uiState.update { it.copy(eventoParaEdicao = evento, showDialog = true) }
    }

    fun fecharDialog() {
        _uiState.update { it.copy(eventoParaEdicao = null, showDialog = false) }
    }

    fun criarOuAtualizar(
        disciplinaId: String,
        descricao: String,
        peso: Float,
        data: String,
        horaInicio: String,
        horaFim: String,
        valor: Float? = null,
        tipo: String = "PROVA"
    ) {
        val edicao = _uiState.value.eventoParaEdicao
        viewModelScope.launch {
            if (edicao != null) {
                val request = EventoRequest(
                    titulo = descricao,
                    tipo = tipo,
                    dataHoraInicio = "${data}T${horaInicio}:00",
                    dataHoraFim = "${data}T${horaFim}:00",
                    peso = peso,
                    valor = valor
                )
                repository.atualizarEvento(edicao.id, request)
                    .onSuccess {
                        fecharDialog()
                        _uiEvent.tryEmit(UiEvent.SuccessDialog("Avaliação atualizada"))
                    }
                    .onFailure { error ->
                        val salvoOffline = error is ApiException && (error.status == 0 || error.status >= 500)
                        if (salvoOffline) {
                            fecharDialog()
                            _uiEvent.tryEmit(UiEvent.SuccessDialog("Avaliação atualizada"))
                        } else {
                            _uiEvent.tryEmit(error.toErrorDialog())
                        }
                    }
            } else {
                val request = EventoRequest(
                    titulo = descricao,
                    tipo = tipo,
                    dataHoraInicio = "${data}T${horaInicio}:00",
                    dataHoraFim = "${data}T${horaFim}:00",
                    peso = peso,
                    valor = valor,
                    disciplinaId = disciplinaId
                )
                repository.criarEvento(request)
                    .onSuccess {
                        fecharDialog()
                        _uiEvent.tryEmit(UiEvent.SuccessDialog("Avaliação criada"))
                    }
                    .onFailure { error ->
                        val salvoOffline = error is ApiException && (error.status == 0 || error.status >= 500)
                        if (salvoOffline) {
                            fecharDialog()
                            _uiEvent.tryEmit(UiEvent.SuccessDialog("Avaliação criada"))
                        } else {
                            _uiEvent.tryEmit(error.toErrorDialog())
                        }
                    }
            }
        }
    }

    fun abrirDialogDelete(evento: EventoEntity) {
        _uiState.update {
            it.copy(eventoParaDeletar = evento, showDeleteDialog = true)
        }
    }

    fun fecharDialogDelete() {
        _uiState.update {
            it.copy(eventoParaDeletar = null, showDeleteDialog = false)
        }
    }

    fun confirmarDelete() {
        val evento = _uiState.value.eventoParaDeletar ?: return
        viewModelScope.launch {
            repository.excluirEvento(evento.id)
                .onSuccess {
                    fecharDialogDelete()
                    _uiEvent.tryEmit(UiEvent.SuccessDialog("Avaliação excluída"))
                }
                .onFailure { error ->
                    val salvoOffline = error is ApiException && (error.status == 0 || error.status >= 500)
                    if (salvoOffline) {
                        fecharDialogDelete()
                        _uiEvent.tryEmit(UiEvent.SuccessDialog("Avaliação excluída"))
                    } else {
                        _uiEvent.tryEmit(error.toErrorDialog())
                    }
                }
        }
    }

    fun abrirConfigMedia() {
        _uiState.update { it.copy(showConfigMediaDialog = true) }
    }

    fun fecharConfigMedia() {
        _uiState.update { it.copy(showConfigMediaDialog = false) }
    }

    fun salvarMediaMinima(valor: Float) {
        viewModelScope.launch {
            mediaConfig.setMediaMinima(valor)
            fecharConfigMedia()
            _uiEvent.tryEmit(UiEvent.SuccessDialog("Média mínima alterada para %.1f".format(valor)))
        }
    }

    private fun calcularEstatisticas(
        eventos: List<EventoEntity>,
        mediaMinima: Float
    ): Estatisticas {
        val comNota = eventos.filter { it.valor != null }
        val pesoTotais = comNota.sumOf { it.peso?.toDouble() ?: 0.0 }.toFloat()
        val somaPonderada = comNota.sumOf { (it.valor!! * (it.peso ?: 0f)).toDouble() }.toFloat()

        val media = if (pesoTotais > 0f) somaPonderada / pesoTotais else null

        val pesoRestante = eventos.filter { it.valor == null }.sumOf { it.peso?.toDouble() ?: 0.0 }.toFloat()
        val totalPesos = if (eventos.sumOf { it.peso?.toDouble() ?: 0.0 } > 0.0) {
            eventos.sumOf { it.peso?.toDouble() ?: 0.0 }.toFloat()
        } else {
            1f
        }

        val notaMinimaNecessaria: Float? = if (pesoRestante > 0f && media != null) {
            ((mediaMinima * totalPesos - somaPonderada) / pesoRestante).coerceIn(0f, 10f)
        } else if (pesoRestante > 0f && media == null) {
            mediaMinima
        } else {
            null
        }

        val status = when {
            media == null -> StatusAprovacao.INDEFINIDO
            media >= mediaMinima -> StatusAprovacao.APROVADO
            pesoRestante <= 0f -> StatusAprovacao.REPROVADO
            else -> StatusAprovacao.RECUPERACAO
        }

        return Estatisticas(media, notaMinimaNecessaria, status)
    }

    private data class Estatisticas(
        val media: Float?,
        val notaMinimaNecessaria: Float?,
        val statusAprovacao: StatusAprovacao
    )
}
