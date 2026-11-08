package com.example.walletpet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.walletpet.ui.WalletPetApp
import com.example.walletpet.ui.theme.WalletPetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WalletPetTheme {
                WalletPetApp()
            }
        }
    }
}