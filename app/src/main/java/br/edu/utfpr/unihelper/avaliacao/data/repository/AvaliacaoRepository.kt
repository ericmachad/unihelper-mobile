package br.edu.utfpr.unihelper.avaliacao.data.repository

import br.edu.utfpr.unihelper.avaliacao.data.local.AvaliacaoDao
import br.edu.utfpr.unihelper.avaliacao.data.local.AvaliacaoEntity
import br.edu.utfpr.unihelper.avaliacao.data.remote.AvaliacaoApi
import br.edu.utfpr.unihelper.avaliacao.data.remote.CriarEventoRequest
import br.edu.utfpr.unihelper.avaliacao.data.remote.LancarNotaRequest
import br.edu.utfpr.unihelper.avaliacao.data.remote.MediaResponse
import br.edu.utfpr.unihelper.core.network.safeApiCall
import kotlinx.coroutines.flow.Flow

class AvaliacaoRepository(
    private val dao: AvaliacaoDao,
    private val api: AvaliacaoApi
) {
    fun listarPorDisciplina(disciplinaId: String): Flow<List<AvaliacaoEntity>> =
        dao.listarPorDisciplina(disciplinaId)

    suspend fun syncDoBackend(disciplinaId: String) {
        safeApiCall {
            api.listarPorDisciplina(disciplinaId)
        }.onSuccess { eventos ->
            eventos.forEach { evento ->
                val entity = AvaliacaoEntity(
                    id = evento.id,
                    descricao = evento.titulo,
                    peso = evento.peso ?: 0f,
                    data = evento.dataHoraInicio.take(10),
                    valor = evento.valor,
                    tipo = evento.tipo,
                    disciplinaId = evento.disciplinaId ?: disciplinaId
                )
                dao.inserir(entity)
            }
        }
    }

    suspend fun calcularMedia(
        disciplinaId: String,
        mediaMinima: Float = 6.0f
    ): Result<MediaResponse> = safeApiCall {
        api.calcularMedia(disciplinaId, mediaMinima)
    }

    suspend fun criar(
        avaliacao: AvaliacaoEntity,
        disciplinaId: String
    ) {
        dao.inserir(avaliacao)
        safeApiCall {
            api.criar(
                CriarEventoRequest(
                    titulo = avaliacao.descricao,
                    tipo = avaliacao.tipo,
                    dataHoraInicio = "${avaliacao.data}T00:00:00",
                    dataHoraFim = "${avaliacao.data}T23:59:00",
                    valor = avaliacao.valor,
                    peso = avaliacao.peso,
                    disciplinaId = disciplinaId
                )
            )
        }.onSuccess { response ->
            dao.deletarPorId(avaliacao.id)
            dao.inserir(
                avaliacao.copy(
                    id = response.id,
                    descricao = response.titulo,
                    peso = response.peso ?: 0f,
                    data = response.dataHoraInicio.take(10),
                    valor = response.valor,
                    tipo = response.tipo,
                    disciplinaId = response.disciplinaId ?: disciplinaId
                )
            )
        }
    }

    suspend fun atualizar(avaliacao: AvaliacaoEntity) {
        dao.atualizar(avaliacao)
        safeApiCall {
            api.atualizar(
                id = avaliacao.id,
                CriarEventoRequest(
                    titulo = avaliacao.descricao,
                    tipo = avaliacao.tipo,
                    dataHoraInicio = "${avaliacao.data}T00:00:00",
                    dataHoraFim = "${avaliacao.data}T23:59:00",
                    valor = avaliacao.valor,
                    peso = avaliacao.peso,
                    disciplinaId = avaliacao.disciplinaId
                )
            )
        }
    }

    suspend fun deletar(id: String) {
        dao.deletarPorId(id)
        safeApiCall { api.excluir(id) }
    }

    suspend fun lancarNota(id: String, valor: Float) {
        dao.lancarNota(id, valor)
        safeApiCall { api.lancarNota(id, LancarNotaRequest(valor)) }
    }
}
