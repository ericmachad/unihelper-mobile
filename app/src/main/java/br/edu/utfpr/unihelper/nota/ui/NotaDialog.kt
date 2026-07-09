package br.edu.utfpr.unihelper.nota.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.edu.utfpr.unihelper.nota.data.remote.NotaResponse
import br.edu.utfpr.unihelper.ui.theme.Primary

@Composable
fun NotaDialog(
    notaExistente: NotaResponse? = null,
    onDismiss: () -> Unit,
    onConfirm: (titulo: String, conteudo: String?) -> Unit
) {
    var titulo by remember { mutableStateOf(notaExistente?.titulo ?: "") }
    var conteudo by remember { mutableStateOf(notaExistente?.conteudo ?: "") }
    var erro by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (notaExistente != null) "Editar Anotação" else "Nova Anotação")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it; erro = null },
                    label = { Text("Título") },
                    singleLine = true,
                    isError = erro != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = conteudo,
                    onValueChange = { conteudo = it },
                    label = { Text("Conteúdo") },
                    minLines = 3,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                if (erro != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = erro!!, color = androidx.compose.ui.graphics.Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isBlank()) {
                        erro = "Título é obrigatório"
                    } else {
                        onConfirm(titulo.trim(), conteudo.trim().ifEmpty { null })
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
                Text("Cancelar")
            }
        }
    )
}
