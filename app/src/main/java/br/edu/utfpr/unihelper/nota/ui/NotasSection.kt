package br.edu.utfpr.unihelper.nota.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.nota.data.remote.NotaResponse
import br.edu.utfpr.unihelper.ui.theme.Alert
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.TextGray

@Composable
fun NotasSection(
    disciplinaId: String?,
    viewModel: NotaViewModel,
    onNotaClicada: (NotaResponse) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var notaEditando by remember { mutableStateOf<NotaResponse?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Excluir Anotação") },
            text = { Text("Tem certeza que deseja excluir esta anotação?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (disciplinaId != null && showDeleteDialog != null) {
                            viewModel.excluir(disciplinaId, showDeleteDialog!!)
                        }
                        showDeleteDialog = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = Alert
                    )
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Anotações",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nova Anotação",
                    tint = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.termoBusca,
            onValueChange = {
                if (disciplinaId != null) viewModel.buscar(disciplinaId, it)
            },
            placeholder = { Text("Buscar anotações...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isLoading) {
            Text(
                text = "Carregando...",
                fontSize = 13.sp,
                color = TextGray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else if (state.notas.isEmpty()) {
            Text(
                text = "Nenhuma anotação encontrada",
                fontSize = 13.sp,
                color = TextGray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            state.notas.forEach { nota ->
                NotaCard(
                    nota = nota,
                    onClick = { onNotaClicada(nota) },
                    onEditar = {
                        notaEditando = nota
                        showDialog = true
                    },
                    onExcluir = {
                        showDeleteDialog = nota.id
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }

    if (showDialog) {
        NotaDialog(
            notaExistente = notaEditando,
            onDismiss = {
                showDialog = false
                notaEditando = null
            },
            onConfirm = { titulo, conteudo ->
                if (disciplinaId != null) {
                    if (notaEditando != null) {
                        viewModel.atualizar(disciplinaId, notaEditando!!.id, titulo, conteudo)
                    } else {
                        viewModel.criar(disciplinaId, titulo, conteudo)
                    }
                }
                showDialog = false
                notaEditando = null
            }
        )
    }
}

@Composable
private fun NotaCard(
    nota: NotaResponse,
    onClick: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NoteAlt,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nota.titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!nota.conteudo.isNullOrBlank()) {
                    Text(
                        text = nota.conteudo,
                        fontSize = 12.sp,
                        color = TextGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onEditar, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextGray)
            }
            IconButton(onClick = onExcluir, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = TextGray)
            }
        }
    }
}
