package br.edu.utfpr.unihelper.core.network

import android.util.Log
import br.edu.utfpr.unihelper.core.ui.UiEvent
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
                val message = apiError?.message
                    ?: throwable.message()
                    ?: "Erro inesperado do servidor"
                Log.e("SafeApiCall", "HTTP ${throwable.code()}: $message")
                ApiException(
                    status = apiError?.status ?: throwable.code(),
                    message = message
                )
            }

            is java.net.ConnectException -> {
                Log.e("SafeApiCall", "ConnectException: ${throwable.message}")
                ApiException(
                    status = 0,
                    message = "Não foi possível conectar ao servidor"
                )
            }

            is java.net.SocketTimeoutException -> {
                Log.e("SafeApiCall", "Timeout: ${throwable.message}")
                ApiException(
                    status = 0,
                    message = "Tempo limite excedido"
                )
            }

            else -> {
                Log.e("SafeApiCall", "Erro inesperado: ${throwable.localizedMessage}", throwable)
                ApiException(
                    status = 0,
                    message = throwable.localizedMessage ?: "Erro inesperado"
                )
            }
        }
    }
}

class ApiException(
    val status: Int,
    override val message: String
) : Exception(message)

fun statusToTitle(status: Int): String = when (status) {
    0 -> "Erro de conexão"
    400 -> "Requisição inválida"
    401 -> "Não autorizado"
    403 -> "Acesso negado"
    404 -> "Não encontrado"
    409 -> "Conflito"
    422 -> "Dados inválidos"
    500 -> "Erro interno do servidor"
    else -> "Erro"
}

fun Throwable.toErrorDialog(): UiEvent.ErrorDialog {
    val apiException = this as? ApiException
    val status = apiException?.status ?: 0
    return UiEvent.ErrorDialog(
        title = statusToTitle(status),
        message = message ?: "Erro inesperado",
        isAuthError = status == 401
    )
}

private fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> {
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(transform(it)) }
    )
}
