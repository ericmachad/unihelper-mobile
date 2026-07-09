package br.edu.utfpr.unihelper

import android.app.Application
import br.edu.utfpr.unihelper.agenda.di.agendaModule
import br.edu.utfpr.unihelper.auth.di.authModule
import br.edu.utfpr.unihelper.dashboard.di.dashboardModule
import br.edu.utfpr.unihelper.avaliacao.di.avaliacaoModule
import br.edu.utfpr.unihelper.core.di.databaseModule
import br.edu.utfpr.unihelper.core.di.networkModule
import br.edu.utfpr.unihelper.disciplina.di.disciplinaModule
import br.edu.utfpr.unihelper.documento.di.documentoModule
import br.edu.utfpr.unihelper.nota.di.notaModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class UnihelperApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@UnihelperApp)
            modules(networkModule, databaseModule, authModule, disciplinaModule, avaliacaoModule, agendaModule, dashboardModule, documentoModule, notaModule)
        }
    }
}
