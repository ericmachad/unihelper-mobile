package br.edu.utfpr.unihelper.documento.di

import br.edu.utfpr.unihelper.documento.data.local.DocumentoDao
import br.edu.utfpr.unihelper.documento.data.remote.DocumentoApi
import br.edu.utfpr.unihelper.documento.data.repository.DocumentoRepository
import br.edu.utfpr.unihelper.documento.ui.DocumentoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val documentoModule = module {
    single<DocumentoApi> { get<Retrofit>().create(DocumentoApi::class.java) }

    single { DocumentoRepository(get(), get(), get()) }

    viewModel { DocumentoViewModel(get()) }
}
