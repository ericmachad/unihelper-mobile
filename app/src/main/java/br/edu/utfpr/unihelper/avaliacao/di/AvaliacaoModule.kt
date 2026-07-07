package br.edu.utfpr.unihelper.avaliacao.di

import br.edu.utfpr.unihelper.avaliacao.data.local.AvaliacaoDao
import br.edu.utfpr.unihelper.avaliacao.data.remote.AvaliacaoApi
import br.edu.utfpr.unihelper.avaliacao.data.repository.AvaliacaoRepository
import br.edu.utfpr.unihelper.avaliacao.ui.AvaliacaoViewModel
import br.edu.utfpr.unihelper.core.local.AppDatabase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val avaliacaoModule = module {
    single<AvaliacaoDao> { get<AppDatabase>().avaliacaoDao() }

    single<AvaliacaoApi> { get<Retrofit>().create(AvaliacaoApi::class.java) }

    single { AvaliacaoRepository(get(), get()) }

    viewModel { AvaliacaoViewModel(get(), get()) }
}
