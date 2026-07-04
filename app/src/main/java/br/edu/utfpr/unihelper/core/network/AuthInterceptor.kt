package br.edu.utfpr.unihelper.core.network

import br.edu.utfpr.unihelper.core.local.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStorage: TokenStorage,
    private val tokenRefreshHelper: TokenRefreshHelper
) : Interceptor {

    private val lock = Any()
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        if (isTokenRefreshRequest(original)) {
            return chain.proceed(original)
        }

        val authenticated = original.newBuilder()
            .header("Authorization", "Bearer ${tokenStorage.getToken() ?: ""}")
            .build()

        val response = chain.proceed(authenticated)

        if (response.code == 401 && tokenStorage.getRefreshToken() != null) {
            response.close()

            val newToken = synchronized(lock) {
                if (isRefreshing) {
                    // another thread already refreshing, wait briefly then use new token
                    null
                } else {
                    isRefreshing = true
                    val result = tokenRefreshHelper.refresh()
                    isRefreshing = false
                    result
                }
            }

            if (newToken != null) {
                val retry = original.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(retry)
            }
        }

        return response
    }

    private fun isTokenRefreshRequest(request: okhttp3.Request): Boolean {
        return request.url.encodedPath == "/auth/refresh"
    }
}
