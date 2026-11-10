package com.example.walletpet.presentation.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Guarda si la app ya fue desbloqueada (sobrevive a girar la pantalla). */
class AuthViewModel : ViewModel() {
    private val _desbloqueada = MutableStateFlow(false)
    val desbloqueada: StateFlow<Boolean> = _desbloqueada.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun alExito() {
        _desbloqueada.value = true
        _mensaje.value = null
    }

    fun alError(texto: String) {
        _mensaje.value = texto
    }
}
