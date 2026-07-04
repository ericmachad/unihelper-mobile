package br.edu.utfpr.unihelper.auth.di

import br.edu.utfpr.unihelper.auth.data.remote.AuthApi
import br.edu.utfpr.unihelper.auth.data.repository.AuthRepository
import br.edu.utfpr.unihelper.auth.ui.AuthViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val authModule = module {
    single<AuthApi> { get<Retrofit>().create(AuthApi::class.java) }

    single { AuthRepository(get(), get()) }

    viewModel { AuthViewModel(get()) }
}
