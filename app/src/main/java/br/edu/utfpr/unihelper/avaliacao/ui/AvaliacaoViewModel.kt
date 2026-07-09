package br.edu.utfpr.unihelper.avaliacao.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.unihelper.avaliacao.data.local.AvaliacaoEntity
import br.edu.utfpr.unihelper.avaliacao.data.repository.AvaliacaoRepository
import br.edu.utfpr.unihelper.core.local.MediaConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class BlocoAvaliacaoUiState(
    val avaliacoes: List<AvaliacaoEntity> = emptyList(),
    val media: Float? = null,
    val notaMinimaNecessaria: Float? = null,
    val statusAprovacao: StatusAprovacao = StatusAprovacao.INDEFINIDO,
    val mediaMinima: Float = MediaConfig.DEFAULT_MEDIA_MINIMA,
    val isLoading: Boolean = true,
    val avaliacaoParaNota: AvaliacaoEntity? = null,
    val avaliacaoParaEdicao: AvaliacaoEntity? = null,
    val avaliacaoParaDeletar: AvaliacaoEntity? = null,
    val showBottomSheet: Boolean = false,
    val showDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showConfigMediaDialog: Boolean = false,
    val mensagemSucesso: String? = null,
    val error: String? = null
)

enum class StatusAprovacao {
    APROVADO,
    RECUPERACAO,
    REPROVADO,
    INDEFINIDO
}

