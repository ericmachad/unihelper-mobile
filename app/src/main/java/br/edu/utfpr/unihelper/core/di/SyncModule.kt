package br.edu.utfpr.unihelper.core.di

import br.edu.utfpr.unihelper.core.sync.SyncManager
import org.koin.dsl.module

val syncModule = module {
    single { SyncManager(get(), get()) }
}
