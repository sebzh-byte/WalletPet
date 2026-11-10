package com.example.walletpet.presentation.gastos

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.walletpet.core.ui.components.EstadoBarra
import com.example.walletpet.di.AppContainer
import com.example.walletpet.domain.model.Ahorro
import com.example.walletpet.domain.model.Catalogo
import com.example.walletpet.domain.model.Meta
import com.example.walletpet.domain.model.Movimiento
import com.example.walletpet.domain.model.ReporteMensual
import com.example.walletpet.domain.model.Validacion
import com.example.walletpet.domain.model.validarMonto
import com.example.walletpet.domain.repository.GastoRepository
import com.example.walletpet.domain.repository.MascotaRepository
import com.example.walletpet.domain.repository.MetaRepository
import com.example.walletpet.integration.pdf.PdfReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GastosUiState(
    val cargando: Boolean = true,
    val barra: EstadoBarra = EstadoBarra(),
    val gastosDelMes: List<Pair<String, Double>> = emptyList(),
    val gastosTotales: List<Pair<String, Double>> = emptyList(),
    val listaGastos: List<Movimiento> = emptyList(),      // cada gasto con su nombre (pestaña "Todo")
    val reportes: List<ReporteMensual> = emptyList(),
    val metas: List<Meta> = emptyList()
)

/** Cosas que pasan una sola vez (no son estado): abrir el PDF o un aviso. */
sealed interface EventoGastos {
    data class PdfListo(val uri: Uri) : EventoGastos
    data class Mensaje(val texto: String) : EventoGastos
}

class GastosViewModel(
    private val gastoRepository: GastoRepository,
    metaRepository: MetaRepository,
    mascotaRepository: MascotaRepository,
    private val pdfReporter: PdfReporter
) : ViewModel() {

    private val _eventos = Channel<EventoGastos>(Channel.BUFFERED)
    val eventos: Flow<EventoGastos> = _eventos.receiveAsFlow()

    private val locale = Locale.forLanguageTag("es-MX")

    val state: StateFlow<GastosUiState> = combine(
        gastoRepository.movimientos,
        metaRepository.ahorros,
        metaRepository.metas,
        mascotaRepository.mascota
    ) { movimientos, ahorros, metas, m ->
        val mesActual = claveMes(System.currentTimeMillis())
        val gastos = movimientos.filter { !it.esIngreso }
        GastosUiState(
            cargando = false,
            barra = EstadoBarra(m.nivel, m.xp, m.monedas),
            gastosDelMes = porCategoria(gastos.filter { claveMes(it.fechaMillis) == mesActual }),
            gastosTotales = porCategoria(gastos),
            listaGastos = gastos,
            reportes = armarReportes(ahorros, movimientos),
            metas = metas
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GastosUiState())

    /** Regresa el error o null si se guardó. La categoría es opcional (si no se elige va a "Otros"). */
    fun agregarMovimiento(esIngreso: Boolean, nombre: String, montoTexto: String, categoria: String): String? {
        val limpio = nombre.trim()
        if (!esIngreso && limpio.isEmpty()) return "Por favor, escribe el nombre del gasto"
        if (limpio.length > 40) return "El nombre es muy largo (máximo 40 letras)"
        val monto = when (val v = validarMonto(montoTexto)) {
            is Validacion.Error -> return v.mensaje
            is Validacion.Ok -> v.valor
        }
        viewModelScope.launch {
            if (esIngreso) {
                gastoRepository.agregar(limpio.ifEmpty { "Ingreso" }, "Ingreso", monto, true)
            } else {
                gastoRepository.agregar(limpio, categoria.ifEmpty { Catalogo.OTROS }, monto, false)
            }
        }
        return null
    }

    fun eliminarMovimiento(movimiento: Movimiento) {
        viewModelScope.launch {
            gastoRepository.eliminar(movimiento.id)
            _eventos.send(EventoGastos.Mensaje("Se eliminó \"${movimiento.nombre.ifEmpty { movimiento.categoria }}\""))
        }
    }

    /** Genera el PDF del mes en un hilo aparte y avisa a la pantalla cuando está listo. */
    fun exportarPdf(clave: String) {
        val actual = state.value
        val reporte = actual.reportes.find { it.clave == clave }
        if (reporte == null) {
            _eventos.trySend(EventoGastos.Mensaje("No hay datos para este reporte."))
            return
        }
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                runCatching { pdfReporter.generar(reporte, actual.metas) }
            }
            resultado
                .onSuccess { _eventos.send(EventoGastos.PdfListo(it)) }
                .onFailure { _eventos.send(EventoGastos.Mensaje("No se pudo crear el PDF. Inténtalo de nuevo.")) }
        }
    }

    // ---------- Ayudantes ----------

    private fun claveMes(millis: Long): String =
        SimpleDateFormat("yyyy-MM", Locale.US).format(Date(millis))

    private fun tituloMes(millis: Long): String =
        SimpleDateFormat("MMMM yyyy", locale).format(Date(millis))
            .replaceFirstChar { it.titlecase(locale) }

    private fun porCategoria(gastos: List<Movimiento>): List<Pair<String, Double>> =
        gastos.groupBy { it.categoria }
            .map { (c, lista) -> c to lista.sumOf { it.monto } }
            .sortedByDescending { it.second }

    private fun armarReportes(ahorros: List<Ahorro>, movimientos: List<Movimiento>): List<ReporteMensual> {
        val meses = (ahorros.map { it.fechaMillis } + movimientos.map { it.fechaMillis })
            .groupBy { claveMes(it) }
        return meses.keys.sortedDescending().map { clave ->
            val ahorrosMes = ahorros.filter { claveMes(it.fechaMillis) == clave }
            val movsMes = movimientos.filter { claveMes(it.fechaMillis) == clave }
            val ejemplo = meses.getValue(clave).first()
            ReporteMensual(
                clave = clave,
                titulo = tituloMes(ejemplo),
                totalAhorrado = ahorrosMes.sumOf { it.monto },
                aportaciones = ahorrosMes.size,
                porMeta = ahorrosMes.groupBy { it.metaNombre }.map { (n, l) -> n to l.sumOf { it.monto } },
                gastos = movsMes.filter { !it.esIngreso }.sumOf { it.monto },
                ingresos = movsMes.filter { it.esIngreso }.sumOf { it.monto },
                ahorros = ahorrosMes,
                listaGastos = movsMes.filter { !it.esIngreso }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GastosViewModel(
                    AppContainer.gastoRepository,
                    AppContainer.metaRepository,
                    AppContainer.mascotaRepository,
                    AppContainer.pdfReporter
                )
            }
        }
    }
}