class AvaliacaoViewModel(
    private val repository: AvaliacaoRepository,
    private val mediaConfig: MediaConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlocoAvaliacaoUiState())
    val uiState: StateFlow<BlocoAvaliacaoUiState> = _uiState.asStateFlow()

    fun carregar(disciplinaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.syncDoBackend(disciplinaId)

            mediaConfig.mediaMinima.collect { mediaMinima ->
                _uiState.update { it.copy(mediaMinima = mediaMinima) }
            }
        }

        viewModelScope.launch {
            repository.listarPorDisciplina(disciplinaId).collect { avaliacoes ->
                val mediaMinima = _uiState.value.mediaMinima
                val stats = calcularEstatisticas(avaliacoes, mediaMinima)
                _uiState.update {
                    it.copy(
                        avaliacoes = avaliacoes,
                        isLoading = false,
                        media = stats.media,
                        notaMinimaNecessaria = stats.notaMinimaNecessaria,
                        statusAprovacao = stats.statusAprovacao
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.calcularMedia(disciplinaId, _uiState.value.mediaMinima)
                .onSuccess { mediaResponse ->
                    val status = when (mediaResponse.statusAprovacao) {
                        "APROVADO" -> StatusAprovacao.APROVADO
                        "RECUPERACAO" -> StatusAprovacao.RECUPERACAO
                        "REPROVADO" -> StatusAprovacao.REPROVADO
                        else -> StatusAprovacao.INDEFINIDO
                    }
                    _uiState.update {
                        it.copy(
                            media = mediaResponse.media ?: it.media,
                            notaMinimaNecessaria = mediaResponse.notaMinimaNecessaria,
                            statusAprovacao = status,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun abrirBottomSheetNota(avaliacao: AvaliacaoEntity) {
        _uiState.update { it.copy(avaliacaoParaNota = avaliacao, showBottomSheet = true) }
    }

    fun fecharBottomSheet() {
        _uiState.update { it.copy(avaliacaoParaNota = null, showBottomSheet = false) }
    }

    fun lancarNota(valor: Float) {
        val avaliacao = _uiState.value.avaliacaoParaNota ?: return
        viewModelScope.launch {
            repository.lancarNota(avaliacao.id, valor)
            fecharBottomSheet()
            exibirSucesso("Nota lançada com sucesso")
        }
    }

    fun abrirDialogCriar() {
        _uiState.update { it.copy(avaliacaoParaEdicao = null, showDialog = true) }
    }

    fun abrirDialogEditar(avaliacao: AvaliacaoEntity) {
        _uiState.update { it.copy(avaliacaoParaEdicao = avaliacao, showDialog = true) }
    }

    fun fecharDialog() {
        _uiState.update { it.copy(avaliacaoParaEdicao = null, showDialog = false) }
    }

    fun criarOuAtualizar(
        disciplinaId: String,
        descricao: String,
        peso: Float,
        data: String,
        valor: Float? = null,
        tipo: String = "PROVA"
    ) {
        val edicao = _uiState.value.avaliacaoParaEdicao
        viewModelScope.launch {
            if (edicao != null) {
                repository.atualizar(
                    edicao.copy(descricao = descricao, peso = peso, data = data, tipo = tipo)
                )
                fecharDialog()
                exibirSucesso("Avaliação atualizada")
            } else {
                repository.criar(
                    avaliacao = AvaliacaoEntity(
                        id = UUID.randomUUID().toString(),
                        descricao = descricao,
                        peso = peso,
                        data = data,
                        valor = valor,
                        tipo = tipo,
                        disciplinaId = disciplinaId
                    ),
                    disciplinaId = disciplinaId
                )
                fecharDialog()
                exibirSucesso("Avaliação criada")
            }
        }
    }

    fun abrirDialogDelete(avaliacao: AvaliacaoEntity) {
        _uiState.update {
            it.copy(avaliacaoParaDeletar = avaliacao, showDeleteDialog = true)
        }
    }

    fun fecharDialogDelete() {
        _uiState.update {
            it.copy(avaliacaoParaDeletar = null, showDeleteDialog = false)
        }
    }

    fun confirmarDelete() {
        val avaliacao = _uiState.value.avaliacaoParaDeletar ?: return
        viewModelScope.launch {
            repository.deletar(avaliacao.id)
            fecharDialogDelete()
            exibirSucesso("Avaliação excluída")
        }
    }

    fun abrirConfigMedia() {
        _uiState.update { it.copy(showConfigMediaDialog = true) }
    }

    fun fecharConfigMedia() {
        _uiState.update { it.copy(showConfigMediaDialog = false) }
    }

    fun salvarMediaMinima(valor: Float) {
        viewModelScope.launch {
            mediaConfig.setMediaMinima(valor)
            fecharConfigMedia()
            exibirSucesso("Média mínima alterada para %.1f".format(valor))
        }
    }

    fun limparMensagens() {
        _uiState.update { it.copy(mensagemSucesso = null, error = null) }
    }

    private fun exibirSucesso(mensagem: String) {
        _uiState.update { it.copy(mensagemSucesso = mensagem) }
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(mensagemSucesso = null) }
        }
    }

    private fun calcularEstatisticas(
        avaliacoes: List<AvaliacaoEntity>,
        mediaMinima: Float
    ): Estatisticas {
        val comNota = avaliacoes.filter { it.valor != null }
        val pesoTotais = comNota.sumOf { it.peso.toDouble() }.toFloat()
        val somaPonderada = comNota.sumOf { (it.valor!! * it.peso).toDouble() }.toFloat()

        val media = if (pesoTotais > 0f) somaPonderada / pesoTotais else null

        val pesoRestante = avaliacoes.filter { it.valor == null }.sumOf { it.peso.toDouble() }.toFloat()
        val totalPesos = if (avaliacoes.sumOf { it.peso.toDouble() } > 0.0) {
            avaliacoes.sumOf { it.peso.toDouble() }.toFloat()
        } else {
            1f
        }

        val notaMinimaNecessaria: Float? = if (pesoRestante > 0f && media != null) {
            ((mediaMinima * totalPesos - somaPonderada) / pesoRestante).coerceIn(0f, 10f)
        } else if (pesoRestante > 0f && media == null) {
            mediaMinima
        } else {
            null
        }

        val status = when {
            media == null -> StatusAprovacao.INDEFINIDO
            media >= mediaMinima -> StatusAprovacao.APROVADO
            pesoRestante <= 0f -> StatusAprovacao.REPROVADO
            else -> StatusAprovacao.RECUPERACAO
        }

        return Estatisticas(media, notaMinimaNecessaria, status)
    }

    private data class Estatisticas(
        val media: Float?,
        val notaMinimaNecessaria: Float?,
        val statusAprovacao: StatusAprovacao
    )
}
