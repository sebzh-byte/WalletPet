package com.example.walletpet.data.repository

import com.example.walletpet.data.local.CompraEntity
import com.example.walletpet.data.local.MascotaDao
import com.example.walletpet.data.local.MascotaEntity
import com.example.walletpet.domain.model.Catalogo
import com.example.walletpet.domain.model.Mascota
import com.example.walletpet.domain.model.Producto
import com.example.walletpet.domain.model.Recompensa
import com.example.walletpet.domain.model.ReglasMascota
import com.example.walletpet.domain.model.ResultadoAlimentar
import com.example.walletpet.domain.model.ResultadoCompra
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.walletpet.domain.repository.MascotaRepository

class MascotaRepositoryImpl(private val dao: MascotaDao) : MascotaRepository {

    // Evita que dos acciones al mismo tiempo (ej. comprar y alimentar) se pisen las monedas
    private val candado = Mutex()

    override val mascota: Flow<Mascota> =
        combine(dao.observar(), dao.observarCompras()) { fila, compras ->
            (fila ?: MascotaEntity()).aModelo(compras.toSet())
        }

    private suspend fun actual(): MascotaEntity = dao.obtener() ?: MascotaEntity().also { dao.guardar(it) }

    override suspend fun recompensarAhorro(monto: Double): Recompensa = candado.withLock {
        val m = actual()
        val ganadas = ReglasMascota.monedasPorAhorro(monto)
        var xp = m.xp + ReglasMascota.XP_POR_AHORRO
        var nivel = m.nivel
        val subio = xp >= 0.999f
        if (subio) {
            xp = 0f
            nivel += 1
        }
        dao.guardar(m.copy(monedas = m.monedas + ganadas, xp = xp, nivel = nivel))
        Recompensa(ganadas, subio, nivel)
    }

    override suspend fun alimentar(): ResultadoAlimentar = candado.withLock {
        val m = actual()
        val ahora = System.currentTimeMillis()
        val comida = ReglasMascota.comidaActual(m.aModelo(emptySet()), ahora)
        when {
            comida >= ReglasMascota.PANCITA_LLENA -> ResultadoAlimentar.Lleno
            m.croquetas <= 0 -> ResultadoAlimentar.SinCroquetas
            else -> {
                val nueva = (comida + ReglasMascota.COMIDA_POR_PORCION).coerceAtMost(1f)
                val restantes = m.croquetas - 1
                dao.guardar(m.copy(comida = nueva, comidaActualizadaMillis = ahora, croquetas = restantes))
                ResultadoAlimentar.Alimentado(restantes)
            }
        }
    }

    override suspend fun comprar(producto: Producto): ResultadoCompra = candado.withLock {
        val m = actual()
        val esCroqueta = producto.nombre == Catalogo.CROQUETAS
        when {
            producto.nivelRequerido > m.nivel -> ResultadoCompra.Bloqueado
            m.monedas < producto.precio -> ResultadoCompra.SinMonedas
            esCroqueta -> {
                val total = m.croquetas + ReglasMascota.PORCIONES_POR_BOLSA
                dao.guardar(m.copy(monedas = m.monedas - producto.precio, croquetas = total))
                ResultadoCompra.Comprado(producto, total)
            }
            else -> {
                // ¿Ya lo tenía? Se revisa con la tabla de compras
                val yaLoTiene = !producto.consumible && dao.obtenerCompra(producto.nombre) != null
                if (yaLoTiene) return@withLock ResultadoCompra.YaLoTienes
                dao.guardar(m.copy(monedas = m.monedas - producto.precio))
                dao.insertarCompra(CompraEntity(producto.nombre))
                ResultadoCompra.Comprado(producto, m.croquetas)
            }
        }
    }

    private fun MascotaEntity.aModelo(compras: Set<String>) = Mascota(
        monedas = monedas,
        nivel = nivel,
        xp = xp,
        comida = comida,
        comidaActualizadaMillis = comidaActualizadaMillis,
        croquetas = croquetas,
        comprados = compras
    )
}
