package br.edu.utfpr.unihelper.dispositivo.data.repository

import br.edu.utfpr.unihelper.core.network.safeApiCall
import br.edu.utfpr.unihelper.dispositivo.data.remote.DispositivoApi
import br.edu.utfpr.unihelper.dispositivo.data.remote.TokenRequest

class DispositivoRepository(
    private val api: DispositivoApi
) {
    suspend fun registrarToken(token: String): Result<Unit> = safeApiCall {
        api.registrarToken(TokenRequest(token))
    }
}