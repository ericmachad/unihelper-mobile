package br.edu.utfpr.unihelper.core.di

import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

val databaseModule = module {
    single {
        // AppDatabase será criada aqui quando houver entidades
    }
}
