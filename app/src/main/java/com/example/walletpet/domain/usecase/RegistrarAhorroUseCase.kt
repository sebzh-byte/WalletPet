package com.example.walletpet.domain.usecase

import com.example.walletpet.domain.model.Recompensa
import com.example.walletpet.domain.repository.MascotaRepository
import com.example.walletpet.domain.repository.MetaRepository

/**
 * Regla de negocio que usan dos pantallas (Inicio y Metas):
 * guardar el ahorro en la meta y darle al usuario sus monedas y experiencia.
 */
class RegistrarAhorroUseCase(
    private val metas: MetaRepository,
    private val mascota: MascotaRepository
) {
    suspend operator fun invoke(metaId: Long, monto: Double): Recompensa {
        metas.registrarAhorro(metaId, monto)
        return mascota.recompensarAhorro(monto)
    }
}
