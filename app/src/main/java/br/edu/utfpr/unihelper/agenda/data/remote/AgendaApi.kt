package br.edu.utfpr.unihelper.agenda.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AgendaApi {

    @GET("agenda")
    suspend fun listar(
        @Query("dataInicio") dataInicio: String,
        @Query("dataFim") dataFim: String
    ): List<AgendaItemResponse>

    @POST("eventos")
    suspend fun criarEvento(@Body request: EventoRequest): EventoResponse

    @PUT("eventos/{id}")
    suspend fun atualizarEvento(@Path("id") id: String, @Body request: EventoRequest): EventoResponse

    @DELETE("eventos/{id}")
    suspend fun excluirEvento(@Path("id") id: String)
}
