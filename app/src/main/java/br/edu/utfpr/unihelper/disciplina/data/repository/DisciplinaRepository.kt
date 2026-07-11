package br.edu.utfpr.unihelper.disciplina.data.repository

import br.edu.utfpr.unihelper.agenda.data.local.EventoDao
import br.edu.utfpr.unihelper.core.local.SyncStatus
import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.core.sync.SyncScheduler
import br.edu.utfpr.unihelper.disciplina.data.local.DisciplinaDao
import br.edu.utfpr.unihelper.disciplina.data.local.DisciplinaEntity
import br.edu.utfpr.unihelper.disciplina.data.local.HorarioDao
import br.edu.utfpr.unihelper.disciplina.data.local.HorarioEntity
import br.edu.utfpr.unihelper.disciplina.data.remote.AlterarFaltasRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.CriarDisciplinaRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.CriarHorarioRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaApi
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DisciplinaRepository(
    private val api: DisciplinaApi,
    private val disciplinaDao: DisciplinaDao,
    private val horarioDao: HorarioDao,
    private val eventoDao: EventoDao,
    private val syncScheduler: SyncScheduler
) {
    fun listarFlow(): Flow<List<DisciplinaEntity>> = disciplinaDao.listar()

    fun listarDisciplinasFlow(): Flow<List<DisciplinaResponse>> =
        disciplinaDao.listar().map { entities -> entities.map { it.toResponse() } }

    suspend fun listar(): Result<List<DisciplinaResponse>> {
        syncPending()
        return safeApiCall { api.listar() }.onSuccess { disciplinas ->
            disciplinas.forEach { d ->
                disciplinaDao.inserir(d.toEntity())
                d.horarios.forEach { h ->
                    horarioDao.inserir(h.toEntity(d.id))
                }
            }
        }
    }

    suspend fun buscarPorId(id: String): Result<DisciplinaResponse> {
        val cached = disciplinaDao.buscarPorId(id)
        return safeApiCall { api.buscarPorId(id) }.onSuccess { response ->
            disciplinaDao.inserir(response.toEntity())
            response.horarios.forEach { horario ->
                horarioDao.inserir(horario.toEntity(response.id))
            }
        }.recoverCatching { e ->
            cached?.let { it.toResponse() } ?: throw e
        }
    }

    suspend fun criar(request: CriarDisciplinaRequest): Result<DisciplinaResponse> {
        val localId = java.util.UUID.randomUUID().toString()
        val entity = DisciplinaEntity(
            id = localId,
            nome = request.nome,
            professor = request.professor,
            cargaHorariaTotal = request.cargaHorariaTotal,
            cargaHorariaSemanal = request.cargaHorariaSemanal,
            limiteFaltas = request.limiteFaltas,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        disciplinaDao.inserir(entity)
        request.horarios.forEach { h ->
            horarioDao.inserir(HorarioEntity(
                id = java.util.UUID.randomUUID().toString(),
                diaSemana = h.diaSemana,
                horaInicio = h.horaInicio,
                horaFim = h.horaFim,
                disciplinaId = localId,
                syncStatus = SyncStatus.PENDING_CREATE
            ))
        }

        return safeApiCall { api.criar(request) }.onSuccess { response ->
            disciplinaDao.substituirPorServerResponse(localId, response.toEntity())
            horarioDao.substituirPorDisciplina(localId, response.horarios.map { it.toEntity(response.id) })
        }.onFailure {
            syncScheduler.agendar()
        }
    }

    suspend fun atualizar(id: String, request: CriarDisciplinaRequest): Result<DisciplinaResponse> {
        disciplinaDao.atualizarStatus(id, SyncStatus.PENDING_UPDATE.name)
        return safeApiCall { api.atualizar(id, request) }.onSuccess { response ->
            disciplinaDao.inserir(response.toEntity())
            horarioDao.substituirPorDisciplina(id, response.horarios.map { it.toEntity(response.id) })
        }.onFailure {
            syncScheduler.agendar()
        }
    }

    suspend fun excluir(id: String): Result<Unit> {
        disciplinaDao.atualizarStatus(id, SyncStatus.PENDING_DELETE.name)
        return safeApiCall { api.excluir(id) }.map { }.onSuccess {
            eventoDao.deletarPorDisciplina(id)
            disciplinaDao.deletarPorId(id)
            horarioDao.deletarPorDisciplina(id)
        }.onFailure {
            syncScheduler.agendar()
        }
    }

    suspend fun alterarFaltas(id: String, operacao: String): Result<DisciplinaResponse> {
        return safeApiCall { api.alterarFaltas(id, AlterarFaltasRequest(operacao)) }.onSuccess { response ->
            disciplinaDao.inserir(response.toEntity())
        }
    }

    suspend fun syncPending() {
        val pendentes = disciplinaDao.listarPendentes()
        for (disciplina in pendentes) {
            when (disciplina.syncStatus) {
                SyncStatus.PENDING_CREATE -> {
                    val horarios = horarioDao.listarPorDisciplinaSync(disciplina.id)
                    val request = disciplina.toRequest(horarios)
                    safeApiCall { api.criar(request) }.onSuccess { response ->
                        disciplinaDao.substituirPorServerResponse(disciplina.id, response.toEntity())
                        horarioDao.substituirPorDisciplina(disciplina.id, response.horarios.map { it.toEntity(response.id) })
                    }
                }
                SyncStatus.PENDING_UPDATE -> {
                    val horarios = horarioDao.listarPorDisciplinaSync(disciplina.id)
                    val request = disciplina.toRequest(horarios)
                    safeApiCall { api.atualizar(disciplina.id, request) }.onSuccess {
                        disciplinaDao.atualizarStatus(disciplina.id, SyncStatus.SYNCED.name)
                    }
                }
                SyncStatus.PENDING_DELETE -> {
                    safeApiCall { api.excluir(disciplina.id) }.map { }.onSuccess {
                        disciplinaDao.deletarPorId(disciplina.id)
                        horarioDao.deletarPorDisciplina(disciplina.id)
                    }
                }
                else -> {}
            }
        }
    }
}

private fun DisciplinaResponse.toEntity() = DisciplinaEntity(
    id = id,
    nome = nome,
    professor = professor,
    cargaHorariaTotal = cargaHorariaTotal,
    cargaHorariaSemanal = cargaHorariaSemanal,
    limiteFaltas = limiteFaltas,
    faltasRegistradas = faltasRegistradas,
    faltasCriticas = faltasCriticas,
    syncStatus = SyncStatus.SYNCED
)

private fun br.edu.utfpr.unihelper.disciplina.data.remote.HorarioResponse.toEntity(disciplinaId: String) = HorarioEntity(
    id = id,
    diaSemana = diaSemana,
    horaInicio = horaInicio,
    horaFim = horaFim,
    disciplinaId = disciplinaId,
    syncStatus = SyncStatus.SYNCED
)

private fun DisciplinaEntity.toResponse() = DisciplinaResponse(
    id = id,
    nome = nome,
    professor = professor,
    cargaHorariaTotal = cargaHorariaTotal,
    cargaHorariaSemanal = cargaHorariaSemanal,
    limiteFaltas = limiteFaltas,
    faltasRegistradas = faltasRegistradas,
    faltasCriticas = faltasCriticas,
    horarios = emptyList()
)

private fun DisciplinaEntity.toRequest(horarios: List<HorarioEntity>) = CriarDisciplinaRequest(
    nome = nome,
    professor = professor,
    cargaHorariaTotal = cargaHorariaTotal,
    cargaHorariaSemanal = cargaHorariaSemanal,
    limiteFaltas = limiteFaltas,
    horarios = horarios.map { it.toHorarioRequest() }
)

private fun HorarioEntity.toHorarioRequest() = CriarHorarioRequest(
    diaSemana = diaSemana,
    horaInicio = horaInicio,
    horaFim = horaFim
)
