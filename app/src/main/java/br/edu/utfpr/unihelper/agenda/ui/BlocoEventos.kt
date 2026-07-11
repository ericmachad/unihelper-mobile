package br.edu.utfpr.unihelper.agenda.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.agenda.data.local.EventoEntity
import br.edu.utfpr.unihelper.core.ui.ErrorDialogHandler
import br.edu.utfpr.unihelper.core.ui.SuccessDialogHandler
import br.edu.utfpr.unihelper.core.ui.UiEvent
import br.edu.utfpr.unihelper.disciplina.data.remote.HorarioResponse
import br.edu.utfpr.unihelper.ui.theme.Accent
import br.edu.utfpr.unihelper.ui.theme.Alert
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Success
import br.edu.utfpr.unihelper.ui.theme.TextGray
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BlocoEventos(
    disciplinaId: String,
    horarios: List<HorarioResponse> = emptyList(),
    viewModel: EventosDisciplinaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var feedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(disciplinaId) {
        viewModel.carregar(disciplinaId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Snackbar -> feedback = event.message
                else -> { }
            }
        }
    }

    SuccessDialogHandler(uiEvent = viewModel.uiEvent)
    ErrorDialogHandler(uiEvent = viewModel.uiEvent)

    if (uiState.showDialog) {
        val totalPesosAtual = uiState.eventos
            .filter { it.id != uiState.eventoParaEdicao?.id }
            .sumOf { it.peso?.toDouble() ?: 0.0 }
            .toFloat()
        EventoDialog(
            totalPesosAtual = totalPesosAtual,
            horarios = horarios,
            evento = uiState.eventoParaEdicao,
            onSalvar = { descricao, peso, data, horaInicio, horaFim, valor, tipo ->
                viewModel.criarOuAtualizar(disciplinaId, descricao, peso, data, horaInicio, horaFim, valor, tipo)
            },
            onDismiss = { viewModel.fecharDialog() }
        )
    }

    uiState.eventoParaNota?.let { evento ->
        if (uiState.showBottomSheet) {
            LancarNotaBottomSheet(
                evento = evento,
                onSalvar = { valor -> viewModel.lancarNota(valor) },
                onDismiss = { viewModel.fecharBottomSheet() }
            )
        }
    }

    if (uiState.showDeleteDialog) {
        uiState.eventoParaDeletar?.let { evento ->
            AlertDialog(
                onDismissRequest = { viewModel.fecharDialogDelete() },
                title = { Text("Excluir Avaliação") },
                text = {
                    Text("Tem certeza que deseja excluir \"${evento.titulo}\"? Esta ação não pode ser desfeita.")
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.confirmarDelete() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Alert)
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.fecharDialogDelete() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

    if (uiState.showConfigMediaDialog) {
        ConfigMediaMinimaDialog(
            valorAtual = uiState.mediaMinima,
            onSalvar = { viewModel.salvarMediaMinima(it) },
            onDismiss = { viewModel.fecharConfigMedia() }
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Avaliações",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )
            FilledTonalButton(
                onClick = { viewModel.abrirDialogCriar() },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Adicionar")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Text(
                text = "Carregando...",
                fontSize = 13.sp,
                color = TextGray
            )
        } else if (uiState.eventos.isEmpty()) {
            Text(
                text = "Nenhuma avaliação cadastrada",
                fontSize = 13.sp,
                color = TextGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            uiState.eventos.forEach { evento ->
                EventoCard(
                    evento = evento,
                    mediaMinima = uiState.mediaMinima,
                    onEditar = { viewModel.abrirDialogEditar(evento) },
                    onLancarNota = { viewModel.abrirBottomSheetNota(evento) },
                    onExcluir = { viewModel.abrirDialogDelete(evento) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val media = uiState.media
        val notaMinima = uiState.notaMinimaNecessaria
        val status = uiState.statusAprovacao

        val statusText = when (status) {
            StatusAprovacao.APROVADO -> "Aprovado"
            StatusAprovacao.RECUPERACAO -> "Em recuperação"
            StatusAprovacao.REPROVADO -> "Reprovado"
            StatusAprovacao.INDEFINIDO -> "—"
        }

        val statusColor = when (status) {
            StatusAprovacao.APROVADO -> Success
            StatusAprovacao.RECUPERACAO -> Accent
            StatusAprovacao.REPROVADO -> Alert
            StatusAprovacao.INDEFINIDO -> TextGray
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Média",
                    fontSize = 11.sp,
                    color = TextGray
                )
                Text(
                    text = if (media != null) "%.1f".format(media) else "—",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Mínima",
                    fontSize = 11.sp,
                    color = TextGray
                )
                Text(
                    text = "%.1f".format(uiState.mediaMinima),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.clickable { viewModel.abrirConfigMedia() }
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Status",
                    fontSize = 11.sp,
                    color = TextGray
                )
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
        }

        if (notaMinima != null && status == StatusAprovacao.RECUPERACAO) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nota mínima necessária: %.1f".format(notaMinima),
                fontSize = 12.sp,
                color = Alert,
                fontWeight = FontWeight.Medium
            )
        }

        feedback?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                fontSize = 13.sp,
                color = Success,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
