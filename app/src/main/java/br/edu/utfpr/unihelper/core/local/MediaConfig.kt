package br.edu.utfpr.unihelper.core.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mediaConfigDataStore by preferencesDataStore(name = "media_config")

class MediaConfig(private val context: Context) {

    companion object {
        private val KEY_MEDIA_MINIMA = floatPreferencesKey("media_minima")
        const val DEFAULT_MEDIA_MINIMA = 6.0f
    }

    val mediaMinima: Flow<Float> = context.mediaConfigDataStore.data.map { prefs ->
        prefs[KEY_MEDIA_MINIMA] ?: DEFAULT_MEDIA_MINIMA
    }

    suspend fun setMediaMinima(valor: Float) {
        require(valor in 1f..10f) { "Média mínima deve estar entre 1 e 10" }
        context.mediaConfigDataStore.edit { it[KEY_MEDIA_MINIMA] = valor }
    }
}
