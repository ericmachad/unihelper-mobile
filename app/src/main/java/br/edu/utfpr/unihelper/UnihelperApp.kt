package br.edu.utfpr.unihelper

import android.app.Application
import br.edu.utfpr.unihelper.auth.di.authModule
import br.edu.utfpr.unihelper.core.di.databaseModule
import br.edu.utfpr.unihelper.core.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class UnihelperApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@UnihelperApp)
            modules(networkModule, databaseModule, authModule)
        }
    }
}
