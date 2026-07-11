package br.edu.utfpr.unihelper.core.ui

sealed class UiEvent {
    data class Snackbar(val message: String) : UiEvent()
    data class SuccessDialog(val message: String) : UiEvent()
    data class ErrorDialog(
        val title: String,
        val message: String,
        val isAuthError: Boolean = false
    ) : UiEvent()
}
