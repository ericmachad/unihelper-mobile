package br.edu.utfpr.unihelper.agenda.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.core.ui.ErrorDialogHandler
import br.edu.utfpr.unihelper.core.ui.SuccessDialogHandler
import br.edu.utfpr.unihelper.ui.theme.Accent
import br.edu.utfpr.unihelper.ui.theme.Alert
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Border
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Success
import br.edu.utfpr.unihelper.ui.theme.Surface
import br.edu.utfpr.unihelper.ui.theme.TextGray
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AgendaCRUDScreen(viewModel: AgendaViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is br.edu.utfpr.unihelper.core.ui.UiEvent.Snackbar ->
                    snackbarHostState.showSnackbar(event.message)
                else -> { }
            }
        }
    }

    SuccessDialogHandler(uiEvent = viewModel.uiEvent)
    ErrorDialogHandler(uiEvent = viewModel.uiEvent)

    if (uiState.showEventoBottomSheet) {
        EventoBottomSheet(
            eventoParaEditar = uiState.eventoParaEditar,
            disciplinas = uiState.disciplinas,
            onSalvar = { titulo, tipo, dataHoraInicio, dataHoraFim, peso, disciplinaId ->
                viewModel.salvarEvento(titulo, tipo, dataHoraInicio, dataHoraFim, peso, disciplinaId)
            },
            onDismiss = { viewModel.fecharEventoBottomSheet() }
        )
    }

    if (uiState.showExcluirConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarExcluir() },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Excluir evento", fontWeight = FontWeight.Bold, color = Primary) },
            text = { Text("Tem certeza que deseja excluir este evento?") },
            confirmButton = {
                TextButton(onClick = { viewModel.excluirEvento() }) {
                    Text("Excluir", color = Alert)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarExcluir() }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Carregando...", color = TextGray)
            }
        } else if (uiState.itens.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhum evento nos próximos meses", color = TextGray, fontSize = 14.sp)
                }
            }
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.carregarProximos(isRefresh = true) },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.itens) { item ->
                        AgendaCRUDCard(
                            item = item,
                            onEditar = { viewModel.abrirEditarItem(item) },
                            onExcluir = { viewModel.confirmarExcluir(item.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun AgendaCRUDCard(
    item: AgendaItemUi,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    val corBorda = when (item.tipoEvento) {
        "PROVA" -> Alert
        "TRABALHO" -> Accent
        else -> Success
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Border),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(modifier = Modifier.padding(end = 4.dp)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxWidth(0.015f)
                    .background(corBorda)
            )

            Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                Text(
                    text = item.titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = dataHoraCompacta(item.dataHora, item.dataHoraFim),
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    if (item.tipoEvento != "OUTRO") {
                        Text(
                            text = "  •  ${item.tipoEvento}",
                            fontSize = 12.sp,
                            color = corBorda,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (item.disciplinaNome != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.disciplinaNome,
                        fontSize = 12.sp,
                        color = Accent
                    )
                }
                if (item.tipoOrigem == "AVALIACAO" && item.valor != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Nota: %.1f".format(item.valor),
                        fontSize = 12.sp,
                        color = if (item.valor >= 6f) Success else Alert,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(onClick = onEditar) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = TextGray,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onExcluir) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = Alert,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun dataHoraCompacta(dataHora: String, dataHoraFim: String?): String {
    return try {
        val inicio = LocalDateTime.parse(dataHora)
        val formato = DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale.forLanguageTag("pt-BR"))
        val inicioStr = inicio.format(formato)
        if (dataHoraFim != null) {
            val fim = LocalDateTime.parse(dataHoraFim)
            "$inicioStr - ${fim.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        } else {
            inicioStr
        }
    } catch (_: Exception) {
        dataHora.take(16).replace("T", " ")
    }
}
