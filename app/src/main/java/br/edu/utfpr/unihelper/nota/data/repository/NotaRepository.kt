package br.edu.utfpr.unihelper.nota.data.repository

import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.nota.data.remote.NotaApi
import br.edu.utfpr.unihelper.nota.data.remote.NotaRequest
import br.edu.utfpr.unihelper.nota.data.remote.NotaResponse

class NotaRepository(
    private val api: NotaApi
) {
    suspend fun listar(disciplinaId: String): Result<List<NotaResponse>> = safeApiCall {
        api.listar(disciplinaId)
    }

    suspend fun buscar(disciplinaId: String, id: String): Result<NotaResponse> = safeApiCall {
        api.buscar(disciplinaId, id)
    }

    suspend fun criar(disciplinaId: String, request: NotaRequest): Result<NotaResponse> = safeApiCall {
        api.criar(disciplinaId, request)
    }

    suspend fun atualizar(disciplinaId: String, id: String, request: NotaRequest): Result<NotaResponse> = safeApiCall {
        api.atualizar(disciplinaId, id, request)
    }

    suspend fun excluir(disciplinaId: String, id: String): Result<Unit> = safeApiCall {
        api.excluir(disciplinaId, id)
    }

    suspend fun buscarPorTitulo(disciplinaId: String, termo: String): Result<List<NotaResponse>> = safeApiCall {
        api.buscarPorTitulo(disciplinaId, termo)
    }
}
