package br.edu.utfpr.unihelper.dispositivo.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface DispositivoApi {

    @POST("dispositivo/token")
    suspend fun registrarToken(@Body request: TokenRequest)

    @DELETE("dispositivo/token")
    suspend fun removerToken()
}

@Serializable
data class TokenRequest(val token: String)