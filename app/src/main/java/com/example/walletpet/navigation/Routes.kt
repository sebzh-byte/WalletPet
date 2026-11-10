package com.example.walletpet.navigation

/** Todas las rutas de la app en un solo lugar. */
object Routes {
    const val INICIO = "inicio"
    const val TIENDA = "tienda"
    const val METAS = "metas"
    const val GASTOS = "gastos"
    const val REPORTES = "reportes"
    const val REPORTE_DETALLE = "reporte/{clave}"   // clave = "2026-11"

    fun reporte(clave: String) = "reporte/$clave"

    /** Pestañas de la barra inferior, en orden. */
    val pestanas = listOf(INICIO, TIENDA, METAS, GASTOS)

    /** Qué pestaña se marca según la ruta actual (los reportes están dentro de Gastos). */
    fun pestanaDe(ruta: String?): Int = when (ruta) {
        TIENDA -> 1
        METAS -> 2
        GASTOS, REPORTES, REPORTE_DETALLE -> 3
        else -> 0
    }
}
