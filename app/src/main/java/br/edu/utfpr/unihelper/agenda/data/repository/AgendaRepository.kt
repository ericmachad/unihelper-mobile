package br.edu.utfpr.unihelper.agenda.data.repository

import br.edu.utfpr.unihelper.agenda.data.local.EventoDao
import br.edu.utfpr.unihelper.agenda.data.local.EventoEntity
import br.edu.utfpr.unihelper.agenda.data.remote.AgendaApi
import br.edu.utfpr.unihelper.agenda.data.remote.AgendaItemResponse
import br.edu.utfpr.unihelper.agenda.data.remote.EventoRequest
import br.edu.utfpr.unihelper.agenda.data.remote.EventoResponse
import br.edu.utfpr.unihelper.agenda.data.remote.LancarNotaRequest
import br.edu.utfpr.unihelper.agenda.data.remote.MediaResponse
import br.edu.utfpr.unihelper.core.local.SyncStatus
import br.edu.utfpr.unihelper.core.network.ApiException
import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.core.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow

class AgendaRepository(
    private val api: AgendaApi,
    private val dao: EventoDao,
    private val syncScheduler: SyncScheduler
) {
    suspend fun listar(dataInicio: String, dataFim: String): Result<List<AgendaItemResponse>> {
        syncPending()
        return safeApiCall { api.listar(dataInicio, dataFim) }.onSuccess { items ->
            dao.inserirTodas(items.map { it.toEntity() })
        }
    }

    suspend fun syncDoBackend(disciplinaId: String) {
        safeApiCall { api.listarPorDisciplina(disciplinaId) }.onSuccess { eventos ->
            eventos.forEach { dao.inserir(it.toEntity()) }
        }
    }

    fun listarPorDisciplinaFlow(disciplinaId: String): Flow<List<EventoEntity>> =
        dao.listarPorDisciplina(disciplinaId)

    fun listarFlow(dataInicio: String, dataFim: String): Flow<List<EventoEntity>> =
        dao.listar(dataInicio, dataFim)

    suspend fun buscarPorId(id: String): Result<EventoResponse> =
        safeApiCall { api.buscarPorId(id) }

    suspend fun lancarNota(id: String, valor: Float): Result<EventoResponse> {
        return safeApiCall { api.lancarNota(id, LancarNotaRequest(valor)) }
            .onSuccess { dao.inserir(it.toEntity()) }
            .onFailure { error ->
                if (!error.isClientError()) {
                    dao.atualizarStatus(id, SyncStatus.PENDING_UPDATE.name)
                    syncScheduler.agendar()
                }
            }
    }

    suspend fun calcularMedia(disciplinaId: String, mediaMinima: Float): Result<MediaResponse> =
        safeApiCall { api.calcularMedia(disciplinaId, mediaMinima) }

    suspend fun criarEvento(request: EventoRequest): Result<EventoResponse> {
        return safeApiCall { api.criarEvento(request) }
            .onSuccess { response -> dao.inserir(response.toEntity()) }
            .onFailure { error ->
                if (!error.isClientError()) {
                    val localId = java.util.UUID.randomUUID().toString()
                    val entity = EventoEntity(
                        id = localId,
                        titulo = request.titulo,
                        tipo = request.tipo,
                        dataHoraInicio = request.dataHoraInicio,
                        dataHoraFim = request.dataHoraFim,
                        peso = request.peso,
                        disciplinaId = request.disciplinaId,
                        syncStatus = SyncStatus.PENDING_CREATE
                    )
                    dao.inserir(entity)
                    syncScheduler.agendar()
                }
            }
    }

    suspend fun atualizarEvento(id: String, request: EventoRequest): Result<EventoResponse> {
        return safeApiCall { api.atualizarEvento(id, request) }
            .onSuccess { response -> dao.inserir(response.toEntity()) }
            .onFailure { error ->
                if (!error.isClientError()) {
                    dao.atualizarStatus(id, SyncStatus.PENDING_UPDATE.name)
                    syncScheduler.agendar()
                }
            }
    }

    suspend fun excluirEvento(id: String): Result<Unit> {
        return safeApiCall { api.excluirEvento(id) }
            .onSuccess { dao.deletarPorId(id) }
            .onFailure { error ->
                if (!error.isClientError()) {
                    dao.atualizarStatus(id, SyncStatus.PENDING_DELETE.name)
                    syncScheduler.agendar()
                }
            }
    }

    suspend fun syncPending() {
        val pendentes = dao.listarPendentes()
        for (evento in pendentes) {
            when (evento.syncStatus) {
                SyncStatus.PENDING_CREATE -> {
                    val request = EventoRequest(
                        titulo = evento.titulo,
                        tipo = evento.tipo,
                        dataHoraInicio = evento.dataHoraInicio,
                        dataHoraFim = evento.dataHoraFim,
                        peso = evento.peso,
                        disciplinaId = evento.disciplinaId
                    )
                    safeApiCall { api.criarEvento(request) }.onSuccess { response ->
                        dao.deletarPorId(evento.id)
                        dao.inserir(response.toEntity())
                    }.onFailure { error ->
                        if (error.isClientError()) {
                            dao.deletarPorId(evento.id)
                        }
                    }
                }
                SyncStatus.PENDING_UPDATE -> {
                    val request = EventoRequest(
                        titulo = evento.titulo,
                        tipo = evento.tipo,
                        dataHoraInicio = evento.dataHoraInicio,
                        dataHoraFim = evento.dataHoraFim,
                        peso = evento.peso,
                        disciplinaId = evento.disciplinaId
                    )
                    safeApiCall { api.atualizarEvento(evento.id, request) }.onSuccess {
                        dao.atualizarStatus(evento.id, SyncStatus.SYNCED.name)
                    }
                }
                SyncStatus.PENDING_DELETE -> {
                    safeApiCall { api.excluirEvento(evento.id) }.onSuccess {
                        dao.deletarPorId(evento.id)
                    }
                }
                else -> {}
            }
        }
    }
}

private fun AgendaItemResponse.toEntity() = EventoEntity(
    id = id,
    titulo = titulo,
    tipo = tipoEvento,
    dataHoraInicio = dataHora,
    dataHoraFim = dataHoraFim ?: "",
    peso = peso,
    disciplinaId = disciplinaId,
    disciplinaNome = disciplinaNome,
    syncStatus = SyncStatus.SYNCED
)

private fun EventoResponse.toEntity() = EventoEntity(
    id = id,
    titulo = titulo,
    tipo = tipo,
    dataHoraInicio = dataHoraInicio,
    dataHoraFim = dataHoraFim,
    peso = peso,
    valor = valor,
    disciplinaId = disciplinaId,
    disciplinaNome = disciplinaNome,
    syncStatus = SyncStatus.SYNCED
)

private fun Throwable.isClientError(): Boolean {
    return (this as? ApiException)?.status in 400..499
}
