package br.edu.utfpr.unihelper.avaliacao.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AvaliacaoApi {

    @GET("disciplinas/{disciplinaId}/avaliacoes")
    suspend fun listarPorDisciplina(@Path("disciplinaId") disciplinaId: String): List<AvaliacaoResponse>

    @POST("disciplinas/{disciplinaId}/avaliacoes")
    suspend fun criar(
        @Path("disciplinaId") disciplinaId: String,
        @Body request: CriarAvaliacaoRequest
    ): AvaliacaoResponse

    @GET("avaliacoes/{id}")
    suspend fun buscarPorId(@Path("id") id: String): AvaliacaoResponse

    @PUT("avaliacoes/{id}")
    suspend fun atualizar(
        @Path("id") id: String,
        @Body request: CriarAvaliacaoRequest
    ): AvaliacaoResponse

    @DELETE("avaliacoes/{id}")
    suspend fun excluir(@Path("id") id: String): Response<Unit>

    @PATCH("avaliacoes/{id}/nota")
    suspend fun lancarNota(
        @Path("id") id: String,
        @Body request: LancarNotaRequest
    ): AvaliacaoResponse
}
