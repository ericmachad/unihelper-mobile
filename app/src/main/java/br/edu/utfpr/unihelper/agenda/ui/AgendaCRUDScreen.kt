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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.agenda.data.remote.AgendaItemResponse
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

@Composable
fun AgendaCRUDScreen(viewModel: AgendaViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensagemSucesso) {
        uiState.mensagemSucesso?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagens()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagens()
        }
    }

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
        } else if (uiState.eventos.isEmpty()) {
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.eventos) { evento ->
                    AgendaCRUDCard(
                        evento = evento,
                        onEditar = { viewModel.abrirEditarEvento(evento) },
                        onExcluir = { viewModel.confirmarExcluir(evento.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
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
    evento: AgendaItemResponse,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    val corBorda = when (evento.tipoEvento) {
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
                    text = evento.titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = dataHoraCompacta(evento.dataHora, evento.dataHoraFim),
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    if (evento.tipoEvento != "OUTRO") {
                        Text(
                            text = "  •  ${evento.tipoEvento}",
                            fontSize = 12.sp,
                            color = corBorda,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (evento.disciplinaNome != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = evento.disciplinaNome,
                        fontSize = 12.sp,
                        color = Accent
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
