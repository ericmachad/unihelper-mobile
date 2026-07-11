package br.edu.utfpr.unihelper.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.agenda.data.repository.AgendaRepository
import br.edu.utfpr.unihelper.core.network.toErrorDialog
import br.edu.utfpr.unihelper.core.ui.UiEvent
import br.edu.utfpr.unihelper.dashboard.data.DashboardEvent
import br.edu.utfpr.unihelper.dashboard.data.toDashboardEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DashboardUiState(
    val mesAtual: YearMonth = YearMonth.now(),
    val selectedDate: Int? = null,
    val eventos: List<DashboardEvent> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

class DashboardViewModel(
    private val repository: AgendaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var flowJob: Job? = null

    init {
        carregarMes()
    }

    fun carregarMes(isRefresh: Boolean = false) {
        val mes = _uiState.value.mesAtual
        val inicio = mes.atDay(1)
        val fim = mes.atEndOfMonth()

        flowJob?.cancel()
        flowJob = viewModelScope.launch {
            repository.listarFlow(inicio.toString(), fim.toString()).collect { entities ->
                _uiState.update {
                    it.copy(
                        eventos = entities.mapNotNull { entity -> entity.toDashboardEvent() },
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }

        if (isRefresh) {
            _uiState.update { it.copy(isRefreshing = true) }
        } else {
            _uiState.update { it.copy(isLoading = true) }
        }

        viewModelScope.launch {
            repository.listar(inicio.toString(), fim.toString())
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
                    _uiEvent.tryEmit(UiEvent.Snackbar("Não foi possível atualizar. Dados offline exibidos."))
                }
        }
    }

    fun onMonthChange(avancar: Boolean) {
        _uiState.update {
            it.copy(
                mesAtual = if (avancar) it.mesAtual.plusMonths(1) else it.mesAtual.minusMonths(1),
                selectedDate = null
            )
        }
        carregarMes()
    }

    fun onDateSelected(day: Int) {
        _uiState.update {
            it.copy(selectedDate = if (it.selectedDate == day) null else day)
        }
    }

    fun formatarMesAno(): String {
        val mes = _uiState.value.mesAtual
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("pt-BR"))
        return mes.atDay(1).format(formatter).replaceFirstChar { it.uppercase() }
    }
}