package br.edu.utfpr.unihelper.dashboard.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.agenda.data.repository.AgendaRepository
import br.edu.utfpr.unihelper.core.local.TokenStorage
import br.edu.utfpr.unihelper.dashboard.data.DashboardEvent
import br.edu.utfpr.unihelper.dashboard.data.toDashboardEvent
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val error: String? = null,
    val fcmToken: String? = null,
    val isLoadingToken: Boolean = false,
    val showFcmDialog: Boolean = false
)

class DashboardViewModel(
    private val repository: AgendaRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        carregarMes()
    }

    fun carregarMes() {
        val mes = _uiState.value.mesAtual
        val inicio = mes.atDay(1)
        val fim = mes.atEndOfMonth()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.listar(inicio.toString(), fim.toString())
                .fold(
                    onSuccess = { items ->
                        _uiState.update {
                            it.copy(
                                eventos = items.mapNotNull { it.toDashboardEvent() },
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isLoading = false, error = e.message ?: "Erro ao carregar")
                        }
                    }
                )
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

    fun getFcmToken(): String? = tokenStorage.getFcmToken()

    fun showFcmDialog() {
        val stored = tokenStorage.getFcmToken()
        if (stored != null) {
            _uiState.update { it.copy(fcmToken = stored, showFcmDialog = true) }
        } else {
            _uiState.update { it.copy(fcmToken = null, isLoadingToken = true, showFcmDialog = true) }
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d(TAG, "FCM Token obtido: $token")
                        tokenStorage.saveFcmToken(token)
                        _uiState.update { it.copy(fcmToken = token, isLoadingToken = false) }
                    } else {
                        Log.e(TAG, "Falha ao obter FCM Token", task.exception)
                        _uiState.update { it.copy(fcmToken = null, isLoadingToken = false) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao chamar FirebaseMessaging", e)
                _uiState.update { it.copy(fcmToken = null, isLoadingToken = false) }
            }
        }
    }

    companion object {
        private const val TAG = "DashboardVM"
    }

    fun dismissFcmDialog() {
        _uiState.update { it.copy(showFcmDialog = false) }
    }
}
