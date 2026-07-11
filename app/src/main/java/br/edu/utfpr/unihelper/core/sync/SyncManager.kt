package br.edu.utfpr.unihelper.core.sync

import br.edu.utfpr.unihelper.disciplina.data.repository.DisciplinaRepository
import br.edu.utfpr.unihelper.agenda.data.repository.AgendaRepository
import br.edu.utfpr.unihelper.documento.data.repository.DocumentoRepository
import br.edu.utfpr.unihelper.nota.data.repository.NotaRepository

class SyncManager(
    private val disciplinaRepository: DisciplinaRepository,
    private val agendaRepository: AgendaRepository,
    private val documentoRepository: DocumentoRepository,
    private val notaRepository: NotaRepository
) {
    suspend fun pushAll() {
        disciplinaRepository.syncPending()
        agendaRepository.syncPending()
        documentoRepository.syncPending()
        notaRepository.syncPending()
    }
}