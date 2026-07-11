package br.edu.utfpr.unihelper.core.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStorage(context: Context) {

    private var memoryToken: String? = null

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        memoryToken = token
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return memoryToken ?: prefs.getString(KEY_TOKEN, null).also { memoryToken = it }
    }

    fun saveRefreshToken(refreshToken: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun hasSession(): Boolean {
        return prefs.getString(KEY_REFRESH_TOKEN, null) != null
    }

    fun saveIdUsuario(id: String) {
        prefs.edit().putString(KEY_ID_USUARIO, id).apply()
    }

    fun getIdUsuario(): String? {
        return prefs.getString(KEY_ID_USUARIO, null)
    }

    fun saveNomeCompleto(nome: String) {
        prefs.edit().putString(KEY_NOME, nome).apply()
    }

    fun getNomeCompleto(): String? {
        return prefs.getString(KEY_NOME, null)
    }

    fun saveApelido(apelido: String?) {
        prefs.edit().putString(KEY_APELIDO, apelido ?: "").apply()
    }

    fun getApelido(): String? {
        return prefs.getString(KEY_APELIDO, null)?.ifBlank { null }
    }

    fun saveEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun saveCurso(curso: String?) {
        prefs.edit().putString(KEY_CURSO, curso ?: "").apply()
    }

    fun getCurso(): String? {
        return prefs.getString(KEY_CURSO, null)?.ifBlank { null }
    }

    fun saveFcmToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getFcmToken(): String? {
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    fun clearSession() {
        memoryToken = null
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ID_USUARIO)
            .remove(KEY_NOME)
            .remove(KEY_APELIDO)
            .remove(KEY_EMAIL)
            .remove(KEY_CURSO)
            .remove(KEY_PENDING_CONFIRMATION_EMAIL)
            .apply()
    }

    fun savePendingConfirmationEmail(email: String?) {
        if (email != null) {
            prefs.edit().putString(KEY_PENDING_CONFIRMATION_EMAIL, email).apply()
        } else {
            prefs.edit().remove(KEY_PENDING_CONFIRMATION_EMAIL).apply()
        }
    }

    fun getPendingConfirmationEmail(): String? {
        return prefs.getString(KEY_PENDING_CONFIRMATION_EMAIL, null)
    }

    fun hasPendingConfirmation(): Boolean {
        return getPendingConfirmationEmail() != null
    }

    companion object {
        private const val PREFS_NAME = "unihelper_secure_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ID_USUARIO = "id_usuario"
        private const val KEY_NOME = "nome_completo"
        private const val KEY_APELIDO = "apelido"
        private const val KEY_EMAIL = "email"
        private const val KEY_CURSO = "curso"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_PENDING_CONFIRMATION_EMAIL = "pending_confirmation_email"

        var instance: TokenStorage? = null
    }
}
