package com.example.walletpet.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Tablas de la base de datos local (Room). Cada clase = una tabla.

@Entity(tableName = "metas")
data class MetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val objetivo: Double,
    val creadaMillis: Long = System.currentTimeMillis()
)

/** Cada vez que el usuario registra ahorro en una meta. Si se borra la meta, se borran sus ahorros. */
@Entity(
    tableName = "ahorros",
    foreignKeys = [ForeignKey(
        entity = MetaEntity::class,
        parentColumns = ["id"],
        childColumns = ["metaId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("metaId")]
)
data class AhorroEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val metaId: Long,
    val monto: Double,
    val fechaMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "movimientos")
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoria: String,
    val monto: Double,
    val esIngreso: Boolean,
    val fechaMillis: Long = System.currentTimeMillis()
)

/** Una sola fila (id = 1) con el estado del gato y del jugador. */
@Entity(tableName = "mascota")
data class MascotaEntity(
    @PrimaryKey val id: Int = 1,
    val monedas: Int = 100,
    val nivel: Int = 1,
    val xp: Float = 0f,
    val comida: Float = 0.5f,
    val comidaActualizadaMillis: Long = System.currentTimeMillis(),
    val croquetas: Int = 0
)

/** Objetos de la tienda que ya se compraron (las croquetas no van aquí). */
@Entity(tableName = "compras")
data class CompraEntity(
    @PrimaryKey val nombre: String,
    val fechaMillis: Long = System.currentTimeMillis()
)

// Resultados de consultas con JOIN (no son tablas)

data class MetaConAhorro(
    val id: Long,
    val nombre: String,
    val objetivo: Double,
    val ahorrado: Double,
    val ultimoAhorro: Long?
)

data class AhorroConMeta(
    val id: Long,
    val metaNombre: String,
    val monto: Double,
    val fechaMillis: Long
)
