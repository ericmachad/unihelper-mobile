package br.edu.utfpr.unihelper.documento.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.network.toErrorDialog
import br.edu.utfpr.unihelper.core.ui.UiEvent
import br.edu.utfpr.unihelper.documento.data.local.DocumentoEntity
import br.edu.utfpr.unihelper.documento.data.remote.DocumentoResponse
import br.edu.utfpr.unihelper.documento.data.repository.DocumentoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocumentoUiState(
    val documentos: List<DocumentoResponse> = emptyList(),
    val isLoading: Boolean = false,
    val uploadProgress: Boolean = false,
    val downloadBytes: ByteArray? = null,
    val downloadMimeType: String? = null,
    val downloadNome: String? = null
)

data class DocumentoDeleteState(
    val isLoading: Boolean = false,
    val sucesso: Boolean = false
)

class DocumentoViewModel(
    private val repository: DocumentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentoUiState())
    val uiState: StateFlow<DocumentoUiState> = _uiState.asStateFlow()

    private val _deleteState = MutableStateFlow(DocumentoDeleteState())
    val deleteState: StateFlow<DocumentoDeleteState> = _deleteState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var documentosFlowJob: Job? = null
    private var currentDisciplinaId: String? = null

    fun carregarDocumentos(disciplinaId: String) {
        if (disciplinaId == currentDisciplinaId) return
        currentDisciplinaId = disciplinaId

        documentosFlowJob?.cancel()
        documentosFlowJob = viewModelScope.launch {
            repository.listarFlow(disciplinaId).collect { entities ->
                _uiState.update {
                    it.copy(documentos = entities.map { entity -> entity.toResponse() }, isLoading = false)
                }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.listar(disciplinaId)
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.tryEmit(UiEvent.Snackbar("Não foi possível atualizar. Dados offline exibidos."))
                }
        }
    }

    fun upload(context: Context, disciplinaId: String, uri: Uri, descricao: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(uploadProgress = true)
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch
                inputStream.close()

                val fileName = obterNomeArquivo(context, uri) ?: "arquivo"
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                repository.upload(disciplinaId, bytes, fileName, mimeType, descricao)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(uploadProgress = false)
                        _uiEvent.tryEmit(UiEvent.SuccessDialog("Documento enviado"))
                        carregarDocumentos(disciplinaId)
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(uploadProgress = false)
                        if (error is ApiException && error.status == 0) {
                            _uiEvent.tryEmit(UiEvent.SuccessDialog("Documento enviado"))
                            carregarDocumentos(disciplinaId)
                        } else {
                            _uiEvent.tryEmit(error.toErrorDialog())
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(uploadProgress = false)
                _uiEvent.tryEmit(UiEvent.Snackbar(e.message ?: "Erro ao ler arquivo"))
            }
        }
    }

    fun download(disciplinaId: String, doc: DocumentoResponse) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.download(disciplinaId, doc.id)
                .onSuccess { body ->
                    _uiState.value = _uiState.value.copy(
                        downloadBytes = body.bytes(),
                        downloadMimeType = doc.mimeType,
                        downloadNome = doc.nomeArquivo,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.tryEmit(it.toErrorDialog())
                }
        }
    }

    fun limparDownload() {
        _uiState.value = _uiState.value.copy(
            downloadBytes = null, downloadMimeType = null, downloadNome = null
        )
    }

    fun deletar(disciplinaId: String, documentoId: String) {
        viewModelScope.launch {
            _deleteState.value = DocumentoDeleteState(isLoading = true)
            repository.deletar(disciplinaId, documentoId)
                .onSuccess {
                    _deleteState.value = DocumentoDeleteState(sucesso = true)
                    _uiEvent.tryEmit(UiEvent.SuccessDialog("Documento excluído"))
                }
                .onFailure { error ->
                    _deleteState.value = DocumentoDeleteState()
                    if (error is ApiException && error.status == 0) {
                        _uiEvent.tryEmit(UiEvent.SuccessDialog("Documento excluído"))
                    } else {
                        _uiEvent.tryEmit(error.toErrorDialog())
                    }
                }
        }
    }

    fun limparDeleteState() {
        _deleteState.value = DocumentoDeleteState()
    }

    private fun obterNomeArquivo(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            if (nameIndex >= 0) it.getString(nameIndex) else null
        }
    }
}

private fun DocumentoEntity.toResponse() = DocumentoResponse(
    id = id,
    nomeArquivo = nomeArquivo,
    mimeType = mimeType,
    tamanhoBytes = tamanhoBytes,
    descricao = descricao,
    criadoEm = criadoEm
)
