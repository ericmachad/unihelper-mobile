package br.edu.utfpr.unihelper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import br.edu.utfpr.unihelper.agenda.di.agendaModule
import br.edu.utfpr.unihelper.auth.di.authModule
import br.edu.utfpr.unihelper.dashboard.di.dashboardModule
import br.edu.utfpr.unihelper.core.di.databaseModule
import br.edu.utfpr.unihelper.core.di.networkModule
import br.edu.utfpr.unihelper.core.di.syncModule
import br.edu.utfpr.unihelper.core.local.TokenStorage
import br.edu.utfpr.unihelper.core.sync.SyncWorker
import br.edu.utfpr.unihelper.disciplina.di.disciplinaModule
import br.edu.utfpr.unihelper.dispositivo.data.repository.DispositivoRepository
import br.edu.utfpr.unihelper.dispositivo.di.dispositivoModule
import br.edu.utfpr.unihelper.documento.di.documentoModule
import br.edu.utfpr.unihelper.nota.di.notaModule
import br.edu.utfpr.unihelper.notificacao.di.notificacaoModule
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class UnihelperApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        startKoin {
            androidContext(this@UnihelperApp)
            modules(networkModule, databaseModule, syncModule, authModule, disciplinaModule, agendaModule, dashboardModule, documentoModule, notaModule, notificacaoModule, dispositivoModule)
        }

        TokenStorage.instance = org.koin.core.context.GlobalContext.get().get()

        SyncWorker.agendar(this)

        migrarFcmFallback()

        reenviarFcmToken()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "unihelper_notificacoes",
                "Notificações",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun reenviarFcmToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val storage = TokenStorage.instance ?: return@launch
                var fcmToken = storage.getFcmToken()
                if (fcmToken == null) {
                    fcmToken = com.google.android.gms.tasks.Tasks.await(
                        FirebaseMessaging.getInstance().token,
                        30, TimeUnit.SECONDS
                    )
                    storage.saveFcmToken(fcmToken)
                }
                val jwt = storage.getToken() ?: return@launch
                val repo = org.koin.core.context.GlobalContext.get()
                    .get<DispositivoRepository>()
                repo.registrarToken(fcmToken)
            } catch (_: Exception) {
            }
        }
    }

    private fun migrarFcmFallback() {
        val fallback = getSharedPreferences("fcm_fallback", MODE_PRIVATE)
        val pending = fallback.getString("pending_fcm_token", null)
        if (pending != null) {
            TokenStorage.instance?.saveFcmToken(pending)
            fallback.edit().remove("pending_fcm_token").apply()
        }
    }
}
