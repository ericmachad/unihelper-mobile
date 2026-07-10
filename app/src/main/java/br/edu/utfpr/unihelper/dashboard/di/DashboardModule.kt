package br.edu.utfpr.unihelper.dashboard.di

import br.edu.utfpr.unihelper.dashboard.ui.DashboardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dashboardModule = module {
    viewModel { DashboardViewModel(get(), get()) }
}
