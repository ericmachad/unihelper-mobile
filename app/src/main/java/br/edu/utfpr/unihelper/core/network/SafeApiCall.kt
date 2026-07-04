package br.edu.utfpr.unihelper.core.network

import kotlinx.serialization.json.Json
import retrofit2.HttpException

private val errorJson = Json { ignoreUnknownKeys = true }

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return runCatching {
        block()
    }.mapFailure { throwable ->
        when (throwable) {
            is HttpException -> {
                val errorBody = throwable.response()?.errorBody()?.string()
                val apiError = errorBody?.let {
                    runCatching { errorJson.decodeFromString<ApiErrorResponse>(it) }
                        .getOrNull()
                }
                ApiException(
                    status = apiError?.status ?: throwable.code(),
                    message = apiError?.message
                        ?: throwable.message()
                        ?: "Erro inesperado do servidor"
                )
            }

            is java.net.ConnectException -> ApiException(
                status = 0,
                message = "Não foi possível conectar ao servidor"
            )

            is java.net.SocketTimeoutException -> ApiException(
                status = 0,
                message = "Tempo limite excedido"
            )

            else -> ApiException(
                status = 0,
                message = throwable.localizedMessage ?: "Erro inesperado"
            )
        }
    }
}

class ApiException(
    val status: Int,
    override val message: String
) : Exception(message)

private fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> {
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(transform(it)) }
    )
}
