package br.edu.utfpr.unihelper.notificacao.di

import br.edu.utfpr.unihelper.notificacao.data.remote.NotificacaoApi
import br.edu.utfpr.unihelper.notificacao.data.repository.NotificacaoRepository
import br.edu.utfpr.unihelper.notificacao.ui.NotificacaoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val notificacaoModule = module {
    single<NotificacaoApi> { get<Retrofit>().create(NotificacaoApi::class.java) }
    single { NotificacaoRepository(get()) }
    viewModel { NotificacaoViewModel(get()) }
}
