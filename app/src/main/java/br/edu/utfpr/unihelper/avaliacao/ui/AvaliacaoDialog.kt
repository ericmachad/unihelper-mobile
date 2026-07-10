package br.edu.utfpr.unihelper.avaliacao.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.avaliacao.data.local.AvaliacaoEntity
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Border
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.TextGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvaliacaoDialog(
    disciplinaId: String,
    avaliacao: AvaliacaoEntity? = null,
    onSalvar: (descricao: String, peso: Float, data: String, valor: Float?, tipo: String) -> Unit,
    onDismiss: () -> Unit
) {
    val editando = avaliacao != null
    var descricao by remember { mutableStateOf(avaliacao?.descricao ?: "") }
    var peso by remember { mutableStateOf(avaliacao?.peso?.toString() ?: "") }
    var data by remember { mutableStateOf(avaliacao?.data ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var descricaoError by remember { mutableStateOf(false) }
    var descricaoErrorMsg by remember { mutableStateOf("") }
    var pesoError by remember { mutableStateOf(false) }
    var dataError by remember { mutableStateOf(false) }
    var dataErrorMsg by remember { mutableStateOf("") }
    var selectedTipo by remember { mutableStateOf(avaliacao?.tipo ?: "PROVA") }

    val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (data.isNotEmpty()) {
                dateFormat.parse(data)?.time ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            data = dateFormat.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = if (editando) "Editar Avaliação" else "Nova Avaliação",
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it; descricaoError = false; descricaoErrorMsg = "" },
                    label = { Text("Descrição") },
                    isError = descricaoError,
                    supportingText = if (descricaoError && descricaoErrorMsg.isNotEmpty()) {
                        { Text(descricaoErrorMsg, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Border,
                        focusedBorderColor = Primary,
                        unfocusedContainerColor = Background
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it; pesoError = false },
                    label = { Text("Peso") },
                    isError = pesoError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Border,
                        focusedBorderColor = Primary,
                        unfocusedContainerColor = Background
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tipo",
                    fontSize = 13.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    listOf("PROVA", "TRABALHO").forEach { tipo ->
                        FilterChip(
                            selected = selectedTipo == tipo,
                            onClick = { selectedTipo = tipo },
                            label = { Text(tipo, fontSize = 13.sp) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = data,
                    onValueChange = { data = it; dataError = false; dataErrorMsg = "" },
                    label = { Text("Data") },
                    isError = dataError,
                    supportingText = if (dataError && dataErrorMsg.isNotEmpty()) {
                        { Text(dataErrorMsg, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Selecionar data",
                            tint = Primary,
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Border,
                        focusedBorderColor = Primary,
                        unfocusedContainerColor = Background
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    descricaoError = descricao.length < 2 || descricao.length > 200
                    descricaoErrorMsg = when {
                        descricao.isBlank() -> "Descrição é obrigatória"
                        descricao.length < 2 -> "Mínimo de 2 caracteres"
                        descricao.length > 200 -> "Máximo de 200 caracteres"
                        else -> ""
                    }
                    pesoError = peso.toFloatOrNull() == null || (peso.toFloatOrNull() ?: 0f) <= 0f
                    dataError = data.isBlank() || !data.matches(dateRegex)
                    dataErrorMsg = when {
                        data.isBlank() -> "Data é obrigatória"
                        !data.matches(dateRegex) -> "Formato inválido (yyyy-MM-dd)"
                        else -> ""
                    }

                    if (!descricaoError && !pesoError && !dataError) {
                        onSalvar(descricao, peso.toFloat(), data, 0f, selectedTipo)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(if (editando) "Atualizar" else "Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}
