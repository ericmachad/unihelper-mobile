package br.edu.utfpr.unihelper.disciplina.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import br.edu.utfpr.unihelper.core.ui.ErrorDialogHandler
import br.edu.utfpr.unihelper.core.ui.SuccessDialogHandler
import br.edu.utfpr.unihelper.core.ui.UiEvent
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.SideEffect
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import br.edu.utfpr.unihelper.disciplina.data.remote.CriarDisciplinaRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.CriarHorarioRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.DiaSemana
import br.edu.utfpr.unihelper.ui.theme.Alert
import br.edu.utfpr.unihelper.ui.theme.Border
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Surface
import br.edu.utfpr.unihelper.ui.theme.TextGray
import org.koin.androidx.compose.koinViewModel

data class HorarioForm(
    var diaSemana: DiaSemana = DiaSemana.SEGUNDA,
    var horaInicio: String = "08:00",
    var horaFim: String = "09:00"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisciplinaFormScreen(
    onNavigateBack: () -> Unit,
    disciplinaId: String? = null,
    viewModel: DisciplinaViewModel = koinViewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
) {
    val formState by viewModel.formState.collectAsState()
    val disciplinaEditando by viewModel.disciplinaEditando.collectAsState()

    var nome by remember { mutableStateOf("") }
    var professor by remember { mutableStateOf("") }
    var bloco by remember { mutableStateOf("") }
    var cargaTotal by remember { mutableStateOf("") }
    var cargaSemanal by remember { mutableStateOf("") }
    var limiteFaltas by remember { mutableStateOf("") }
    var horarios = remember { mutableStateListOf(HorarioForm()) }
    val fieldErrors = remember { mutableStateMapOf<String, String?>() }
    val nomeTouched = remember { mutableStateOf(false) }
    val cargaTotalTouched = remember { mutableStateOf(false) }
    val cargaSemanalTouched = remember { mutableStateOf(false) }
    val limiteFaltasTouched = remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    var lastRemoval by remember { mutableStateOf<Pair<Int, HorarioForm>?>(null) }

    val isEditing = disciplinaId != null

    SideEffect {
        viewModel.limparFormState()
    }

    LaunchedEffect(disciplinaId) {
        if (disciplinaId != null) {
            viewModel.carregar(disciplinaId)
        } else {
            viewModel.limparEdicao()
        }
    }

    LaunchedEffect(disciplinaEditando) {
        disciplinaEditando?.let { d ->
            nome = d.nome
            professor = d.professor ?: ""
            bloco = d.bloco ?: ""
            cargaTotal = d.cargaHorariaTotal.toString()
            cargaSemanal = d.cargaHorariaSemanal.toString()
            limiteFaltas = d.limiteFaltas.toString()
            horarios.clear()
            if (d.horarios.isNotEmpty()) {
                d.horarios.forEach { h ->
                    horarios.add(
                        HorarioForm(
                            diaSemana = DiaSemana.fromInt(h.diaSemana) ?: DiaSemana.SEGUNDA,
                            horaInicio = h.horaInicio,
                            horaFim = h.horaFim
                        )
                    )
                }
            } else {
                horarios.add(HorarioForm())
            }
        }
    }

    val cargaTotalVal = cargaTotal.toIntOrNull() ?: 0
    val cargaSemanalVal = cargaSemanal.toIntOrNull() ?: 0

    val isFormValid = nome.isNotBlank() &&
            cargaTotalVal > 0 &&
            cargaSemanalVal > 0 &&
            cargaSemanalVal <= cargaTotalVal &&
            (limiteFaltas.toIntOrNull() ?: 0) > 0 &&
            (limiteFaltas.toIntOrNull() ?: 0) <= cargaTotalVal &&
            horarios.isNotEmpty() &&
            horarios.all { it.horaInicio.isNotBlank() && it.horaFim.isNotBlank() && it.horaFim > it.horaInicio } &&
            horarios.map { it.diaSemana }.distinct().size == horarios.size

    val validationError by remember {
        derivedStateOf {
            val ct = cargaTotal.toIntOrNull() ?: 0
            val cs = cargaSemanal.toIntOrNull() ?: 0
            val lf = limiteFaltas.toIntOrNull() ?: 0
            when {
                ct > 0 && cs > 0 && cs > ct -> "Carga horária semanal excede a carga total"
                ct > 0 && lf > ct -> "Limite de faltas não pode exceder a carga horária total"
                horarios.any { it.horaFim <= it.horaInicio } -> {
                    val idx = horarios.indexOfFirst { it.horaFim <= it.horaInicio }
                    "Horário ${idx + 1}: fim deve ser após o início"
                }
                horarios.map { it.diaSemana }.distinct().size != horarios.size -> "Horários com dias da semana duplicados"
                else -> null
            }
        }
    }

    fun validarNome(): Boolean {
        return when {
            nome.isBlank() -> {
                fieldErrors["nome"] = "Campo obrigatório"
                false
            }
            else -> {
                fieldErrors.remove("nome")
                true
            }
        }
    }

    fun validarCargaTotal(): Boolean {
        return when {
            cargaTotal.isBlank() -> {
                fieldErrors["cargaTotal"] = "Campo obrigatório"
                false
            }
            (cargaTotal.toIntOrNull() ?: 0) <= 0 -> {
                fieldErrors["cargaTotal"] = "Deve ser maior que zero"
                false
            }
            else -> {
                fieldErrors.remove("cargaTotal")
                true
            }
        }
    }

    fun validarCargaSemanal(): Boolean {
        return when {
            cargaSemanal.isBlank() -> {
                fieldErrors["cargaSemanal"] = "Campo obrigatório"
                false
            }
            (cargaSemanal.toIntOrNull() ?: 0) <= 0 -> {
                fieldErrors["cargaSemanal"] = "Deve ser maior que zero"
                false
            }
            else -> {
                fieldErrors.remove("cargaSemanal")
                true
            }
        }
    }

    fun validarLimiteFaltas(): Boolean {
        return when {
            limiteFaltas.isBlank() -> {
                fieldErrors["limiteFaltas"] = "Campo obrigatório"
                false
            }
            (limiteFaltas.toIntOrNull() ?: 0) <= 0 -> {
                fieldErrors["limiteFaltas"] = "Deve ser maior que zero"
                false
            }
            else -> {
                fieldErrors.remove("limiteFaltas")
                true
            }
        }
    }

    fun validate(): Boolean {
        fieldErrors.clear()
        val nomeValido = validarNome()
        val cargaTotalValida = validarCargaTotal()
        val cargaSemanalValida = validarCargaSemanal()
        val limiteFaltasValido = validarLimiteFaltas()
        return nomeValido && cargaTotalValida && cargaSemanalValida && limiteFaltasValido
    }

    LaunchedEffect(formState.sucesso) {
        if (formState.sucesso) onNavigateBack()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Snackbar -> snackbarHostState.showSnackbar(event.message)
                else -> { }
            }
        }
    }

    SuccessDialogHandler(uiEvent = viewModel.uiEvent)
    ErrorDialogHandler(uiEvent = viewModel.uiEvent)



    LaunchedEffect(lastRemoval) {
        lastRemoval?.let { (idx, horario) ->
            val result = snackbarHostState.showSnackbar(
                message = "Horário removido",
                actionLabel = "Desfazer",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                horarios.add(idx, horario)
            }
            lastRemoval = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Disciplina" else "Nova Disciplina",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it; fieldErrors.remove("nome") },
                label = { Text("Nome da Disciplina *") },
                isError = fieldErrors["nome"] != null,
                supportingText = fieldErrors["nome"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth().onFocusChanged { state ->
                    if (state.isFocused) nomeTouched.value = true
                    if (!state.isFocused && nomeTouched.value) validarNome()
                },
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = professor,
                onValueChange = { professor = it },
                label = { Text("Professor") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bloco,
                onValueChange = { bloco = it },
                label = { Text("Bloco / Sala") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cargaTotal,
                onValueChange = { cargaTotal = it.filter { c -> c.isDigit() }; fieldErrors.remove("cargaTotal") },
                label = { Text("Carga Horária Total *") },
                isError = fieldErrors["cargaTotal"] != null,
                supportingText = fieldErrors["cargaTotal"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth().onFocusChanged { state ->
                    if (state.isFocused) cargaTotalTouched.value = true
                    if (!state.isFocused && cargaTotalTouched.value) validarCargaTotal()
                },
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cargaSemanal,
                onValueChange = { cargaSemanal = it.filter { c -> c.isDigit() }; fieldErrors.remove("cargaSemanal") },
                label = { Text("Carga Horária Semanal *") },
                isError = fieldErrors["cargaSemanal"] != null,
                supportingText = fieldErrors["cargaSemanal"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth().onFocusChanged { state ->
                    if (state.isFocused) cargaSemanalTouched.value = true
                    if (!state.isFocused && cargaSemanalTouched.value) validarCargaSemanal()
                },
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = limiteFaltas,
                onValueChange = { limiteFaltas = it.filter { c -> c.isDigit() }; fieldErrors.remove("limiteFaltas") },
                label = { Text("Limite de Faltas *") },
                isError = fieldErrors["limiteFaltas"] != null,
                supportingText = fieldErrors["limiteFaltas"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth().onFocusChanged { state ->
                    if (state.isFocused) limiteFaltasTouched.value = true
                    if (!state.isFocused && limiteFaltasTouched.value) validarLimiteFaltas()
                },
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Horários",
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            horarios.forEachIndexed { index, horario ->
                HorarioCard(
                    horario = horario,
                    index = index,
                    total = horarios.size,
                    onUpdate = { i, h -> horarios[i] = h },
                    onRemove = { i ->
                        val removido = horarios[i]
                        horarios.removeAt(i)
                        lastRemoval = i to removido
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { horarios.add(HorarioForm()) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar Horário", color = Primary)
            }

            validationError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!validate()) return@Button
                    val request = CriarDisciplinaRequest(
                        nome = nome.trim(),
                        professor = professor.trim().ifBlank { null },
                        bloco = bloco.trim().ifBlank { null },
                        cargaHorariaTotal = cargaTotal.toIntOrNull() ?: 0,
                        cargaHorariaSemanal = cargaSemanal.toIntOrNull() ?: 0,
                        limiteFaltas = limiteFaltas.toIntOrNull() ?: 0,
                        horarios = horarios.map { h ->
                            CriarHorarioRequest(
                                diaSemana = h.diaSemana.valor,
                                horaInicio = h.horaInicio,
                                horaFim = h.horaFim
                            )
                        }
                    )
                    if (isEditing) {
                        viewModel.atualizar(disciplinaId!!, request)
                    } else {
                        viewModel.criar(request)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                ),
                enabled = isFormValid && !formState.isLoading
            ) {
                if (formState.isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "Salvar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Color(0xFFE5E7EB),
    focusedBorderColor = Primary,
    unfocusedContainerColor = Color(0xFFF9FAFB),
    focusedContainerColor = Color(0xFFF9FAFB),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onTimeChanged: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    val pickerState = rememberTimePickerState(
        initialHour = (value.substringBefore(":").toIntOrNull() ?: 8).coerceIn(0, 23),
        initialMinute = (value.substringAfter(":").toIntOrNull() ?: 0).coerceIn(0, 59),
        is24Hour = true
    )

    LaunchedEffect(value) {
        val h = (value.substringBefore(":").toIntOrNull() ?: 8).coerceIn(0, 23)
        val m = (value.substringAfter(":").toIntOrNull() ?: 0).coerceIn(0, 59)
        pickerState.hour = h
        pickerState.minute = m
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Selecionar Horário") },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val h = pickerState.hour.toString().padStart(2, '0')
                    val m = pickerState.minute.toString().padStart(2, '0')
                    onTimeChanged("$h:$m")
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        )
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors(),
            singleLine = true
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorarioCard(
    horario: HorarioForm,
    index: Int,
    total: Int,
    onUpdate: (Int, HorarioForm) -> Unit,
    onRemove: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onRemove(index)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Alert, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remover",
                        tint = Color.White
                    )
                    Text(
                        "Remover horário",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        content = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = horario.diaSemana.abrev,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Dia") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DiaSemana.entries.forEach { dia ->
                                    DropdownMenuItem(
                                        text = { Text(dia.abrev, maxLines = 1) },
                                        onClick = {
                                            onUpdate(index, horario.copy(diaSemana = dia))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (total > 1) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { onRemove(index) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remover horário",
                                    tint = Alert
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimeField(
                            value = horario.horaInicio.ifBlank { "08:00" },
                            label = "Início",
                            modifier = Modifier.weight(1f),
                            onTimeChanged = { onUpdate(index, horario.copy(horaInicio = it)) }
                        )
                        TimeField(
                            value = horario.horaFim.ifBlank { "08:00" },
                            label = "Fim",
                            modifier = Modifier.weight(1f),
                            onTimeChanged = { onUpdate(index, horario.copy(horaFim = it)) }
                        )
                    }
                }
            }
        }
    )
}
