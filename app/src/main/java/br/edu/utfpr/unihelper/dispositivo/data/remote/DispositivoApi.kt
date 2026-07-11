package br.edu.utfpr.unihelper.dispositivo.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface DispositivoApi {

    @POST("dispositivo/token")
    suspend fun registrarToken(@Body request: TokenRequest): Response<Unit>

    @DELETE("dispositivo/token")
    suspend fun removerToken(): Response<Unit>
}

@Serializable
data class TokenRequest(val token: String)