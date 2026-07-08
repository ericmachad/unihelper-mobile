package br.edu.utfpr.unihelper.agenda.di

import br.edu.utfpr.unihelper.agenda.data.remote.AgendaApi
import br.edu.utfpr.unihelper.agenda.data.repository.AgendaRepository
import br.edu.utfpr.unihelper.agenda.ui.AgendaViewModel
import br.edu.utfpr.unihelper.disciplina.data.repository.DisciplinaRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val agendaModule = module {
    single<AgendaApi> { get<Retrofit>().create(AgendaApi::class.java) }
    single { AgendaRepository(get()) }
    viewModel { AgendaViewModel(get(), get()) }
}
