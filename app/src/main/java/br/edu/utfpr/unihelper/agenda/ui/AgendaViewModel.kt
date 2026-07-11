package br.edu.utfpr.unihelper.agenda.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.agenda.data.local.EventoEntity
import br.edu.utfpr.unihelper.agenda.data.remote.AgendaItemResponse
import br.edu.utfpr.unihelper.agenda.data.remote.EventoRequest
import br.edu.utfpr.unihelper.agenda.data.repository.AgendaRepository
import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.network.toErrorDialog
import br.edu.utfpr.unihelper.core.sync.AuthEvent
import br.edu.utfpr.unihelper.core.sync.AuthEventBus
import br.edu.utfpr.unihelper.core.ui.UiEvent
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AgendaItemUi(
    val id: String,
    val titulo: String,
    val tipoOrigem: String,
    val tipoEvento: String,
    val dataHora: String,
    val dataHoraFim: String?,
    val peso: Float?,
    val valor: Float?,
    val disciplinaId: String?,
    val disciplinaNome: String?
)

private fun AgendaItemResponse.toAgendaItemUi() = AgendaItemUi(
    id = id,
    titulo = titulo,
    tipoOrigem = "EVENTO",
    tipoEvento = tipoEvento,
    dataHora = dataHora,
    dataHoraFim = dataHoraFim,
    peso = peso,
    valor = null,
    disciplinaId = disciplinaId,
    disciplinaNome = disciplinaNome
)

private fun EventoEntity.toAgendaItemUi() = AgendaItemUi(
    id = id,
    titulo = titulo,
    tipoOrigem = "EVENTO",
    tipoEvento = tipo,
    dataHora = dataHoraInicio,
    dataHoraFim = dataHoraFim,
    peso = peso,
    valor = valor,
    disciplinaId = disciplinaId,
    disciplinaNome = disciplinaNome
)

data class AgendaUiState(
    val itens: List<AgendaItemUi> = emptyList(),
    val disciplinas: List<DisciplinaResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val showEventoBottomSheet: Boolean = false,
    val eventoParaEditar: AgendaItemResponse? = null,
    val showExcluirConfirm: Boolean = false,
    val eventoExcluirId: String? = null
)

