package br.edu.utfpr.unihelper.core.network

import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import br.edu.utfpr.unihelper.auth.data.remote.RefreshRequest
import br.edu.utfpr.unihelper.core.local.AppDatabase
import br.edu.utfpr.unihelper.core.local.TokenStorage
import br.edu.utfpr.unihelper.core.sync.AuthEvent
import br.edu.utfpr.unihelper.core.sync.AuthEventBus
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

class TokenRefreshHelper(
    private val baseUrl: String,
    private val tokenStorage: TokenStorage,
    private val database: AppDatabase,
    private val authEventBus: AuthEventBus,
    private val isDebug: Boolean
) {
    private val api: RefreshApi

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        val contentType = "application/json".toMediaType()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        api = retrofit.create(RefreshApi::class.java)
    }

    suspend fun refresh(): String? = withContext(Dispatchers.IO) {
        val refreshToken = tokenStorage.getRefreshToken() ?: return@withContext null

        try {
            val response = api.refreshSync(RefreshRequest(refreshToken))
            tokenStorage.saveToken(response.token)
            tokenStorage.saveRefreshToken(response.refreshToken)
            tokenStorage.saveIdUsuario(response.idUsuario)
            tokenStorage.saveNomeCompleto(response.nomeCompleto)
            response.apelido?.let { tokenStorage.saveApelido(it) }
            tokenStorage.saveEmail(response.email)
            response.curso?.let { tokenStorage.saveCurso(it) }
            response.token
        } catch (_: Exception) {
            tokenStorage.clearAll()
            database.limparTudo()
            authEventBus.emit(AuthEvent.LoggedOut)
            null
        }
    }

    private interface RefreshApi {
        @POST("auth/refresh")
        fun refreshSync(@Body request: RefreshRequest): AuthResponse
    }
}