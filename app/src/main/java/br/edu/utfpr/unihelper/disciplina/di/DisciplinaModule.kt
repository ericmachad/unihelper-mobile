package br.edu.utfpr.unihelper.disciplina.di

import br.edu.utfpr.unihelper.disciplina.data.local.DisciplinaDao
import br.edu.utfpr.unihelper.disciplina.data.local.HorarioDao
import br.edu.utfpr.unihelper.disciplina.data.remote.DisciplinaApi
import br.edu.utfpr.unihelper.disciplina.data.repository.DisciplinaRepository
import br.edu.utfpr.unihelper.disciplina.ui.DisciplinaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val disciplinaModule = module {
    single<DisciplinaApi> { get<Retrofit>().create(DisciplinaApi::class.java) }

    single { DisciplinaRepository(get(), get(), get(), get(), get()) }

    viewModel { DisciplinaViewModel(get(), get()) }
}