class AgendaViewModel(
    private val repository: AgendaRepository,
    private val disciplinaRepository: DisciplinaRepository,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private var ultimosEventos: List<AgendaItemResponse> = emptyList()
    private var jobInicial: kotlinx.coroutines.Job? = null
    private var flowJob: kotlinx.coroutines.Job? = null
    private var disciplinasFlowJob: kotlinx.coroutines.Job? = null
    private var inicioStr = ""
    private var fimStr = ""

    init {
        viewModelScope.launch {
            authEventBus.events.collect { event ->
                when (event) {
                    is AuthEvent.LoggedOut -> resetState()
                    is AuthEvent.LoggedIn -> {
                        resetState()
                        iniciarFlow()
                        iniciarDisciplinasFlow()
                        carregarProximos()
                    }
                }
            }
        }
    }

    private fun iniciarFlow() {
        flowJob?.cancel()
        val hoje = LocalDate.now()
        val fim = hoje.plusMonths(6)
        inicioStr = hoje.format(formatter)
        fimStr = fim.format(formatter)

        flowJob = viewModelScope.launch {
            repository.listarFlow(inicioStr, fimStr).collect { entities ->
                val itens = entities.map { it.toAgendaItemUi() }.sortedBy { it.dataHora }
                _uiState.update { it.copy(itens = itens, isLoading = false, isRefreshing = false) }
            }
        }
    }

    fun carregarProximos(isRefresh: Boolean = false) {
        if (isRefresh) {
            _uiState.update { it.copy(isRefreshing = true) }
        }

        jobInicial?.cancel()
        jobInicial = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh) }

            repository.listar(inicioStr, fimStr)
                .onSuccess { eventos -> ultimosEventos = eventos }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
                    _uiEvent.tryEmit(UiEvent.Snackbar("Não foi possível atualizar. Dados offline exibidos."))
                }
        }
    }

    private fun iniciarDisciplinasFlow() {
        disciplinasFlowJob?.cancel()
        disciplinasFlowJob = viewModelScope.launch {
            disciplinaRepository.listarDisciplinasFlow().collect { disciplinas ->
                _uiState.update { it.copy(disciplinas = disciplinas) }
            }
        }
    }

    fun abrirCriarEvento() {
        _uiState.update { it.copy(showEventoBottomSheet = true, eventoParaEditar = null) }
    }

    fun abrirEditarEvento(evento: AgendaItemResponse) {
        _uiState.update { it.copy(showEventoBottomSheet = true, eventoParaEditar = evento) }
    }

    fun abrirEditarItem(item: AgendaItemUi) {
        val eventoResponse = AgendaItemResponse(
            id = item.id,
            titulo = item.titulo,
            tipoEvento = item.tipoEvento,
            dataHora = item.dataHora,
            dataHoraFim = item.dataHoraFim,
            peso = item.peso,
            disciplinaId = item.disciplinaId,
            disciplinaNome = item.disciplinaNome,
            tipoOrigem = item.tipoOrigem
        )
        _uiState.update { it.copy(showEventoBottomSheet = true, eventoParaEditar = eventoResponse) }
    }

    fun fecharEventoBottomSheet() {
        _uiState.update { it.copy(showEventoBottomSheet = false, eventoParaEditar = null) }
    }

    fun salvarEvento(titulo: String, tipo: String, dataHoraInicio: String, dataHoraFim: String, peso: Float?, disciplinaId: String?) {
        val editando = _uiState.value.eventoParaEditar

        viewModelScope.launch {
            if (editando != null) {
                val request = EventoRequest(
                    titulo = titulo,
                    tipo = tipo,
                    dataHoraInicio = dataHoraInicio,
                    dataHoraFim = dataHoraFim,
                    peso = peso,
                    disciplinaId = disciplinaId
                )
                repository.atualizarEvento(editando.id, request)
                    .fold(
                        onSuccess = {
                            fecharEventoBottomSheet()
                            _uiEvent.tryEmit(UiEvent.SuccessDialog("Evento atualizado"))
                            carregarProximos()
                        },
                        onFailure = { error ->
                            if (error is ApiException && error.status == 0) {
                                fecharEventoBottomSheet()
                                _uiEvent.tryEmit(UiEvent.SuccessDialog("Evento atualizado"))
                                carregarProximos()
                            } else {
                                _uiEvent.tryEmit(error.toErrorDialog())
                            }
                        }
                    )
            } else {
                val request = EventoRequest(
                    titulo = titulo,
                    tipo = tipo,
                    dataHoraInicio = dataHoraInicio,
                    dataHoraFim = dataHoraFim,
                    peso = peso,
                    disciplinaId = disciplinaId
                )
                repository.criarEvento(request)
                    .fold(
                        onSuccess = {
                            fecharEventoBottomSheet()
                            _uiEvent.tryEmit(UiEvent.SuccessDialog("Evento criado"))
                            carregarProximos()
                        },
                        onFailure = { error ->
                            if (error is ApiException && error.status == 0) {
                                fecharEventoBottomSheet()
                                _uiEvent.tryEmit(UiEvent.SuccessDialog("Evento criado"))
                                carregarProximos()
                            } else {
                                _uiEvent.tryEmit(error.toErrorDialog())
                            }
                        }
                    )
            }
        }
    }

    fun confirmarExcluir(id: String) {
        _uiState.update { it.copy(showExcluirConfirm = true, eventoExcluirId = id) }
    }

    fun cancelarExcluir() {
        _uiState.update { it.copy(showExcluirConfirm = false, eventoExcluirId = null) }
    }

    fun excluirEvento() {
        val id = _uiState.value.eventoExcluirId ?: return
        viewModelScope.launch {
            repository.excluirEvento(id)
                .fold(
                    onSuccess = {
                        cancelarExcluir()
                        _uiEvent.tryEmit(UiEvent.SuccessDialog("Evento excluído"))
                        carregarProximos()
                    },
                    onFailure = { error ->
                        cancelarExcluir()
                        if (error is ApiException && error.status == 0) {
                            _uiEvent.tryEmit(UiEvent.SuccessDialog("Evento excluído"))
                            carregarProximos()
                        } else {
                            _uiEvent.tryEmit(error.toErrorDialog())
                        }
                    }
                )
        }
    }

    fun resetState() {
        _uiState.value = AgendaUiState(isLoading = true)
    }

}
