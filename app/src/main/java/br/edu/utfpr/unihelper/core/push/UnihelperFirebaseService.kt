package br.edu.utfpr.unihelper.core.push

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import br.edu.utfpr.unihelper.MainActivity
import br.edu.utfpr.unihelper.core.local.TokenStorage
import br.edu.utfpr.unihelper.dispositivo.data.repository.DispositivoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UnihelperFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveFcmToken(token)
        enviarAoBackend(token)
    }

    private fun saveFcmToken(token: String) {
        val storage = TokenStorage.instance
        if (storage != null) {
            storage.saveFcmToken(token)
        } else {
            getSharedPreferences("fcm_fallback", Context.MODE_PRIVATE)
                .edit()
                .putString("pending_fcm_token", token)
                .apply()
        }
    }

    private fun enviarAoBackend(token: String) {
        val jwt = TokenStorage.instance?.getToken()
        if (jwt != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repo = org.koin.core.context.GlobalContext.get()
                        .get<DispositivoRepository>()
                    repo.registrarToken(token)
                } catch (_: Exception) {
                    // Koin não inicializado ou repo indisponível
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "UniHelper"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: ""

        if (AppState.isForeground) {
            // App in foreground: posted to a shared event mechanism
            ForegroundEventBus.newMessage(title, body)
        } else {
            // App in background: create a system notification
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = CHANNEL_ID
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt() % Int.MAX_VALUE, notification)
    }

    companion object {
        private const val CHANNEL_ID = "unihelper_notificacoes"
        private const val CHANNEL_NAME = "Notificações"
    }
}

object ForegroundEventBus {
    data class PushMessage(val title: String, val body: String)

    private val _events = mutableListOf<PushMessage>()
    val events: List<PushMessage> get() = _events.toList()

    fun newMessage(title: String, body: String) {
        _events.add(PushMessage(title, body))
    }

    fun clear() {
        _events.clear()
    }
}