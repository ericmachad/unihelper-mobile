package br.edu.utfpr.unihelper.agenda.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.edu.utfpr.unihelper.core.local.SyncStatus

@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey
    val id: String,
    val titulo: String,
    val tipo: String = "OUTRO",
    val dataHoraInicio: String,
    val dataHoraFim: String,
    val peso: Float? = null,
    val valor: Float? = null,
    val disciplinaId: String? = null,
    val disciplinaNome: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val syncUpdatedAt: Long = System.currentTimeMillis()
)
