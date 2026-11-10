package com.example.walletpet.data.remote.api

import com.example.walletpet.data.remote.dto.TipoCambioDto
import retrofit2.http.GET
import retrofit2.http.Path

/** API pública de tipo de cambio: https://open.er-api.com/v6/latest/MXN (no necesita clave). */
interface ApiService {
    @GET("v6/latest/{moneda}")
    suspend fun getTipoCambio(@Path("moneda") moneda: String): TipoCambioDto
}
