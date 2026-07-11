package br.edu.utfpr.unihelper.auth.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val senha: String
)

@Serializable
data class RegisterRequest(
    val nomeCompleto: String,
    val apelido: String? = null,
    val email: String,
    val senha: String,
    val curso: String? = null
)

@Serializable
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val idUsuario: String,
    val nomeCompleto: String,
    val apelido: String? = null,
    val email: String,
    val curso: String? = null
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class AtualizarPerfilRequest(
    val nomeCompleto: String? = null,
    val apelido: String? = null,
    val curso: String? = null
)

@Serializable
data class AlterarSenhaRequest(
    val senhaAtual: String,
    val novaSenha: String
)

@Serializable
data class RegisterResponse(
    val mensagem: String,
    val email: String
)

@Serializable
data class ResendConfirmationRequest(val email: String)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val novaSenha: String
)

@Serializable
data class ConfirmEmailRequest(
    val email: String,
    val codigo: String
)
