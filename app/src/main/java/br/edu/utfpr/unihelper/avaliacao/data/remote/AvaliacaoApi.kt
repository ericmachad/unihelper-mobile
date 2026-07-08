package br.edu.utfpr.unihelper.avaliacao.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AvaliacaoApi {

    @GET("eventos/disciplinas/{disciplinaId}")
    suspend fun listarPorDisciplina(@Path("disciplinaId") disciplinaId: String): List<EventoResponse>

    @POST("eventos")
    suspend fun criar(@Body request: CriarEventoRequest): EventoResponse

    @GET("eventos/{id}")
    suspend fun buscarPorId(@Path("id") id: String): EventoResponse

    @PUT("eventos/{id}")
    suspend fun atualizar(
        @Path("id") id: String,
        @Body request: CriarEventoRequest
    ): EventoResponse

    @DELETE("eventos/{id}")
    suspend fun excluir(@Path("id") id: String): Response<Unit>

    @PATCH("eventos/{id}/nota")
    suspend fun lancarNota(
        @Path("id") id: String,
        @Body request: LancarNotaRequest
    ): EventoResponse

    @GET("eventos/disciplinas/{disciplinaId}/media")
    suspend fun calcularMedia(
        @Path("disciplinaId") disciplinaId: String,
        @Query("mediaMinima") mediaMinima: Float = 6.0f
    ): MediaResponse
}
