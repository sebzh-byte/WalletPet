package com.example.walletpet.domain.model

// Entidades que usa la app. No saben nada de Room, Retrofit ni Compose.

data class Meta(
    val id: Long,
    val nombre: String,
    val objetivo: Double,
    val ahorrado: Double,
    val ultimoAhorroMillis: Long?
) {
    val progreso: Float
        get() = if (objetivo <= 0) 0f else (ahorrado / objetivo).toFloat().coerceIn(0f, 1f)
    val faltante: Double get() = (objetivo - ahorrado).coerceAtLeast(0.0)
    val completada: Boolean get() = ahorrado >= objetivo
}

/** Un depósito hecho a una meta (sirve para los reportes). */
data class Ahorro(
    val id: Long,
    val metaNombre: String,
    val monto: Double,
    val fechaMillis: Long
)

data class Movimiento(
    val id: Long,
    val categoria: String,
    val monto: Double,
    val esIngreso: Boolean,
    val fechaMillis: Long
)

data class Producto(
    val nombre: String,
    val emoji: String,
    val precio: Int,
    val nivelRequerido: Int,
    val consumible: Boolean = false   // las croquetas se pueden comprar muchas veces
)

data class Mascota(
    val monedas: Int,
    val nivel: Int,
    val xp: Float,
    val comida: Float,                 // lo que tenía la pancita la última vez que se guardó
    val comidaActualizadaMillis: Long, // cuándo se guardó (con eso calculamos el hambre de ahora)
    val croquetas: Int,                // porciones disponibles
    val comprados: Set<String>
)

data class TipoCambio(val base: String, val destino: String, val tasa: Double)

/** Resumen de un mes (sale de Room y se puede exportar a PDF). */
data class ReporteMensual(
    val clave: String,          // "2026-11"
    val titulo: String,         // "Noviembre 2026"
    val totalAhorrado: Double,
    val aportaciones: Int,
    val porMeta: List<Pair<String, Double>>,
    val gastos: Double,
    val ingresos: Double,
    val ahorros: List<Ahorro>
)

enum class PetMood(val frase: String) {
    FELIZ("¡Miau! Vamos muy bien"),
    NORMAL("Hola, meow"),
    HAMBRE("Tengo hambre…"),
    TRISTE("Extraño que ahorremos…")
}

// Resultados de las acciones con el gato y la tienda
sealed interface ResultadoAlimentar {
    data class Alimentado(val porcionesRestantes: Int) : ResultadoAlimentar
    data object Lleno : ResultadoAlimentar
    data object SinCroquetas : ResultadoAlimentar
}

sealed interface ResultadoCompra {
    data class Comprado(val producto: Producto, val croquetas: Int) : ResultadoCompra
    data object SinMonedas : ResultadoCompra
    data object Bloqueado : ResultadoCompra
    data object YaLoTienes : ResultadoCompra
}

data class Recompensa(val monedasGanadas: Int, val subioNivel: Boolean, val nivel: Int)
