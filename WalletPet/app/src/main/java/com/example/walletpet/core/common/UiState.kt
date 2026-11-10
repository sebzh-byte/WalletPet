package com.example.walletpet.core.common

/** Estados de cualquier dato que viene de la API. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(val mensaje: String) : UiState<Nothing>
}
