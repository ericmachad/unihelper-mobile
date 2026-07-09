package br.edu.utfpr.unihelper.documento.data.repository

import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.documento.data.remote.DocumentoApi
import br.edu.utfpr.unihelper.documento.data.remote.DocumentoResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

class DocumentoRepository(
    private val api: DocumentoApi
) {
    suspend fun listar(disciplinaId: String): Result<List<DocumentoResponse>> = safeApiCall {
        api.listar(disciplinaId)
    }

    suspend fun upload(
        disciplinaId: String,
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
        descricao: String?
    ): Result<DocumentoResponse> = safeApiCall {
        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("arquivo", fileName, requestBody)
        val descPart = descricao?.toRequestBody("text/plain".toMediaType())
        api.upload(disciplinaId, part, descPart)
    }

    suspend fun download(disciplinaId: String, id: String): Result<ResponseBody> = safeApiCall {
        api.download(disciplinaId, id)
    }

    suspend fun deletar(disciplinaId: String, id: String): Result<Unit> = safeApiCall {
        api.deletar(disciplinaId, id)
    }
}
