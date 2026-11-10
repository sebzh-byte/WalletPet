package com.example.walletpet.presentation.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.walletpet.core.common.relojCada
import com.example.walletpet.core.ui.components.EstadoBarra
import com.example.walletpet.di.AppContainer
import com.example.walletpet.domain.model.Meta
import com.example.walletpet.domain.model.ReglasMascota
import com.example.walletpet.domain.model.ResultadoAlimentar
import com.example.walletpet.domain.model.Validacion
import com.example.walletpet.domain.model.validarMonto
import com.example.walletpet.domain.repository.MascotaRepository
import com.example.walletpet.domain.repository.MetaRepository
import com.example.walletpet.domain.usecase.RegistrarAhorroUseCase
import com.example.walletpet.integration.audio.SonidoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    metaRepository: MetaRepository,
    private val mascotaRepository: MascotaRepository,
    private val registrarAhorroUseCase: RegistrarAhorroUseCase,
    private val sonido: SonidoManager
) : ViewModel() {

    private val dialogo = MutableStateFlow<DialogoHome?>(null)

    val state: StateFlow<HomeUiState> = combine(
        mascotaRepository.mascota,
        metaRepository.metas,
        relojCada(30_000),        // cada 30 s se recalcula el hambre
        dialogo
    ) { mascota, metas, _, dialogoActual ->
        val ahora = System.currentTimeMillis()
        val comida = ReglasMascota.comidaActual(mascota, ahora)
        val cumplimiento = ReglasMascota.cumplimientoMetas(metas, ahora)
        HomeUiState(
            cargando = false,
            barra = EstadoBarra(mascota.nivel, mascota.xp, mascota.monedas),
            metas = metas.sortedBy { it.completada },     // las pendientes primero
            comida = comida,
            animo = ReglasMascota.animo(comida, cumplimiento),
            croquetas = mascota.croquetas,
            mood = ReglasMascota.humor(comida, cumplimiento),
            dialogo = dialogoActual
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** El usuario tocó "Alimentar": decidimos qué ventanita mostrar. */
    fun tocarAlimentar() {
        val s = state.value
        dialogo.value = when {
            s.comida >= ReglasMascota.PANCITA_LLENA -> DialogoHome.Lleno
            s.croquetas <= 0 -> DialogoHome.SinCroquetas
            else -> DialogoHome.Comer(s.croquetas)
        }
    }

    /** Confirmó que sí: se gasta una porción de croquetas (no monedas). */
    fun confirmarAlimentar() {
        viewModelScope.launch {
            dialogo.value = when (val r = mascotaRepository.alimentar()) {
                is ResultadoAlimentar.Alimentado -> {
                    sonido.maullar()
                    DialogoHome.Alimentado(r.porcionesRestantes)
                }
                ResultadoAlimentar.Lleno -> DialogoHome.Lleno
                ResultadoAlimentar.SinCroquetas -> DialogoHome.SinCroquetas
            }
        }
    }

    fun posponerComida() {
        dialogo.value = DialogoHome.Mensaje("Tu gatito esperará pacientemente a su próxima comida")
    }

    /** Regresa el mensaje de error, o null si el monto es válido y se guardó. */
    fun registrarAhorro(meta: Meta, montoTexto: String): String? {
        val monto = when (val v = validarMonto(montoTexto)) {
            is Validacion.Error -> return v.mensaje
            is Validacion.Ok -> v.valor
        }
        val seCumple = !meta.completada && meta.ahorrado + monto >= meta.objetivo
        viewModelScope.launch {
            val recompensa = registrarAhorroUseCase(meta.id, monto)
            sonido.maullar()
            dialogo.value = DialogoHome.AhorroGuardado(
                monedas = recompensa.monedasGanadas,
                subioNivel = recompensa.subioNivel,
                nivel = recompensa.nivel,
                metaCumplida = if (seCumple) meta.nombre else null
            )
        }
        return null
    }

    fun cerrarDialogo() {
        dialogo.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    AppContainer.metaRepository,
                    AppContainer.mascotaRepository,
                    AppContainer.registrarAhorro,
                    AppContainer.sonidoManager
                )
            }
        }
    }
}
