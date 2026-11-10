package com.example.walletpet.domain.model

/**
 * Todas las reglas del gato en un solo lugar.
 *
 * - Hambre: la pancita se vacía sola con el tiempo; se llena con croquetas.
 * - Ánimo: depende de qué tan seguido ahorras en tus metas y de qué tan lleno está.
 * - Humor (PetMood): sale de los dos anteriores. Con él se elige la imagen/GIF del gato.
 */
object ReglasMascota {

    // Para la demostración pongan VELOCIDAD = 60.0 (una hora pasa en un minuto).
    // Antes de entregar, regrésenlo a 1.0
    const val VELOCIDAD = 1.0

    const val PORCIONES_POR_BOLSA = 6     // una bolsa de croquetas = 6 comidas
    const val COMIDA_POR_PORCION = 0.35f  // cuánto llena cada porción
    const val PANCITA_LLENA = 0.8f        // de aquí para arriba ya no quiere comer
    const val HORAS_PARA_VACIAR = 48.0    // de llena a vacía en 2 días
    const val DIAS_SIN_AHORRAR_TRISTE = 7.0

    private const val MS_HORA = 3_600_000.0

    private fun horasDesde(millis: Long, ahora: Long): Double =
        ((ahora - millis).coerceAtLeast(0) / MS_HORA) * VELOCIDAD

    /** Qué tan llena está la pancita en este momento (0 = vacía, 1 = llena). */
    fun comidaActual(m: Mascota, ahora: Long): Float {
        val gastado = horasDesde(m.comidaActualizadaMillis, ahora) / HORAS_PARA_VACIAR
        return (m.comida - gastado.toFloat()).coerceIn(0f, 1f)
    }

    /** 1 = ahorraste hoy, baja hasta 0 si llevas una semana sin ahorrar. */
    fun cumplimientoMetas(metas: List<Meta>, ahora: Long): Float {
        val pendientes = metas.filter { !it.completada }
        if (metas.isNotEmpty() && pendientes.isEmpty()) return 1f   // ¡todas cumplidas!
        val ultimo = metas.mapNotNull { it.ultimoAhorroMillis }.maxOrNull() ?: return 0.2f
        val dias = horasDesde(ultimo, ahora) / 24.0
        if (dias <= 1.0) return 1f
        return (1.0 - (dias - 1.0) / (DIAS_SIN_AHORRAR_TRISTE - 1.0)).toFloat().coerceIn(0f, 1f)
    }

    fun animo(comida: Float, cumplimiento: Float): Float =
        (comida * 0.4f + cumplimiento * 0.6f).coerceIn(0f, 1f)

    fun humor(comida: Float, cumplimiento: Float): PetMood = when {
        comida < 0.25f -> PetMood.HAMBRE
        cumplimiento < 0.3f -> PetMood.TRISTE
        animo(comida, cumplimiento) >= 0.7f -> PetMood.FELIZ
        else -> PetMood.NORMAL
    }

    /** Monedas y experiencia que da cada ahorro. 10 ahorros = 1 nivel. */
    fun monedasPorAhorro(monto: Double): Int = (monto / 10).toInt()
    const val XP_POR_AHORRO = 0.1f
}
