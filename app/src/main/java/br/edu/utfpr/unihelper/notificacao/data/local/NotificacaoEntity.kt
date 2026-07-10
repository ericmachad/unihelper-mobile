package br.edu.utfpr.unihelper.notificacao.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.edu.utfpr.unihelper.core.local.SyncStatus

@Entity(tableName = "notificacoes")
data class NotificacaoEntity(
    @PrimaryKey
    val id: String,
    val tipo: String,
    val titulo: String,
    val mensagem: String,
    val lida: Boolean = false,
    val criadaEm: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val syncUpdatedAt: Long = System.currentTimeMillis()
)
