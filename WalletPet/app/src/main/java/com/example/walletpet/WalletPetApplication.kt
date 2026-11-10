package com.example.walletpet

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.walletpet.di.AppContainer

/**
 * Se ejecuta antes que cualquier pantalla (registrada en AndroidManifest con android:name).
 * - Inicia el contenedor de dependencias.
 * - Configura Coil para que pueda mostrar GIFs animados del gato.
 */
class WalletPetApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }
            .build()
}
