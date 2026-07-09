package br.edu.utfpr.unihelper.core.di

import androidx.room.Room
import br.edu.utfpr.unihelper.core.local.AppDatabase
import br.edu.utfpr.unihelper.core.local.MediaConfig
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "unihelper_db"
        ).fallbackToDestructiveMigration().build()
    }

    single { MediaConfig(androidContext()) }
}
