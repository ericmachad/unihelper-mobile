package br.edu.utfpr.unihelper.nota.di

import br.edu.utfpr.unihelper.nota.data.remote.NotaApi
import br.edu.utfpr.unihelper.nota.data.repository.NotaRepository
import br.edu.utfpr.unihelper.nota.ui.NotaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val notaModule = module {
    single<NotaApi> { get<Retrofit>().create(NotaApi::class.java) }

    single { NotaRepository(get(), get(), get()) }

    viewModel { NotaViewModel(get()) }
}
