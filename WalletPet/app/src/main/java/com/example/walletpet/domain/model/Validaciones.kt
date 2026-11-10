package com.example.walletpet.domain.model

sealed interface Validacion {
    data class Ok(val valor: Double) : Validacion
    data class Error(val mensaje: String) : Validacion
}

/** Valida lo que el usuario escribe como dinero. Acepta "150", "150.5" o "150,5". */
fun validarMonto(texto: String): Validacion {
    val limpio = texto.trim().replace(',', '.')
    val valor = limpio.toDoubleOrNull()
    return when {
        limpio.isEmpty() -> Validacion.Error("Por favor, ingresa un monto")
        valor == null || valor.isNaN() -> Validacion.Error("Ese monto no es válido")
        valor <= 0 -> Validacion.Error("El monto debe ser mayor que 0")
        valor > 1_000_000 -> Validacion.Error("El monto es demasiado grande")
        else -> Validacion.Ok(valor)
    }
}

fun Double.pesos(): String = "$" + "%,.0f".format(this)
fun Double.pesosConCentavos(): String = "$" + "%,.2f".format(this)
