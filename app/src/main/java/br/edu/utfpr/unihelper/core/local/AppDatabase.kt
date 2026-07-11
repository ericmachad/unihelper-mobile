package br.edu.utfpr.unihelper.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import br.edu.utfpr.unihelper.agenda.data.local.EventoDao
import br.edu.utfpr.unihelper.agenda.data.local.EventoEntity
import br.edu.utfpr.unihelper.auth.data.local.UsuarioDao
import br.edu.utfpr.unihelper.auth.data.local.UsuarioEntity
import br.edu.utfpr.unihelper.disciplina.data.local.DisciplinaDao
import br.edu.utfpr.unihelper.disciplina.data.local.DisciplinaEntity
import br.edu.utfpr.unihelper.disciplina.data.local.HorarioDao
import br.edu.utfpr.unihelper.disciplina.data.local.HorarioEntity
import br.edu.utfpr.unihelper.documento.data.local.DocumentoDao
import br.edu.utfpr.unihelper.documento.data.local.DocumentoEntity
import br.edu.utfpr.unihelper.nota.data.local.NotaDao
import br.edu.utfpr.unihelper.nota.data.local.NotaEntity
import br.edu.utfpr.unihelper.notificacao.data.local.NotificacaoDao
import br.edu.utfpr.unihelper.notificacao.data.local.NotificacaoEntity

@Database(
    entities = [
        DisciplinaEntity::class,
        HorarioEntity::class,
        EventoEntity::class,
        NotificacaoEntity::class,
        DocumentoEntity::class,
        UsuarioEntity::class,
        NotaEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun disciplinaDao(): DisciplinaDao
    abstract fun horarioDao(): HorarioDao
    abstract fun eventoDao(): EventoDao
    abstract fun notificacaoDao(): NotificacaoDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun notaDao(): NotaDao

    suspend fun limparTudo() = withContext(Dispatchers.IO) {
        clearAllTables()
    }
}
