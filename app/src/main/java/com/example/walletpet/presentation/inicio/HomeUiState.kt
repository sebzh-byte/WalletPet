package com.example.walletpet.presentation.inicio

import com.example.walletpet.core.ui.components.EstadoBarra
import com.example.walletpet.domain.model.Meta
import com.example.walletpet.domain.model.PetMood

data class HomeUiState(
    val cargando: Boolean = true,
    val barra: EstadoBarra = EstadoBarra(),
    val metas: List<Meta> = emptyList(),
    val comida: Float = 0.5f,     // 1 = pancita llena
    val animo: Float = 0.5f,
    val croquetas: Int = 0,
    val mood: PetMood = PetMood.NORMAL,
    val dialogo: DialogoHome? = null
) {
    val hambre: Float get() = 1f - comida
}

/** Ventanitas que el ViewModel le pide a la pantalla que muestre. */
sealed interface DialogoHome {
    data class Comer(val croquetas: Int) : DialogoHome
    data object Lleno : DialogoHome
    data object SinCroquetas : DialogoHome
    data class Alimentado(val porcionesRestantes: Int) : DialogoHome
    data class AhorroGuardado(
        val monedas: Int,
        val subioNivel: Boolean,
        val nivel: Int,
        val metaCumplida: String?
    ) : DialogoHome
    data class Mensaje(val texto: String) : DialogoHome
}
