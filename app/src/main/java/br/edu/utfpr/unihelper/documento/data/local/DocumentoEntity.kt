package br.edu.utfpr.unihelper.documento.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.edu.utfpr.unihelper.core.local.SyncStatus

@Entity(tableName = "documentos")
data class DocumentoEntity(
    @PrimaryKey
    val id: String,
    val nomeArquivo: String,
    val mimeType: String,
    val tamanhoBytes: Long,
    val descricao: String? = null,
    val criadoEm: String,
    val disciplinaId: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val syncUpdatedAt: Long = System.currentTimeMillis()
)
