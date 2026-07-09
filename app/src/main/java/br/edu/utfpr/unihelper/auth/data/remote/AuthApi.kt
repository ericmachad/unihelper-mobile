package br.edu.utfpr.unihelper.auth.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): AuthResponse

    @PUT("auth/me")
    suspend fun atualizarPerfil(@Body request: AtualizarPerfilRequest): AuthResponse

    @POST("auth/change-password")
    suspend fun alterarSenha(@Body request: AlterarSenhaRequest)

    @POST("auth/logout")
    suspend fun logout()
}
