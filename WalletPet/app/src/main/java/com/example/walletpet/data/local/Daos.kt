package com.example.walletpet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// DAOs: aquí viven las consultas SQL. Los Flow avisan solos cuando cambia la tabla.

@Dao
interface MetaDao {
    @Query(
        """
        SELECT m.id, m.nombre, m.objetivo,
               COALESCE(SUM(a.monto), 0) AS ahorrado,
               MAX(a.fechaMillis) AS ultimoAhorro
        FROM metas m LEFT JOIN ahorros a ON a.metaId = m.id
        GROUP BY m.id
        ORDER BY m.creadaMillis
        """
    )
    fun observarConAhorro(): Flow<List<MetaConAhorro>>

    @Insert
    suspend fun insertar(meta: MetaEntity): Long

    @Query("DELETE FROM metas WHERE id = :id")
    suspend fun eliminar(id: Long)
}

@Dao
interface AhorroDao {
    @Insert
    suspend fun insertar(ahorro: AhorroEntity)

    @Query(
        """
        SELECT a.id, m.nombre AS metaNombre, a.monto, a.fechaMillis
        FROM ahorros a INNER JOIN metas m ON m.id = a.metaId
        ORDER BY a.fechaMillis DESC
        """
    )
    fun observarConMeta(): Flow<List<AhorroConMeta>>
}

@Dao
interface MovimientoDao {
    @Query("SELECT * FROM movimientos ORDER BY fechaMillis DESC")
    fun observarTodos(): Flow<List<MovimientoEntity>>

    @Insert
    suspend fun insertar(movimiento: MovimientoEntity)

    @Query("DELETE FROM movimientos WHERE id = :id")
    suspend fun eliminar(id: Long)
}

@Dao
interface MascotaDao {
    @Query("SELECT * FROM mascota WHERE id = 1")
    fun observar(): Flow<MascotaEntity?>

    @Query("SELECT * FROM mascota WHERE id = 1")
    suspend fun obtener(): MascotaEntity?

    @Upsert
    suspend fun guardar(mascota: MascotaEntity)

    @Query("SELECT nombre FROM compras")
    fun observarCompras(): Flow<List<String>>

    @Query("SELECT * FROM compras WHERE nombre = :nombre")
    suspend fun obtenerCompra(nombre: String): CompraEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCompra(compra: CompraEntity)
}
