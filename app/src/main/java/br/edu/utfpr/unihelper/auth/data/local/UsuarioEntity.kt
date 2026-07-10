package br.edu.utfpr.unihelper.auth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.edu.utfpr.unihelper.core.local.SyncStatus

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey
    val id: String,
    val nomeCompleto: String,
    val apelido: String? = null,
    val email: String,
    val curso: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val syncUpdatedAt: Long = System.currentTimeMillis()
)
