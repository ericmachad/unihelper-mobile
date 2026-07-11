package br.edu.utfpr.unihelper.core.sync

import br.edu.utfpr.unihelper.auth.data.remote.AuthResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class AuthEvent {
    data class LoggedIn(val user: AuthResponse) : AuthEvent()
    data object LoggedOut : AuthEvent()
}

class AuthEventBus {
    private val _events = MutableSharedFlow<AuthEvent>(replay = 1, extraBufferCapacity = 0)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun emit(event: AuthEvent) {
        _events.tryEmit(event)
    }
}
