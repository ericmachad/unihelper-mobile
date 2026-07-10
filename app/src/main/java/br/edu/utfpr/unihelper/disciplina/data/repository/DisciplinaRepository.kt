package br.edu.utfpr.unihelper.disciplina.data.repository

import br.edu.utfpr.unihelper.core.local.SyncStatus
import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.disciplina.data.local.DisciplinaDao
import br.edu.utfpr.unihelper.disciplina.data.local.DisciplinaEntity
import br.edu.utfpr.unihelper.disciplina.data.local.HorarioDao
import br.edu.utfpr.unihelper.disciplina.data.local.HorarioEntity
import br.edu.utfpr.unihelper.disciplina.data.remote.AlterarFaltasRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.CriarDisciplinaRequest
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaApi
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaResponse
import kotlinx.coroutines.flow.Flow

class DisciplinaRepository(
    private val api: DisciplinaApi,
    private val disciplinaDao: DisciplinaDao,
    private val horarioDao: HorarioDao
) {
    fun listarFlow(): Flow<List<DisciplinaEntity>> = disciplinaDao.listar()

    suspend fun listar(): Result<List<DisciplinaResponse>> {
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
            disciplinaDao.deletarPorId(localId)
            disciplinaDao.inserir(response.toEntity())
            horarioDao.deletarPorDisciplina(localId)
            response.horarios.forEach { horario ->
                horarioDao.inserir(horario.toEntity(response.id))
            }
        }
    }

    suspend fun atualizar(id: String, request: CriarDisciplinaRequest): Result<DisciplinaResponse> {
        disciplinaDao.atualizarStatus(id, SyncStatus.PENDING_UPDATE.name)
        return safeApiCall { api.atualizar(id, request) }.onSuccess { response ->
            disciplinaDao.inserir(response.toEntity())
            horarioDao.deletarPorDisciplina(id)
            response.horarios.forEach { horario ->
                horarioDao.inserir(horario.toEntity(response.id))
            }
        }
    }

    suspend fun excluir(id: String): Result<Unit> {
        disciplinaDao.atualizarStatus(id, SyncStatus.PENDING_DELETE.name)
        return safeApiCall { api.excluir(id) }.map { }.onSuccess {
            disciplinaDao.deletarPorId(id)
            horarioDao.deletarPorDisciplina(id)
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
                    val request = disciplina.toRequest()
                    safeApiCall { api.criar(request) }.onSuccess { response ->
                        disciplinaDao.deletarPorId(disciplina.id)
                        disciplinaDao.inserir(response.toEntity())
                    }
                }
                SyncStatus.PENDING_UPDATE -> {
                    val request = disciplina.toRequest()
                    safeApiCall { api.atualizar(disciplina.id, request) }.onSuccess {
                        disciplinaDao.atualizarStatus(disciplina.id, SyncStatus.SYNCED.name)
                    }
                }
                SyncStatus.PENDING_DELETE -> {
                    safeApiCall { api.excluir(disciplina.id) }.map { }.onSuccess {
                        disciplinaDao.deletarPorId(disciplina.id)
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

private fun DisciplinaEntity.toRequest() = CriarDisciplinaRequest(
    nome = nome,
    professor = professor,
    cargaHorariaTotal = cargaHorariaTotal,
    cargaHorariaSemanal = cargaHorariaSemanal,
    limiteFaltas = limiteFaltas,
    horarios = emptyList()
)
