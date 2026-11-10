package com.example.walletpet.integration.audio

import android.content.Context
import android.media.MediaPlayer
import com.example.walletpet.R

/** INTEGRACIÓN: AUDIO. El gato maúlla al ahorrar y al comer (res/raw/maullido.wav). */
class SonidoManager(private val context: Context) {

    fun maullar() {
        try {
            val player = MediaPlayer.create(context, R.raw.maullido) ?: return
            player.setOnCompletionListener { it.release() }   // libera memoria al terminar
            player.start()
        } catch (e: Exception) {
            // Si el audio falla, la app sigue funcionando sin sonido
        }
    }
}
