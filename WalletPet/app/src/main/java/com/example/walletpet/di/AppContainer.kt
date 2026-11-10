package com.example.walletpet.di

import android.content.Context
import com.example.walletpet.data.local.AppDatabase
import com.example.walletpet.data.remote.api.RetrofitClient
import com.example.walletpet.data.repository.GastoRepositoryImpl
import com.example.walletpet.data.repository.MascotaRepositoryImpl
import com.example.walletpet.data.repository.MetaRepositoryImpl
import com.example.walletpet.data.repository.TipoCambioRepositoryImpl
import com.example.walletpet.domain.repository.GastoRepository
import com.example.walletpet.domain.repository.MascotaRepository
import com.example.walletpet.domain.repository.MetaRepository
import com.example.walletpet.domain.repository.TipoCambioRepository
import com.example.walletpet.domain.usecase.RegistrarAhorroUseCase
import com.example.walletpet.integration.audio.SonidoManager
import com.example.walletpet.integration.pdf.PdfReporter

/**
 * Inyección de dependencias manual: el ÚNICO lugar donde se crean la base de datos,
 * Retrofit, los repositorios y los módulos del dispositivo. Las pantallas nunca los crean.
 * Se inicia una vez en WalletPetApplication.onCreate().
 */
object AppContainer {
    private lateinit var appContext: Context
    private val db: AppDatabase by lazy { AppDatabase.crear(appContext) }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val metaRepository: MetaRepository by lazy { MetaRepositoryImpl(db.metaDao(), db.ahorroDao()) }
    val mascotaRepository: MascotaRepository by lazy { MascotaRepositoryImpl(db.mascotaDao()) }
    val gastoRepository: GastoRepository by lazy { GastoRepositoryImpl(db.movimientoDao()) }
    val tipoCambioRepository: TipoCambioRepository by lazy { TipoCambioRepositoryImpl(RetrofitClient.api) }

    val registrarAhorro: RegistrarAhorroUseCase by lazy {
        RegistrarAhorroUseCase(metaRepository, mascotaRepository)
    }

    val sonidoManager: SonidoManager by lazy { SonidoManager(appContext) }
    val pdfReporter: PdfReporter by lazy { PdfReporter(appContext) }
}
