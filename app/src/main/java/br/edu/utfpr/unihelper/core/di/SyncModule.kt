package br.edu.utfpr.unihelper.core.di

import br.edu.utfpr.unihelper.core.sync.AuthEventBus
import br.edu.utfpr.unihelper.core.sync.SyncManager
import org.koin.dsl.module

val syncModule = module {
    single { AuthEventBus() }
    single { SyncManager(get(), get()) }
}
