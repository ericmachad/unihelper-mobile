package br.edu.utfpr.unihelper.agenda.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.agenda.data.remote.AgendaItemResponse
import br.edu.utfpr.unihelper.agenda.data.remote.EventoRequest
import br.edu.utfpr.unihelper.agenda.data.repository.AgendaRepository
import br.edu.utfpr.unihelper.core.sync.AuthEvent
import br.edu.utfpr.unihelper.core.sync.AuthEventBus
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaResponse
import br.edu.utfpr.unihelper.disciplina.data.repository.DisciplinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AgendaUiState(
    val eventos: List<AgendaItemResponse> = emptyList(),
    val disciplinas: List<DisciplinaResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensagemSucesso: String? = null,
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

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        viewModelScope.launch {
            authEventBus.events.collect { event ->
                when (event) {
                    is AuthEvent.LoggedOut -> resetState()
                    is AuthEvent.LoggedIn -> {
                        resetState()
                        carregarProximos()
                        carregarDisciplinas()
                    }
                }
            }
        }
        carregarProximos()
        carregarDisciplinas()
    }

    fun carregarProximos() {
        val hoje = LocalDate.now()
        val fim = hoje.plusMonths(6)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.listar(hoje.format(formatter), fim.format(formatter))
                .fold(
                    onSuccess = { eventos ->
                        _uiState.update { it.copy(eventos = eventos, isLoading = false) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao carregar") }
                    }
                )
        }
    }

    private fun carregarDisciplinas() {
        viewModelScope.launch {
            disciplinaRepository.listar()
                .onSuccess { disciplinas ->
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
                            _uiState.update { it.copy(mensagemSucesso = "Evento atualizado") }
                            carregarProximos()
                        },
                        onFailure = { e ->
                            _uiState.update { it.copy(error = e.message ?: "Erro ao atualizar evento") }
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
                            _uiState.update { it.copy(mensagemSucesso = "Evento criado") }
                            carregarProximos()
                        },
                        onFailure = { e ->
                            _uiState.update { it.copy(error = e.message ?: "Erro ao criar evento") }
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
                        _uiState.update { it.copy(mensagemSucesso = "Evento excluído") }
                        carregarProximos()
                    },
                    onFailure = { e ->
                        cancelarExcluir()
                        _uiState.update { it.copy(error = e.message ?: "Erro ao excluir evento") }
                    }
                )
        }
    }

    fun resetState() {
        _uiState.value = AgendaUiState(isLoading = true)
    }

    fun limparMensagens() {
        _uiState.update { it.copy(mensagemSucesso = null, error = null) }
    }
}
