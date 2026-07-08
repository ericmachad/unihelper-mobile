package br.edu.utfpr.unihelper.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import br.edu.utfpr.unihelper.avaliacao.data.local.AvaliacaoEntity
import br.edu.utfpr.unihelper.avaliacao.data.local.AvaliacaoDao

@Database(
    entities = [AvaliacaoEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun avaliacaoDao(): AvaliacaoDao
}
