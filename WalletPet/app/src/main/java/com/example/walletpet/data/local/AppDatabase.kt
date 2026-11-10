package com.example.walletpet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MetaEntity::class,
        AhorroEntity::class,
        MovimientoEntity::class,
        MascotaEntity::class,
        CompraEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun metaDao(): MetaDao
    abstract fun ahorroDao(): AhorroDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun mascotaDao(): MascotaDao

    companion object {
        /** Versión 2: los gastos ahora tienen nombre. Se agrega la columna sin borrar lo guardado. */
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE movimientos ADD COLUMN nombre TEXT NOT NULL DEFAULT ''")
            }
        }

        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "walletpet.db")
                .addMigrations(MIGRACION_1_2)
                .build()
    }
}
