package br.edu.utfpr.unihelper.disciplina.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.agenda.ui.BlocoEventos
import br.edu.utfpr.unihelper.agenda.ui.EventosDisciplinaViewModel
import br.edu.utfpr.unihelper.core.ui.ErrorDialogHandler
import br.edu.utfpr.unihelper.core.ui.SuccessDialogHandler
import br.edu.utfpr.unihelper.core.ui.UiEvent
import br.edu.utfpr.unihelper.disciplina.data.remote.DiaSemana
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaResponse
import kotlinx.coroutines.flow.collectLatest
import br.edu.utfpr.unihelper.ui.theme.Alert
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Border
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Surface
import br.edu.utfpr.unihelper.ui.theme.TextGray
import org.koin.androidx.compose.koinViewModel

private val coresDisciplinas = listOf(
    Color(0xFF3B82F6),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899),
    Color(0xFFF59E0B),
    Color(0xFF10B981)
)

private fun corDisciplina(nome: String): Color {
    val index = nome.hashCode().let { if (it == Int.MIN_VALUE) 0 else kotlin.math.abs(it) } % coresDisciplinas.size
    return coresDisciplinas[index]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisciplinaDetalheScreen(
    disciplinaId: String,
    onNavigateBack: () -> Unit,
    viewModel: DisciplinaViewModel = koinViewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val disciplina = uiState.disciplinas.find { it.id == disciplinaId }

    val eventosViewModel: EventosDisciplinaViewModel = koinViewModel()
    val eventosState by eventosViewModel.uiState.collectAsState()

    LaunchedEffect(deleteState.sucesso) {
        if (deleteState.sucesso) {
            viewModel.limparDeleteState()
            onNavigateBack()
        }
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Disciplina") },
            text = { Text("Tem certeza que deseja excluir \"${disciplina?.nome ?: ""}\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.excluir(disciplinaId)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Alert)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = disciplina?.nome ?: "Disciplina",
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
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Disciplina",
                            tint = Alert
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface
                )
            )
        }
    ) { padding ->
        if (disciplina == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Disciplina não encontrada",
                    fontSize = 14.sp,
                    color = TextGray
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(corDisciplina(disciplina.nome)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = Surface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = disciplina.nome,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (disciplina.professor != null) {
                                Text(
                                    text = disciplina.professor,
                                    fontSize = 12.sp,
                                    color = TextGray
                                )
                            }
                            Text(
                                text = "${disciplina.faltasRegistradas} falta(s) · ${disciplina.limiteFaltas} limite",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                        disciplina.bloco?.let { bloco ->
                            BlocoChip(text = bloco)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoItem(
                            label = "Carga Total",
                            value = "${disciplina.cargaHorariaTotal}h",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        InfoItem(
                            label = "Carga Semanal",
                            value = "${disciplina.cargaHorariaSemanal}h",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoItem(
                            label = "Limite de Faltas",
                            value = "${disciplina.limiteFaltas}",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        InfoItem(
                            label = "Média Atual",
                            value = if (eventosState.media != null) "%.1f".format(eventosState.media) else "--",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val isFaltaLoading = disciplinaId in uiState.faltasAtualizando

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Faltas",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { viewModel.alterarFaltas(disciplinaId, "DECREMENTAR") },
                            enabled = !isFaltaLoading && disciplina.faltasRegistradas > 0,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Remover falta",
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.alterarFaltas(disciplinaId, "INCREMENTAR") },
                            enabled = !isFaltaLoading && disciplina.faltasRegistradas < disciplina.cargaHorariaTotal,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Registrar falta",
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${disciplina.faltasRegistradas} / ${disciplina.limiteFaltas}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (disciplina.limiteFaltas > 0)
                                (disciplina.faltasRegistradas.toFloat() / disciplina.limiteFaltas.toFloat()).coerceIn(0f, 1f)
                            else 0f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (disciplina.faltasCriticas) Alert else Primary,
                        trackColor = (if (disciplina.faltasCriticas) Alert else Primary).copy(alpha = 0.15f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Horários",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (disciplina.horarios.isEmpty()) {
                Text(
                    text = "Nenhum horário cadastrado",
                    fontSize = 13.sp,
                    color = TextGray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                disciplina.horarios.forEach { horario ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Background)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${DiaSemana.fromInt(horario.diaSemana)?.label ?: "?"}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(70.dp)
                            )
                            Text(
                                text = "${horario.horaInicio} - ${horario.horaFim}",
                                fontSize = 14.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            BlocoEventos(
                disciplinaId = disciplinaId,
                horarios = disciplina.horarios,
                viewModel = eventosViewModel
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Background, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BlocoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Primary.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Primary
        )
    }
}
