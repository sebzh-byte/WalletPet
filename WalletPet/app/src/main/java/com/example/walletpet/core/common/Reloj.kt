package com.example.walletpet.core.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Emite cada cierto tiempo. Sirve para recalcular el hambre del gato aunque nadie toque nada. */
fun relojCada(milisegundos: Long): Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        delay(milisegundos)
    }
}
