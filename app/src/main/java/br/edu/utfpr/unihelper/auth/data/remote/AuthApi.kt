package br.edu.utfpr.unihelper.auth.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse
}
