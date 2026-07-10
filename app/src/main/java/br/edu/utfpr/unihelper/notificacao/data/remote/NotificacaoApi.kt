package br.edu.utfpr.unihelper.notificacao.data.remote

import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificacaoApi {

    @GET("notificacoes")
    suspend fun listar(
        @Query("apenasNaoLidas") apenasNaoLidas: Boolean? = null
    ): NotificacaoListResponse

    @PATCH("notificacoes/{id}/read")
    suspend fun marcarComoLida(@Path("id") id: String): NotificacaoResponse

    @PATCH("notificacoes/read-all")
    suspend fun marcarTodasComoLidas(): Map<String, Any>
}
