package br.edu.utfpr.unihelper.core.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val status: Int,
    val message: String
)
