package br.edu.utfpr.unihelper.nota.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotaApi {

    @GET("disciplinas/{disciplinaId}/notas")
    suspend fun listar(@Path("disciplinaId") disciplinaId: String): List<NotaResponse>

    @GET("disciplinas/{disciplinaId}/notas/{id}")
    suspend fun buscar(
        @Path("disciplinaId") disciplinaId: String,
        @Path("id") id: String
    ): NotaResponse

    @POST("disciplinas/{disciplinaId}/notas")
    suspend fun criar(
        @Path("disciplinaId") disciplinaId: String,
        @Body request: NotaRequest
    ): NotaResponse

    @PUT("disciplinas/{disciplinaId}/notas/{id}")
    suspend fun atualizar(
        @Path("disciplinaId") disciplinaId: String,
        @Path("id") id: String,
        @Body request: NotaRequest
    ): NotaResponse

    @DELETE("disciplinas/{disciplinaId}/notas/{id}")
    suspend fun excluir(
        @Path("disciplinaId") disciplinaId: String,
        @Path("id") id: String
    ): Response<Unit>

    @GET("disciplinas/{disciplinaId}/notas/buscar")
    suspend fun buscarPorTitulo(
        @Path("disciplinaId") disciplinaId: String,
        @Query("q") termo: String
    ): List<NotaResponse>
}
