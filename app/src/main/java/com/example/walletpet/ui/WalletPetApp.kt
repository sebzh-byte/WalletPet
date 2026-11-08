package com.example.walletpet.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.walletpet.ui.components.BarraInferior
import com.example.walletpet.ui.screens.GastosScreen
import com.example.walletpet.ui.screens.home.HomeScreen
import com.example.walletpet.ui.screens.MetasScreen
import com.example.walletpet.ui.screens.SplashScreen
import com.example.walletpet.ui.screens.TiendaScreen
import com.example.walletpet.ui.theme.Crema

@Composable
fun WalletPetApp() {
    var mostrandoSplash by rememberSaveable { mutableStateOf(true) }
    var pestana by rememberSaveable { mutableStateOf(0) }

    if (mostrandoSplash) {
        SplashScreen { mostrandoSplash = false }
    } else {
        Scaffold(
            containerColor = Crema,
            bottomBar = { BarraInferior(pestana) { pestana = it } }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (pestana) {
                    0 -> HomeScreen()
                    1 -> TiendaScreen()
                    2 -> MetasScreen()
                    else -> GastosScreen()
                }
            }
        }
    }
}

