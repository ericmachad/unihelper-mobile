package br.edu.utfpr.unihelper.documento.data.repository

import br.edu.utfpr.unihelper.core.local.SyncStatus
import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.documento.data.local.DocumentoDao
import br.edu.utfpr.unihelper.documento.data.local.DocumentoEntity
import br.edu.utfpr.unihelper.documento.data.remote.DocumentoApi
import br.edu.utfpr.unihelper.documento.data.remote.DocumentoResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import kotlinx.coroutines.flow.Flow

class DocumentoRepository(
    private val api: DocumentoApi,
    private val dao: DocumentoDao
) {
    fun listarFlow(disciplinaId: String): Flow<List<DocumentoEntity>> = dao.listar(disciplinaId)

    suspend fun listar(disciplinaId: String): Result<List<DocumentoResponse>> {
        return safeApiCall { api.listar(disciplinaId) }.onSuccess { docs ->
            dao.deletarPorDisciplina(disciplinaId)
            dao.inserirTodas(docs.map { it.toEntity(disciplinaId) })
        }
    }

    suspend fun upload(
        disciplinaId: String,
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
        descricao: String?
    ): Result<DocumentoResponse> {
        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("arquivo", fileName, requestBody)
        val descPart = descricao?.toRequestBody("text/plain".toMediaType())
        return safeApiCall { api.upload(disciplinaId, part, descPart) }.onSuccess { doc ->
            dao.inserir(doc.toEntity(disciplinaId))
        }
    }

    suspend fun download(disciplinaId: String, id: String): Result<ResponseBody> = safeApiCall {
        api.download(disciplinaId, id)
    }

    suspend fun deletar(disciplinaId: String, id: String): Result<Unit> {
        return safeApiCall { api.deletar(disciplinaId, id) }.map { }.onSuccess {
            dao.deletarPorId(id)
        }
    }
}

private fun DocumentoResponse.toEntity(disciplinaId: String) = DocumentoEntity(
    id = id,
    nomeArquivo = nomeArquivo,
    mimeType = mimeType,
    tamanhoBytes = tamanhoBytes,
    descricao = descricao,
    criadoEm = criadoEm,
    disciplinaId = disciplinaId,
    syncStatus = SyncStatus.SYNCED
)
