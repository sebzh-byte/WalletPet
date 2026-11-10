package com.example.walletpet.presentation.metas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.walletpet.core.common.UiState
import com.example.walletpet.core.ui.components.EstadoBarra
import com.example.walletpet.di.AppContainer
import com.example.walletpet.domain.model.Meta
import com.example.walletpet.domain.model.TipoCambio
import com.example.walletpet.domain.model.Validacion
import com.example.walletpet.domain.model.validarMonto
import com.example.walletpet.domain.repository.MascotaRepository
import com.example.walletpet.domain.repository.MetaRepository
import com.example.walletpet.domain.repository.SinDatosException
import com.example.walletpet.domain.repository.TipoCambioRepository
import com.example.walletpet.domain.usecase.RegistrarAhorroUseCase
import com.example.walletpet.integration.audio.SonidoManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class MetasUiState(
    val cargando: Boolean = true,
    val barra: EstadoBarra = EstadoBarra(),
    val metas: List<Meta> = emptyList(),
    val tipoCambio: UiState<TipoCambio> = UiState.Loading,
    val monedaDestino: String = "USD",
    val mensaje: MensajeMetas? = null
) {
    val totalAhorrado: Double get() = metas.sumOf { it.ahorrado }
    val tasa: Double?
        get() = when (val t = tipoCambio) {
            is UiState.Success -> t.data.tasa
            else -> null
        }
}

sealed interface MensajeMetas {
    data object MetaCreada : MensajeMetas
    data class AhorroGuardado(val monedas: Int, val subioNivel: Boolean, val nivel: Int) : MensajeMetas
    data class MetaEliminada(val nombre: String) : MensajeMetas
}

class MetasViewModel(
    private val metaRepository: MetaRepository,
    mascotaRepository: MascotaRepository,
    private val tipoCambioRepository: TipoCambioRepository,
    private val registrarAhorroUseCase: RegistrarAhorroUseCase,
    private val sonido: SonidoManager
) : ViewModel() {

    private val tipoCambio = MutableStateFlow<UiState<TipoCambio>>(UiState.Loading)
    private val destino = MutableStateFlow("USD")
    private val mensaje = MutableStateFlow<MensajeMetas?>(null)
    private var peticion: Job? = null

    val state: StateFlow<MetasUiState> = combine(
        mascotaRepository.mascota,
        metaRepository.metas,
        tipoCambio,
        destino,
        mensaje
    ) { m, metas, tc, dest, msg ->
        MetasUiState(
            cargando = false,
            barra = EstadoBarra(m.nivel, m.xp, m.monedas),
            metas = metas,
            tipoCambio = tc,
            monedaDestino = dest,
            mensaje = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MetasUiState())

    init {
        cargarTipoCambio()
    }

    /** Consulta la API. También es la acción del botón "Reintentar" y de los chips USD/EUR. */
    fun cargarTipoCambio(moneda: String = destino.value) {
        destino.value = moneda
        peticion?.cancel()
        peticion = viewModelScope.launch {
            tipoCambio.value = UiState.Loading
            tipoCambioRepository.obtenerTasa("MXN", moneda)
                .onSuccess { tipoCambio.value = UiState.Success(it) }
                .onFailure { tipoCambio.value = errorAEstado(it) }
        }
    }

    private fun errorAEstado(e: Throwable): UiState<Nothing> = when (e) {
        is SinDatosException -> UiState.Empty
        is IOException -> UiState.Error("Sin conexión a internet. Revisa tu red e inténtalo de nuevo.")
        is HttpException -> UiState.Error("El servicio no respondió (código ${e.code()}).")
        else -> UiState.Error("Ocurrió un error inesperado.")
    }

    /** Regresa el error para mostrarlo en el formulario, o null si se creó. */
    fun crearMeta(nombre: String, objetivoTexto: String): String? {
        val limpio = nombre.trim()
        if (limpio.isEmpty()) return "Por favor, ingresa el nombre de la meta"
        if (limpio.length > 40) return "El nombre es muy largo (máximo 40 letras)"
        val objetivo = when (val v = validarMonto(objetivoTexto)) {
            is Validacion.Error -> return v.mensaje
            is Validacion.Ok -> v.valor
        }
        viewModelScope.launch {
            metaRepository.crearMeta(limpio, objetivo)
            mensaje.value = MensajeMetas.MetaCreada
        }
        return null
    }

    fun registrarAhorro(meta: Meta, montoTexto: String): String? {
        val monto = when (val v = validarMonto(montoTexto)) {
            is Validacion.Error -> return v.mensaje
            is Validacion.Ok -> v.valor
        }
        viewModelScope.launch {
            val r = registrarAhorroUseCase(meta.id, monto)
            sonido.maullar()
            mensaje.value = MensajeMetas.AhorroGuardado(r.monedasGanadas, r.subioNivel, r.nivel)
        }
        return null
    }

    fun eliminarMeta(meta: Meta) {
        viewModelScope.launch {
            metaRepository.eliminarMeta(meta.id)
            mensaje.value = MensajeMetas.MetaEliminada(meta.nombre)
        }
    }

    fun cerrarMensaje() {
        mensaje.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MetasViewModel(
                    AppContainer.metaRepository,
                    AppContainer.mascotaRepository,
                    AppContainer.tipoCambioRepository,
                    AppContainer.registrarAhorro,
                    AppContainer.sonidoManager
                )
            }
        }
    }
}
