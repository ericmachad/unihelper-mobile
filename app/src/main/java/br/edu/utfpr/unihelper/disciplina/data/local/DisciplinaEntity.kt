package br.edu.utfpr.unihelper.disciplina.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.edu.utfpr.unihelper.core.local.SyncStatus

@Entity(tableName = "disciplinas")
data class DisciplinaEntity(
    @PrimaryKey
    val id: String,
    val nome: String,
    val professor: String? = null,
    val cargaHorariaTotal: Int,
    val cargaHorariaSemanal: Int,
    val limiteFaltas: Int,
    val faltasRegistradas: Int = 0,
    val faltasCriticas: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val syncUpdatedAt: Long = System.currentTimeMillis()
)
