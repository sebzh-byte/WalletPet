package com.example.walletpet.domain.repository

import com.example.walletpet.domain.model.Ahorro
import com.example.walletpet.domain.model.Mascota
import com.example.walletpet.domain.model.Meta
import com.example.walletpet.domain.model.Movimiento
import com.example.walletpet.domain.model.Producto
import com.example.walletpet.domain.model.Recompensa
import com.example.walletpet.domain.model.ResultadoAlimentar
import com.example.walletpet.domain.model.ResultadoCompra
import com.example.walletpet.domain.model.TipoCambio
import kotlinx.coroutines.flow.Flow

// Contratos: los ViewModels solo conocen estas interfaces.
// Las implementaciones (con Room y Retrofit) están en data/repository.

interface MetaRepository {
    val metas: Flow<List<Meta>>
    val ahorros: Flow<List<Ahorro>>
    suspend fun crearMeta(nombre: String, objetivo: Double)
    suspend fun registrarAhorro(metaId: Long, monto: Double)
    suspend fun eliminarMeta(metaId: Long)
}

interface MascotaRepository {
    val mascota: Flow<Mascota>
    suspend fun recompensarAhorro(monto: Double): Recompensa
    suspend fun alimentar(): ResultadoAlimentar
    suspend fun comprar(producto: Producto): ResultadoCompra
}

interface GastoRepository {
    val movimientos: Flow<List<Movimiento>>
    suspend fun agregar(categoria: String, monto: Double, esIngreso: Boolean)
    suspend fun eliminar(id: Long)
}

interface TipoCambioRepository {
    suspend fun obtenerTasa(base: String, destino: String): Result<TipoCambio>
}

/** La API respondió, pero no trajo la moneda pedida. */
class SinDatosException : Exception("La API no devolvió datos")
