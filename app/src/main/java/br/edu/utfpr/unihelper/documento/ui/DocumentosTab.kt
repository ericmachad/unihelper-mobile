package br.edu.utfpr.unihelper.documento.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaResponse
import br.edu.utfpr.unihelper.documento.data.remote.DocumentoResponse
import br.edu.utfpr.unihelper.nota.ui.NotaViewModel
import br.edu.utfpr.unihelper.nota.ui.NotasSection
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.TextGray
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosTab(
    disciplinas: List<DisciplinaResponse>,
    isLoadingDisciplinas: Boolean
) {
    val context = LocalContext.current
    val documentoVM: DocumentoViewModel = koinViewModel()
    val notaVM: NotaViewModel = koinViewModel()
    val docState by documentoVM.uiState.collectAsState()
    val deleteState by documentoVM.deleteState.collectAsState()

    var selectedDisciplinaId by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    val descricaoUpload = remember { mutableStateOf<String?>(null) }

    val selectedDisciplina = disciplinas.find { it.id == selectedDisciplinaId }

    LaunchedEffect(selectedDisciplinaId) {
        selectedDisciplinaId?.let { id ->
            documentoVM.carregarDocumentos(id)
            notaVM.carregarNotas(id)
        }
    }

    LaunchedEffect(deleteState.sucesso) {
        if (deleteState.sucesso) {
            documentoVM.limparDeleteState()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && selectedDisciplinaId != null) {
            documentoVM.upload(context, selectedDisciplinaId!!, uri, descricaoUpload.value)
        }
    }

    val downloadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            docState.downloadBytes?.let { bytes ->
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                documentoVM.limparDownload()
            }
        }
    }

    LaunchedEffect(docState.downloadBytes) {
        docState.downloadBytes?.let { bytes ->
            val mime = docState.downloadMimeType ?: "application/octet-stream"
            val nome = docState.downloadNome ?: "documento"
            downloadLauncher.launch(nome)
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Excluir Documento") },
            text = { Text("Tem certeza que deseja excluir este documento?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedDisciplinaId != null && showDeleteDialog != null) {
                            documentoVM.deletar(selectedDisciplinaId!!, showDeleteDialog!!)
                        }
                        showDeleteDialog = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedDisciplina?.nome ?: "Selecione uma disciplina",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                disciplinas.forEach { disc ->
                    DropdownMenuItem(
                        text = { Text(disc.nome) },
                        onClick = {
                            selectedDisciplinaId = disc.id
                            expanded = false
                        }
                    )
                }
            }
        }

        if (selectedDisciplinaId == null) {
            Spacer(modifier = Modifier.height(48.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Selecione uma disciplina acima",
                    fontSize = 14.sp,
                    color = TextGray
                )
            }
            return@Column
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Documentos",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (docState.uploadProgress) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }

        if (docState.isLoading) {
            Text(
                text = "Carregando...",
                fontSize = 13.sp,
                color = TextGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else if (docState.documentos.isEmpty()) {
            Text(
                text = "Nenhum documento",
                fontSize = 13.sp,
                color = TextGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            docState.documentos.forEach { doc ->
                DocumentoCard(
                    documento = doc,
                    onClick = { documentoVM.download(selectedDisciplinaId!!, doc) },
                    onDelete = { showDeleteDialog = doc.id }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            onClick = { filePickerLauncher.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Upload de PDF, JPEG ou PNG", color = Primary, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        NotasSection(
            disciplinaId = selectedDisciplinaId,
            viewModel = notaVM,
            onNotaClicada = {}
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DocumentoCard(
    documento: DocumentoResponse,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val icone: ImageVector = when {
        documento.mimeType.contains("pdf") -> Icons.Default.PictureAsPdf
        documento.mimeType.contains("image") -> Icons.Default.Image
        else -> Icons.Default.Description
    }

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
                imageVector = icone,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = documento.nomeArquivo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val tamanho = when {
                    documento.tamanhoBytes < 1024 -> "${documento.tamanhoBytes} B"
                    documento.tamanhoBytes < 1024 * 1024 -> "${documento.tamanhoBytes / 1024} KB"
                    else -> "%.1f MB".format(documento.tamanhoBytes / (1024.0 * 1024.0))
                }
                Text(
                    text = if (documento.descricao != null) "${documento.descricao} · $tamanho" else tamanho,
                    fontSize = 11.sp,
                    color = TextGray,
                    maxLines = 1
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = TextGray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
