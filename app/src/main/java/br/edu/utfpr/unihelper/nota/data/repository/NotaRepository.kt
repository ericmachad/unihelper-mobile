package br.edu.utfpr.unihelper.nota.data.repository

import br.edu.utfpr.unihelper.core.local.SyncStatus
import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.core.sync.SyncScheduler
import br.edu.utfpr.unihelper.nota.data.local.NotaDao
import br.edu.utfpr.unihelper.nota.data.local.NotaEntity
import br.edu.utfpr.unihelper.nota.data.remote.NotaApi
import br.edu.utfpr.unihelper.nota.data.remote.NotaRequest
import br.edu.utfpr.unihelper.nota.data.remote.NotaResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotaRepository(
    private val api: NotaApi,
    private val dao: NotaDao,
    private val syncScheduler: SyncScheduler
) {
    fun listarFlow(disciplinaId: String): Flow<List<NotaResponse>> =
        dao.listar(disciplinaId).map { entities -> entities.map { it.toResponse() } }

    suspend fun listar(disciplinaId: String): Result<List<NotaResponse>> {
        syncPending()
        return safeApiCall { api.listar(disciplinaId) }.onSuccess { notas ->
            dao.inserirTodas(notas.map { it.toEntity(disciplinaId) })
        }
    }

    suspend fun buscar(disciplinaId: String, id: String): Result<NotaResponse> {
        val cached = dao.buscarPorId(id)?.toResponse()
        return safeApiCall { api.buscar(disciplinaId, id) }
            .onSuccess { dao.inserir(it.toEntity(disciplinaId)) }
            .recoverCatching { e -> cached ?: throw e }
    }

    suspend fun criar(disciplinaId: String, request: NotaRequest): Result<NotaResponse> {
        val localId = java.util.UUID.randomUUID().toString()
        val entity = NotaEntity(
            id = localId,
            titulo = request.titulo,
            conteudo = request.conteudo,
            criadoEm = java.time.Instant.now().toString(),
            disciplinaId = disciplinaId,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        dao.inserir(entity)
        return safeApiCall { api.criar(disciplinaId, request) }.onSuccess { response ->
            dao.deletarPorId(localId)
            dao.inserir(response.toEntity(disciplinaId))
        }.onFailure {
            syncScheduler.agendar()
        }
    }

    suspend fun atualizar(
        disciplinaId: String,
        id: String,
        request: NotaRequest
    ): Result<NotaResponse> {
        dao.atualizarStatus(id, SyncStatus.PENDING_UPDATE.name)
        return safeApiCall { api.atualizar(disciplinaId, id, request) }.onSuccess { response ->
            dao.inserir(response.toEntity(disciplinaId))
        }.onFailure {
            syncScheduler.agendar()
        }
    }

    suspend fun excluir(disciplinaId: String, id: String): Result<Unit> {
        dao.atualizarStatus(id, SyncStatus.PENDING_DELETE.name)
        return safeApiCall { api.excluir(disciplinaId, id) }.map { }.onSuccess {
            dao.deletarPorId(id)
        }.onFailure {
            syncScheduler.agendar()
        }
    }

    suspend fun buscarPorTitulo(disciplinaId: String, termo: String): Result<List<NotaResponse>> =
        safeApiCall { api.buscarPorTitulo(disciplinaId, termo) }.onSuccess { notas ->
            notas.forEach { dao.inserir(it.toEntity(disciplinaId)) }
        }

    suspend fun syncPending() {
        val pendentes = dao.listarPendentes()
        for (nota in pendentes) {
            when (nota.syncStatus) {
                SyncStatus.PENDING_CREATE -> {
                    val request = NotaRequest(titulo = nota.titulo, conteudo = nota.conteudo)
                    safeApiCall { api.criar(nota.disciplinaId, request) }.onSuccess { response ->
                        dao.deletarPorId(nota.id)
                        dao.inserir(response.toEntity(nota.disciplinaId))
                    }
                }
                SyncStatus.PENDING_UPDATE -> {
                    val request = NotaRequest(titulo = nota.titulo, conteudo = nota.conteudo)
                    safeApiCall { api.atualizar(nota.disciplinaId, nota.id, request) }.onSuccess {
                        dao.atualizarStatus(nota.id, SyncStatus.SYNCED.name)
                    }
                }
                SyncStatus.PENDING_DELETE -> {
                    safeApiCall { api.excluir(nota.disciplinaId, nota.id) }.map { }.onSuccess {
                        dao.deletarPorId(nota.id)
                    }
                }
                else -> {}
            }
        }
    }
}

private fun NotaResponse.toEntity(disciplinaId: String) = NotaEntity(
    id = id,
    titulo = titulo,
    conteudo = conteudo,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm,
    disciplinaId = disciplinaId,
    syncStatus = SyncStatus.SYNCED
)

private fun NotaEntity.toResponse() = NotaResponse(
    id = id,
    titulo = titulo,
    conteudo = conteudo,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)