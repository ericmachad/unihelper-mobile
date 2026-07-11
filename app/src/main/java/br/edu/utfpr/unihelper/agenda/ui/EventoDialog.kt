package br.edu.utfpr.unihelper.agenda.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import br.edu.utfpr.unihelper.agenda.data.local.EventoEntity
import br.edu.utfpr.unihelper.disciplina.data.remote.DiaSemana
import br.edu.utfpr.unihelper.disciplina.data.remote.HorarioResponse
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Border
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.TextGray
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoDialog(
    totalPesosAtual: Float,
    horarios: List<HorarioResponse> = emptyList(),
    evento: EventoEntity? = null,
    onSalvar: (descricao: String, peso: Float, data: String, horaInicio: String, horaFim: String, valor: Float?, tipo: String) -> Unit,
    onDismiss: () -> Unit
) {
    val editando = evento != null
    var descricao by remember { mutableStateOf(evento?.titulo ?: "") }
    var peso by remember { mutableStateOf(evento?.peso?.toString() ?: "") }
    val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val dataInicial = evento?.dataHoraInicio?.take(10)?.let {
        try { LocalDate.parse(it).format(displayFormatter) } catch (_: Exception) { it }
    } ?: ""

    var data by remember { mutableStateOf(dataInicial) }
    val horaInicial = if (evento != null) {
        evento.dataHoraInicio.substringAfter("T").substringBefore(":")
    } else ""
    val minInicial = if (evento != null) {
        evento.dataHoraInicio.substringAfter(":").substringBefore(":")
    } else ""

    val horarioPadrao = remember(evento, horarios) {
        if (evento == null && horarios.isNotEmpty()) {
            val hoje = LocalDate.now()
            val diaSemanaValor = when (hoje.dayOfWeek) {
                DayOfWeek.MONDAY -> 2
                DayOfWeek.TUESDAY -> 3
                DayOfWeek.WEDNESDAY -> 4
                DayOfWeek.THURSDAY -> 5
                DayOfWeek.FRIDAY -> 6
                else -> null
            }
            diaSemanaValor?.let { valor -> horarios.find { it.diaSemana == valor } }
        } else null
    }

    var horaInicio by remember { mutableStateOf(
        if (evento != null) "$horaInicial:$minInicial"
        else horarioPadrao?.horaInicio ?: ""
    ) }
    var horaFim by remember { mutableStateOf(
        if (evento != null) evento.dataHoraFim.take(5)
        else horarioPadrao?.horaFim ?: ""
    ) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePickerInicio by remember { mutableStateOf(false) }
    var showTimePickerFim by remember { mutableStateOf(false) }
    var descricaoError by remember { mutableStateOf(false) }
    var descricaoErrorMsg by remember { mutableStateOf("") }
    var pesoError by remember { mutableStateOf(false) }
    var pesoErrorMsg by remember { mutableStateOf("") }
    var dataError by remember { mutableStateOf(false) }
    var dataErrorMsg by remember { mutableStateOf("") }
    var selectedTipo by remember { mutableStateOf(evento?.tipo ?: "PROVA") }

    val dateRegex = Regex("""^\d{2}/\d{2}/\d{4}$""")
    val timeRegex = Regex("""^\d{2}:\d{2}$""")

    fun preencherHorarioDaData(dataStr: String) {
        if (horarios.isEmpty()) return
        try {
            val date = LocalDate.parse(dataStr)
            val diaSemanaValor = when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> 2
                DayOfWeek.TUESDAY -> 3
                DayOfWeek.WEDNESDAY -> 4
                DayOfWeek.THURSDAY -> 5
                DayOfWeek.FRIDAY -> 6
                else -> return
            }
            val horario = horarios.find { it.diaSemana == diaSemanaValor }
            if (horario != null) {
                horaInicio = horario.horaInicio
                horaFim = horario.horaFim
            }
        } catch (_: Exception) { }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (data.isNotEmpty()) {
                try {
                    LocalDate.parse(data, displayFormatter).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }
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
                            val localDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            data = localDate.format(displayFormatter)
                            preencherHorarioDaData(localDate.toString())
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

    if (showTimePickerInicio) {
        val h = horaInicio.split(":").getOrNull(0)?.toIntOrNull() ?: 8
        val m = horaInicio.split(":").getOrNull(1)?.toIntOrNull() ?: 0
        val timePickerState = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePickerInicio = false },
            title = { Text("Hora Início") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    horaInicio = "${"%02d".format(timePickerState.hour)}:%02d".format(timePickerState.minute)
                    showTimePickerInicio = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerInicio = false }) { Text("Cancelar") }
            }
        )
    }

    if (showTimePickerFim) {
        val h = horaFim.split(":").getOrNull(0)?.toIntOrNull() ?: 9
        val m = horaFim.split(":").getOrNull(1)?.toIntOrNull() ?: 0
        val timePickerState = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePickerFim = false },
            title = { Text("Hora Fim") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    horaFim = "${"%02d".format(timePickerState.hour)}:%02d".format(timePickerState.minute)
                    showTimePickerFim = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerFim = false }) { Text("Cancelar") }
            }
        )
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
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it; descricaoError = false; descricaoErrorMsg = "" },
                    label = { Text("Descrição *") },
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
                    onValueChange = { peso = it; pesoError = false; pesoErrorMsg = "" },
                    label = { Text("Peso *") },
                    isError = pesoError,
                    supportingText = if (pesoError && pesoErrorMsg.isNotEmpty()) {
                        { Text(pesoErrorMsg, color = MaterialTheme.colorScheme.error) }
                    } else null,
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
                    onValueChange = { novaData ->
                        data = novaData; dataError = false; dataErrorMsg = ""
                        if (novaData.matches(dateRegex)) {
                            try {
                                val localDate = LocalDate.parse(novaData, displayFormatter)
                                preencherHorarioDaData(localDate.toString())
                            } catch (_: Exception) { }
                        }
                    },
                    label = { Text("Data *") },
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = { horaInicio = it },
                        label = { Text("Início") },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Border,
                            focusedBorderColor = Primary,
                            unfocusedContainerColor = Background
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showTimePickerInicio = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Alterar horário", tint = Primary)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = horaFim,
                        onValueChange = { horaFim = it },
                        label = { Text("Fim") },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Border,
                            focusedBorderColor = Primary,
                            unfocusedContainerColor = Background
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showTimePickerFim = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Alterar horário", tint = Primary)
                            }
                        }
                    )
                }
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
                    val pesoFloat = peso.toFloatOrNull()
                    val pesoInvalido = pesoFloat == null || pesoFloat <= 0f
                    val excedeLimite = pesoFloat != null && pesoFloat + totalPesosAtual > 10f
                    pesoError = pesoInvalido || excedeLimite
                    pesoErrorMsg = when {
                        pesoInvalido -> "Peso deve ser maior que zero"
                        excedeLimite -> "Soma dos pesos ultrapassa o limite de 10 (atual: ${"%.1f".format(totalPesosAtual)}, disponível: ${"%.1f".format(10f - totalPesosAtual)})"
                        else -> ""
                    }
                    dataError = data.isBlank() || !data.matches(dateRegex)
                    dataErrorMsg = when {
                        data.isBlank() -> "Data é obrigatória"
                        !data.matches(dateRegex) -> "Formato inválido (dd/MM/yyyy)"
                        else -> ""
                    }

                    if (!descricaoError && !pesoError && !dataError) {
                        val dataApi = try {
                            LocalDate.parse(data, displayFormatter).toString()
                        } catch (_: Exception) { data }
                        val hInicio = horaInicio.ifBlank { "08:00" }
                        val hFim = horaFim.ifBlank { "09:00" }
                        onSalvar(descricao, peso.toFloat(), dataApi, hInicio, hFim, null, selectedTipo)
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
