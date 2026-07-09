package br.edu.utfpr.unihelper.documento.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.documento.data.remote.DocumentoResponse
import br.edu.utfpr.unihelper.documento.data.repository.DocumentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocumentoUiState(
    val documentos: List<DocumentoResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadProgress: Boolean = false,
    val downloadBytes: ByteArray? = null,
    val downloadMimeType: String? = null,
    val downloadNome: String? = null
)

data class DocumentoDeleteState(
    val isLoading: Boolean = false,
    val sucesso: Boolean = false,
    val error: String? = null
)

class DocumentoViewModel(
    private val repository: DocumentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentoUiState())
    val uiState: StateFlow<DocumentoUiState> = _uiState.asStateFlow()

    private val _deleteState = MutableStateFlow(DocumentoDeleteState())
    val deleteState: StateFlow<DocumentoDeleteState> = _deleteState.asStateFlow()

    fun carregarDocumentos(disciplinaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.listar(disciplinaId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        documentos = it, isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message, isLoading = false
                    )
                }
        }
    }

    fun upload(context: Context, disciplinaId: String, uri: Uri, descricao: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(uploadProgress = true, error = null)
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch
                inputStream.close()

                val fileName = obterNomeArquivo(context, uri) ?: "arquivo"
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                repository.upload(disciplinaId, bytes, fileName, mimeType, descricao)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(uploadProgress = false)
                        carregarDocumentos(disciplinaId)
                    }
                    .onFailure {
                        _uiState.value = _uiState.value.copy(
                            error = it.message, uploadProgress = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Erro ao ler arquivo", uploadProgress = false
                )
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
                    _uiState.value = _uiState.value.copy(
                        error = it.message, isLoading = false
                    )
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
                    carregarDocumentos(disciplinaId)
                }
                .onFailure {
                    _deleteState.value = DocumentoDeleteState(error = it.message)
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
