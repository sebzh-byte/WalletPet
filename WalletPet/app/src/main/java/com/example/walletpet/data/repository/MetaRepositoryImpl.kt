package com.example.walletpet.data.repository

import com.example.walletpet.data.local.AhorroDao
import com.example.walletpet.data.local.AhorroEntity
import com.example.walletpet.data.local.MetaDao
import com.example.walletpet.data.local.MetaEntity
import com.example.walletpet.domain.model.Ahorro
import com.example.walletpet.domain.model.Meta
import com.example.walletpet.domain.repository.MetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MetaRepositoryImpl(
    private val metaDao: MetaDao,
    private val ahorroDao: AhorroDao
) : MetaRepository {

    override val metas: Flow<List<Meta>> = metaDao.observarConAhorro().map { lista ->
        lista.map { Meta(it.id, it.nombre, it.objetivo, it.ahorrado, it.ultimoAhorro) }
    }

    override val ahorros: Flow<List<Ahorro>> = ahorroDao.observarConMeta().map { lista ->
        lista.map { Ahorro(it.id, it.metaNombre, it.monto, it.fechaMillis) }
    }

    override suspend fun crearMeta(nombre: String, objetivo: Double) {
        metaDao.insertar(MetaEntity(nombre = nombre, objetivo = objetivo))
    }

    override suspend fun registrarAhorro(metaId: Long, monto: Double) {
        ahorroDao.insertar(AhorroEntity(metaId = metaId, monto = monto))
    }

    override suspend fun eliminarMeta(metaId: Long) = metaDao.eliminar(metaId)
}
