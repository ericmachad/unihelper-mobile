package br.edu.utfpr.unihelper.nota.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.edu.utfpr.unihelper.core.local.SyncStatus

@Entity(tableName = "notas")
data class NotaEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val conteudo: String? = null,
    val criadoEm: String,
    val atualizadoEm: String? = null,
    val disciplinaId: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val syncUpdatedAt: Long = System.currentTimeMillis()
)