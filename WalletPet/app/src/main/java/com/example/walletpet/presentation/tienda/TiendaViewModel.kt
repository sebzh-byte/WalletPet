package com.example.walletpet.presentation.tienda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.walletpet.core.ui.components.EstadoBarra
import com.example.walletpet.di.AppContainer
import com.example.walletpet.domain.model.Catalogo
import com.example.walletpet.domain.model.Producto
import com.example.walletpet.domain.model.ResultadoCompra
import com.example.walletpet.domain.repository.MascotaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductoUi(
    val producto: Producto,
    val bloqueado: Boolean,   // falta nivel
    val loTiene: Boolean      // ya se compró (no aplica a las croquetas)
)

data class TiendaUiState(
    val barra: EstadoBarra = EstadoBarra(),
    val productos: List<ProductoUi> = emptyList(),
    val croquetas: Int = 0,
    val resultado: ResultadoCompra? = null
)

class TiendaViewModel(private val repository: MascotaRepository) : ViewModel() {

    private val resultado = MutableStateFlow<ResultadoCompra?>(null)

    val state: StateFlow<TiendaUiState> =
        combine(repository.mascota, resultado) { m, r ->
            TiendaUiState(
                barra = EstadoBarra(m.nivel, m.xp, m.monedas),
                productos = Catalogo.productos.map { p ->
                    ProductoUi(
                        producto = p,
                        bloqueado = p.nivelRequerido > m.nivel,
                        loTiene = !p.consumible && p.nombre in m.comprados
                    )
                },
                croquetas = m.croquetas,
                resultado = r
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TiendaUiState())

    fun comprar(producto: Producto) {
        viewModelScope.launch { resultado.value = repository.comprar(producto) }
    }

    fun cerrarResultado() {
        resultado.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { TiendaViewModel(AppContainer.mascotaRepository) }
        }
    }
}
