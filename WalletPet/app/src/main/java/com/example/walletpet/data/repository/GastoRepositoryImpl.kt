package com.example.walletpet.data.repository

import com.example.walletpet.data.local.MovimientoDao
import com.example.walletpet.data.local.MovimientoEntity
import com.example.walletpet.domain.model.Movimiento
import com.example.walletpet.domain.repository.GastoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GastoRepositoryImpl(private val dao: MovimientoDao) : GastoRepository {

    override val movimientos: Flow<List<Movimiento>> = dao.observarTodos().map { lista ->
        lista.map { Movimiento(it.id, it.nombre, it.categoria, it.monto, it.esIngreso, it.fechaMillis) }
    }

    override suspend fun agregar(nombre: String, categoria: String, monto: Double, esIngreso: Boolean) {
        dao.insertar(MovimientoEntity(nombre = nombre, categoria = categoria, monto = monto, esIngreso = esIngreso))
    }

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
