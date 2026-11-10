package com.example.walletpet.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Respuesta cruda de la API, tal como llega. El repositorio la convierte a TipoCambio. */
data class TipoCambioDto(
    @SerializedName("result") val result: String?,
    @SerializedName("base_code") val baseCode: String?,
    @SerializedName("rates") val rates: Map<String, Double>?
)
