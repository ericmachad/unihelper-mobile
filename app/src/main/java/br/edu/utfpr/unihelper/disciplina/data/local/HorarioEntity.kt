package br.edu.utfpr.unihelper.disciplina.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.edu.utfpr.unihelper.core.local.SyncStatus

@Entity(tableName = "horarios")
data class HorarioEntity(
    @PrimaryKey
    val id: String,
    val diaSemana: Int,
    val horaInicio: String,
    val horaFim: String,
    val disciplinaId: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val syncUpdatedAt: Long = System.currentTimeMillis()
)
