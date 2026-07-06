package br.edu.utfpr.unihelper.disciplina.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DisciplinaApi {

    @GET("disciplinas")
    suspend fun listar(): List<DisciplinaResponse>

    @GET("disciplinas/{id}")
    suspend fun buscarPorId(@Path("id") id: String): DisciplinaResponse

    @POST("disciplinas")
    suspend fun criar(@Body request: CriarDisciplinaRequest): DisciplinaResponse

    @PUT("disciplinas/{id}")
    suspend fun atualizar(
        @Path("id") id: String,
        @Body request: CriarDisciplinaRequest
    ): DisciplinaResponse

    @DELETE("disciplinas/{id}")
    suspend fun excluir(@Path("id") id: String): Response<Unit>

    @PATCH("disciplinas/{id}/faltas")
    suspend fun alterarFaltas(
        @Path("id") id: String,
        @Body request: AlterarFaltasRequest
    ): DisciplinaResponse
}
