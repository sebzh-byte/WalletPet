package com.example.walletpet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MetaEntity::class,
        AhorroEntity::class,
        MovimientoEntity::class,
        MascotaEntity::class,
        CompraEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun metaDao(): MetaDao
    abstract fun ahorroDao(): AhorroDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun mascotaDao(): MascotaDao

    companion object {
        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "walletpet.db")
                .build()
    }
}
