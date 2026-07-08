package br.edu.utfpr.unihelper.avaliacao.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import br.edu.utfpr.unihelper.ui.theme.Accent
import br.edu.utfpr.unihelper.ui.theme.Alert
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Border
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Success
import br.edu.utfpr.unihelper.ui.theme.TextGray
@Composable
fun BlocoAvaliacao(
    disciplinaId: String,
    viewModel: AvaliacaoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(disciplinaId) {
        viewModel.carregar(disciplinaId)
    }

    // Dialog de criar/editar
    if (uiState.showDialog) {
        AvaliacaoDialog(
            disciplinaId = disciplinaId,
            avaliacao = uiState.avaliacaoParaEdicao,
            onSalvar = { descricao, peso, data, valor, tipo ->
                viewModel.criarOuAtualizar(disciplinaId, descricao, peso, data, valor, tipo)
            },
            onDismiss = { viewModel.fecharDialog() }
        )
    }

    // Bottom sheet de lançar nota
    uiState.avaliacaoParaNota?.let { avaliacao ->
        if (uiState.showBottomSheet) {
            LancarNotaBottomSheet(
                avaliacao = avaliacao,
                onSalvar = { valor -> viewModel.lancarNota(valor) },
                onDismiss = { viewModel.fecharBottomSheet() }
            )
        }
    }

    // Dialog de confirmar exclusão
    if (uiState.showDeleteDialog) {
        uiState.avaliacaoParaDeletar?.let { avaliacao ->
            AlertDialog(
                onDismissRequest = { viewModel.fecharDialogDelete() },
                title = { Text("Excluir Avaliação") },
                text = {
                    Text("Tem certeza que deseja excluir \"${avaliacao.descricao}\"? Esta ação não pode ser desfeita.")
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

    // Dialog de configurar média mínima
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
        } else if (uiState.avaliacoes.isEmpty()) {
            Text(
                text = "Nenhuma avaliação cadastrada",
                fontSize = 13.sp,
                color = TextGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            uiState.avaliacoes.forEach { avaliacao ->
                AvaliacaoCard(
                    avaliacao = avaliacao,
                    mediaMinima = uiState.mediaMinima,
                    onEditar = { viewModel.abrirDialogEditar(avaliacao) },
                    onLancarNota = { viewModel.abrirBottomSheetNota(avaliacao) },
                    onExcluir = { viewModel.abrirDialogDelete(avaliacao) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Footer com estatísticas
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

        // Mensagens de feedback
        uiState.mensagemSucesso?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                fontSize = 13.sp,
                color = Success,
                fontWeight = FontWeight.Medium
            )
        }

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                fontSize = 12.sp,
                color = Alert
            )
        }
    }
}

@Composable
private fun ConfigMediaMinimaDialog(
    valorAtual: Float,
    onSalvar: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf("%.1f".format(valorAtual)) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Média Mínima",
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        },
        text = {
            Column {
                Text(
                    text = "Defina a nota mínima para aprovação (1 a 10):",
                    fontSize = 14.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { value ->
                        val filtered = value.filter { c: Char -> c.isDigit() || c == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            textValue = filtered
                            val parsed = filtered.toFloatOrNull()
                            hasError = parsed != null && (parsed < 1f || parsed > 10f)
                        }
                    },
                    isError = hasError,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Border,
                        focusedBorderColor = Primary,
                        unfocusedContainerColor = Background
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (hasError) {
                    Text(
                        text = "Valor deve estar entre 1 e 10",
                        fontSize = 12.sp,
                        color = Alert,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = textValue.toFloatOrNull()
                    if (parsed != null && parsed in 1f..10f) {
                        onSalvar(parsed)
                    } else {
                        hasError = true
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}
