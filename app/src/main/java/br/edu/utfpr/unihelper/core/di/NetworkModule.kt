package br.edu.utfpr.unihelper.core.di

import br.edu.utfpr.unihelper.BuildConfig
import br.edu.utfpr.unihelper.core.local.TokenStorage
import br.edu.utfpr.unihelper.core.network.AuthInterceptor
import br.edu.utfpr.unihelper.core.network.TokenRefreshHelper
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { TokenStorage(androidContext()) }

    single {
        TokenRefreshHelper(
            baseUrl = BuildConfig.API_BASE_URL + "/",
            tokenStorage = get(),
            isDebug = BuildConfig.DEBUG
        )
    }

    single { AuthInterceptor(get(), get()) }

    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val contentType = "application/json".toMediaType()

        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL + "/")
            .client(get())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}
