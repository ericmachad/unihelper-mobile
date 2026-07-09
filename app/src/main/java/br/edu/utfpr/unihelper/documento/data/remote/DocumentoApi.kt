package br.edu.utfpr.unihelper.documento.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface DocumentoApi {

    @GET("disciplinas/{disciplinaId}/documentos")
    suspend fun listar(@Path("disciplinaId") disciplinaId: String): List<DocumentoResponse>

    @Multipart
    @POST("disciplinas/{disciplinaId}/documentos")
    suspend fun upload(
        @Path("disciplinaId") disciplinaId: String,
        @Part arquivo: MultipartBody.Part,
        @Part("descricao") descricao: RequestBody?
    ): DocumentoResponse

    @GET("disciplinas/{disciplinaId}/documentos/{id}")
    suspend fun download(
        @Path("disciplinaId") disciplinaId: String,
        @Path("id") id: String
    ): ResponseBody

    @DELETE("disciplinas/{disciplinaId}/documentos/{id}")
    suspend fun deletar(
        @Path("disciplinaId") disciplinaId: String,
        @Path("id") id: String
    ): Response<Unit>
}
