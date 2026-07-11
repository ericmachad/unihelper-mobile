package br.edu.utfpr.unihelper.agenda.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.agenda.data.remote.AgendaItemResponse
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaResponse
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Border
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Surface
import br.edu.utfpr.unihelper.ui.theme.TextGray
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoBottomSheet(
    eventoParaEditar: AgendaItemResponse?,
    disciplinas: List<DisciplinaResponse>,
    onSalvar: (titulo: String, tipo: String, dataHoraInicio: String, dataHoraFim: String, peso: Float?, disciplinaId: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = eventoParaEditar != null
    val dtInicio = if (isEditing) {
        try { LocalDateTime.parse(eventoParaEditar!!.dataHora) } catch (_: Exception) { LocalDateTime.now() }
    } else LocalDateTime.now()
    val dtFim = if (isEditing && eventoParaEditar!!.dataHoraFim != null) {
        try { LocalDateTime.parse(eventoParaEditar!!.dataHoraFim) } catch (_: Exception) { dtInicio.plusHours(1) }
    } else dtInicio.plusHours(1)

    var titulo by remember { mutableStateOf(eventoParaEditar?.titulo ?: "") }
    var tipo by remember { mutableStateOf(eventoParaEditar?.tipoEvento ?: "OUTRO") }
    var disciplinaId by remember { mutableStateOf(eventoParaEditar?.disciplinaId) }
    var data by remember { mutableStateOf(dtInicio.format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var horaInicio by remember { mutableStateOf(dtInicio.format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var horaFim by remember { mutableStateOf(dtFim.format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var tituloError by remember { mutableStateOf(false) }
    var pesoText by remember { mutableStateOf(eventoParaEditar?.peso?.toString() ?: "") }
    var pesoError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePickerInicio by remember { mutableStateOf(false) }
    var showTimePickerFim by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tipos = listOf("PROVA", "TRABALHO", "OUTRO")
    val precisaDisciplina = tipo == "PROVA" || tipo == "TRABALHO"
    val displayDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                LocalDate.parse(data).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        data = localDate.toString()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePickerInicio) {
        Material3TimePickerDialog(
            initialHour = horaInicio.split(":").getOrNull(0)?.toIntOrNull() ?: 8,
            initialMinute = horaInicio.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
            onConfirm = { h, m ->
                horaInicio = String.format("%02d:%02d", h, m)
                showTimePickerInicio = false
            },
            onDismiss = { showTimePickerInicio = false }
        )
    }

    if (showTimePickerFim) {
        Material3TimePickerDialog(
            initialHour = horaFim.split(":").getOrNull(0)?.toIntOrNull() ?: 9,
            initialMinute = horaFim.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
            onConfirm = { h, m ->
                horaFim = String.format("%02d:%02d", h, m)
                showTimePickerFim = false
            },
            onDismiss = { showTimePickerFim = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (isEditing) "Editar Evento" else "Novo Evento",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it; tituloError = false },
                label = { Text("Título *") },
                isError = tituloError,
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

            TipoDropdown(
                selected = tipo,
                onSelected = { tipo = it; if (!precisaDisciplina) disciplinaId = null },
                items = tipos
            )

            if (precisaDisciplina) {
                Spacer(modifier = Modifier.height(12.dp))
                DisciplinaDropdown(
                    selectedId = disciplinaId,
                    onSelected = { disciplinaId = it },
                    items = disciplinas
                )
            }

            if (precisaDisciplina) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pesoText,
                    onValueChange = { pesoText = it; pesoError = false },
                    label = { Text("Peso *") },
                    isError = pesoError,
                    supportingText = if (pesoError) {{ Text("Peso deve ser maior que zero") }} else null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Border,
                        focusedBorderColor = Primary,
                        unfocusedContainerColor = Background,
                        focusedContainerColor = Surface
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = try {
                        LocalDate.parse(data).format(displayDateFormat)
                    } catch (_: Exception) { data },
                    onValueChange = {},
                    label = { Text("Data *") },
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Border,
                        focusedBorderColor = Primary,
                        unfocusedContainerColor = Background
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = {},
                        label = { Text("Início *") },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Border,
                            focusedBorderColor = Primary,
                            unfocusedContainerColor = Background
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showTimePickerInicio = true }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = horaFim,
                        onValueChange = {},
                        label = { Text("Fim *") },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Border,
                            focusedBorderColor = Primary,
                            unfocusedContainerColor = Background
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showTimePickerFim = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (titulo.isBlank()) {
                        tituloError = true
                        return@Button
                    }
                    if (precisaDisciplina) {
                        val pesoNum = pesoText.toFloatOrNull()
                        if (pesoNum == null || pesoNum <= 0f) {
                            pesoError = true
                            return@Button
                        }
                    }
                    val peso = if (precisaDisciplina) pesoText.toFloatOrNull() else null
                    onSalvar(titulo, tipo, "${data}T${horaInicio}:00", "${data}T${horaFim}:00", peso, disciplinaId)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Salvar" else "Criar", modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TipoDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    items: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Border,
                focusedBorderColor = Primary,
                unfocusedContainerColor = Background
            ),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = { onSelected(item); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Material3TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Selecionar horário", fontWeight = FontWeight.Bold, color = Primary) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("OK", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisciplinaDropdown(
    selectedId: String?,
    onSelected: (String?) -> Unit,
    items: List<DisciplinaResponse>
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedNome = items.find { it.id == selectedId }?.nome ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedNome,
            onValueChange = {},
            readOnly = true,
            label = { Text("Disciplina *") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Border,
                focusedBorderColor = Primary,
                unfocusedContainerColor = Background
            ),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.nome) },
                    onClick = { onSelected(item.id); expanded = false }
                )
            }
        }
    }
}
