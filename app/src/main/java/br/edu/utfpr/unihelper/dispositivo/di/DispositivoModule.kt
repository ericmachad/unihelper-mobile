package br.edu.utfpr.unihelper.dispositivo.di

import br.edu.utfpr.unihelper.dispositivo.data.remote.DispositivoApi
import br.edu.utfpr.unihelper.dispositivo.data.repository.DispositivoRepository
import org.koin.dsl.module

val dispositivoModule = module {
    single { get<retrofit2.Retrofit>().create(DispositivoApi::class.java) }
    single { DispositivoRepository(get()) }
}