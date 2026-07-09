package br.edu.utfpr.unihelper.documento.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class DocumentoResponse(
    val id: String,
    val nomeArquivo: String,
    val mimeType: String,
    val tamanhoBytes: Long,
    val descricao: String? = null,
    val criadoEm: String
)
