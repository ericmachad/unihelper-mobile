package br.edu.utfpr.unihelper.disciplina.data.repository

import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.disciplina.data.remote.AlterarFaltasRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.CriarDisciplinaRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaApi
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaResponse

class DisciplinaRepository(
    private val api: DisciplinaApi
) {
    suspend fun listar(): Result<List<DisciplinaResponse>> = safeApiCall {
        api.listar()
    }

    suspend fun buscarPorId(id: String): Result<DisciplinaResponse> = safeApiCall {
        api.buscarPorId(id)
    }

    suspend fun criar(request: CriarDisciplinaRequest): Result<DisciplinaResponse> = safeApiCall {
        api.criar(request)
    }

    suspend fun atualizar(
        id: String,
        request: CriarDisciplinaRequest
    ): Result<DisciplinaResponse> = safeApiCall {
        api.atualizar(id, request)
    }

    suspend fun excluir(id: String): Result<Unit> = safeApiCall {
        api.excluir(id)
    }

    suspend fun alterarFaltas(
        id: String,
        operacao: String
    ): Result<DisciplinaResponse> = safeApiCall {
        api.alterarFaltas(id, AlterarFaltasRequest(operacao))
    }
}
