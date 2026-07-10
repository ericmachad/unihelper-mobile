package br.edu.utfpr.unihelper.core.sync

import br.edu.utfpr.unihelper.disciplina.data.repository.DisciplinaRepository
import br.edu.utfpr.unihelper.agenda.data.repository.AgendaRepository

class SyncManager(
    private val disciplinaRepository: DisciplinaRepository,
    private val agendaRepository: AgendaRepository
) {
    suspend fun pushAll() {
        disciplinaRepository.syncPending()
        agendaRepository.syncPending()
    }
}
