package br.edu.utfpr.unihelper.dispositivo.di

import br.edu.utfpr.unihelper.dispositivo.data.remote.DispositivoApi
import br.edu.utfpr.unihelper.dispositivo.data.repository.DispositivoRepository
import org.koin.dsl.module
import retrofit2.Retrofit

val dispositivoModule = module {
    single<DispositivoApi> { get<Retrofit>().create(DispositivoApi::class.java) }
    single { DispositivoRepository(get()) }
}