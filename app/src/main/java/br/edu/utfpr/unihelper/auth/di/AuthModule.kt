package br.edu.utfpr.unihelper.auth.di

import br.edu.utfpr.unihelper.auth.data.remote.AuthApi
import br.edu.utfpr.unihelper.auth.data.repository.AuthRepository
import br.edu.utfpr.unihelper.auth.ui.AuthViewModel
import br.edu.utfpr.unihelper.auth.ui.ConfirmEmailViewModel
import br.edu.utfpr.unihelper.auth.ui.ForgotPasswordViewModel
import br.edu.utfpr.unihelper.auth.ui.ResetPasswordViewModel
import br.edu.utfpr.unihelper.core.local.SessionManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val authModule = module {
    single<AuthApi> { get<Retrofit>().create(AuthApi::class.java) }

    single { SessionManager(get(), get(), get(), get()) }

    single { AuthRepository(get(), get(), get()) }

    viewModel { AuthViewModel(get(), get()) }
    viewModel { ConfirmEmailViewModel(get()) }
    viewModel { ForgotPasswordViewModel(get()) }
    viewModel { ResetPasswordViewModel(get()) }
}