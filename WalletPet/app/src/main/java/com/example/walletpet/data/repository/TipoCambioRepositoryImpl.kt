package com.example.walletpet.data.repository

import com.example.walletpet.data.remote.api.ApiService
import com.example.walletpet.domain.model.TipoCambio
import com.example.walletpet.domain.repository.SinDatosException
import com.example.walletpet.domain.repository.TipoCambioRepository
import kotlin.coroutines.cancellation.CancellationException

class TipoCambioRepositoryImpl(private val api: ApiService) : TipoCambioRepository {

    override suspend fun obtenerTasa(base: String, destino: String): Result<TipoCambio> =
        try {
            val respuesta = api.getTipoCambio(base)
            val tasa = respuesta.rates?.get(destino)
            if (respuesta.result != "success" || tasa == null) {
                Result.failure(SinDatosException())
            } else {
                Result.success(TipoCambio(base, destino, tasa))   // DTO -> modelo de la app
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
