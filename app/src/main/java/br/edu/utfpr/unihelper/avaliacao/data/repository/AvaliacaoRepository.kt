package br.edu.utfpr.unihelper.avaliacao.data.repository

import br.edu.utfpr.unihelper.avaliacao.data.local.AvaliacaoDao
import br.edu.utfpr.unihelper.avaliacao.data.local.AvaliacaoEntity
import br.edu.utfpr.unihelper.avaliacao.data.remote.AvaliacaoApi
import br.edu.utfpr.unihelper.avaliacao.data.remote.CriarAvaliacaoRequest
import br.edu.utfpr.unihelper.avaliacao.data.remote.LancarNotaRequest
import kotlinx.coroutines.flow.Flow

class AvaliacaoRepository(
    private val dao: AvaliacaoDao,
    private val api: AvaliacaoApi
) {
    fun listarPorDisciplina(disciplinaId: String): Flow<List<AvaliacaoEntity>> =
        dao.listarPorDisciplina(disciplinaId)

    suspend fun criar(
        avaliacao: AvaliacaoEntity,
        disciplinaId: String
    ) {
        dao.inserir(avaliacao)
        try {
            val response = api.criar(
                disciplinaId = disciplinaId,
                request = CriarAvaliacaoRequest(
                    descricao = avaliacao.descricao,
                    peso = avaliacao.peso,
                    data = avaliacao.data,
                    valor = avaliacao.valor
                )
            )
            if (response.id != avaliacao.id) {
                dao.deletarPorId(avaliacao.id)
                dao.inserir(
                    avaliacao.copy(id = response.id)
                )
            }
        } catch (_: Exception) {
        }
    }

    suspend fun atualizar(avaliacao: AvaliacaoEntity) {
        dao.atualizar(avaliacao)
        try {
            api.atualizar(
                id = avaliacao.id,
                request = CriarAvaliacaoRequest(
                    descricao = avaliacao.descricao,
                    peso = avaliacao.peso,
                    data = avaliacao.data,
                    valor = avaliacao.valor
                )
            )
        } catch (_: Exception) {
        }
    }

    suspend fun deletar(id: String) {
        dao.deletarPorId(id)
        try {
            api.excluir(id)
        } catch (_: Exception) {
        }
    }

    suspend fun lancarNota(id: String, valor: Float) {
        dao.lancarNota(id, valor)
        try {
            api.lancarNota(id, LancarNotaRequest(valor))
        } catch (_: Exception) {
        }
    }
}
