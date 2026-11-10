package com.example.walletpet.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import java.util.Calendar

data class Meta(val id: Int, val nombre: String, val ahorrado: Double, val objetivo: Double) {
    val progreso: Float
        get() = if (objetivo <= 0) 0f else (ahorrado / objetivo).toFloat().coerceIn(0f, 1f)
}

data class Movimiento(val categoria: String, val monto: Double, val esIngreso: Boolean, val mes: Int)

data class Producto(val nombre: String, val emoji: String, val precio: Int, val nivelRequerido: Int)

data class Reporte(val titulo: String, val resumen: String, val detalle: String)

fun Double.pesos(): String = "$" + "%.0f".format(this)

fun mesActual(): Int = Calendar.getInstance().get(Calendar.MONTH)

object AppState {
    const val COSTO_COMIDA = 20

    var nivel by mutableIntStateOf(1)
    var xp by mutableFloatStateOf(0.3f)
    var monedas by mutableIntStateOf(100)
    var comida by mutableFloatStateOf(0.5f)
    var animo by mutableFloatStateOf(0.8f)

    val metas = mutableStateListOf(
        Meta(1, "Audífonos nuevos", 450.0, 750.0),
        Meta(2, "Viaje de fin de semestre", 120.0, 2000.0),
        Meta(3, "Laptop gaming", 1060.0, 2000.0)
    )

    private val mesPasado = (mesActual() + 11) % 12

    val movimientos = mutableStateListOf(
        Movimiento("Alimentación", 200.0, false, mesActual()),
        Movimiento("Transporte", 45.0, false, mesActual()),
        Movimiento("Transporte", 1555.0, false, mesPasado),
        Movimiento("Entretenimiento", 300.0, false, mesPasado),
        Movimiento("Escuela y Papelería", 370.0, false, mesPasado),
        Movimiento("Alimentación", 1040.0, false, mesPasado),
        Movimiento("Celular / Internet", 380.0, false, mesPasado),
        Movimiento("Beca", 3000.0, true, mesActual())
    )

    val comprados = mutableStateListOf<String>()

    val productos = listOf(
        Producto("Croquetas", "🥫", 100, 1),
        Producto("Arenero", "🧺", 150, 1),
        Producto("Cama", "🛏️", 175, 1),
        Producto("Planta", "🪴", 200, 1),
        Producto("Sorpresa 1", "🎁", 300, 3),
        Producto("Sorpresa 2", "🎁", 320, 3),
        Producto("Sorpresa 3", "🎁", 400, 4),
        Producto("Sorpresa 4", "🎁", 420, 4),
        Producto("Sorpresa 5", "🎁", 450, 4)
    )

    val reportes = listOf(
        Reporte(
            "Mes 1 (Audífonos)",
            "En este apartado se verá el reporte de todas tus inversiones durante el mes",
            "Durante el Mes 1 dedicado a la meta de tus Audífonos nuevos, lograste acumular un total de \$450.00 MXN, lo que representa un avance del 60% respecto a tu objetivo final de \$750.00 MXN. Mantuviste un ritmo constante con una aportación promedio semanal de \$112.50 MXN."
        ),
        Reporte(
            "Mes 2 (Viaje de fin de semestre)",
            "En este apartado se verá el reporte de todas tus inversiones durante el mes",
            "Aquí aparecerá el resumen de tu meta Viaje de fin de semestre."
        )
    )

    val saldo: Double
        get() = movimientos.sumOf { if (it.esIngreso) it.monto else -it.monto }

    fun alimentar(): Boolean {
        if (monedas < COSTO_COMIDA) return false
        monedas -= COSTO_COMIDA
        comida = (comida + 0.4f).coerceAtMost(1f)
        animo = (animo + 0.1f).coerceAtMost(1f)
        return true
    }

    fun registrarAhorro(id: Int, monto: Double) {
        val i = metas.indexOfFirst { it.id == id }
        if (i < 0) return
        metas[i] = metas[i].copy(ahorrado = metas[i].ahorrado + monto)
        monedas += (monto / 10).toInt()
        xp += 0.1f
        if (xp >= 1f) {
            xp = 0f
            nivel += 1
        }
    }

    fun crearMeta(nombre: String, objetivo: Double) {
        val nuevoId = (metas.maxOfOrNull { it.id } ?: 0) + 1
        metas.add(Meta(nuevoId, nombre, 0.0, objetivo))
    }

    fun agregarMovimiento(m: Movimiento) {
        movimientos.add(m)
        animo = if (saldo >= 0) (animo + 0.05f).coerceAtMost(1f) else (animo - 0.15f).coerceAtLeast(0f)
    }

    fun comprar(p: Producto): Boolean {
        if (monedas < p.precio) return false
        monedas -= p.precio
        comprados.add(p.nombre)
        return true
    }
}

