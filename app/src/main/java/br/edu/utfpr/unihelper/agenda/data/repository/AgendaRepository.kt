package br.edu.utfpr.unihelper.agenda.data.repository

import br.edu.utfpr.unihelper.agenda.data.remote.AgendaApi
import br.edu.utfpr.unihelper.agenda.data.remote.AgendaItemResponse
import br.edu.utfpr.unihelper.agenda.data.remote.EventoRequest
import br.edu.utfpr.unihelper.agenda.data.remote.EventoResponse

class AgendaRepository(private val api: AgendaApi) {

    suspend fun listar(dataInicio: String, dataFim: String): Result<List<AgendaItemResponse>> {
        return try {
            Result.success(api.listar(dataInicio, dataFim))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun criarEvento(request: EventoRequest): Result<EventoResponse> {
        return try {
            Result.success(api.criarEvento(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun atualizarEvento(id: String, request: EventoRequest): Result<EventoResponse> {
        return try {
            Result.success(api.atualizarEvento(id, request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun excluirEvento(id: String): Result<Unit> {
        return try {
            Result.success(api.excluirEvento(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
