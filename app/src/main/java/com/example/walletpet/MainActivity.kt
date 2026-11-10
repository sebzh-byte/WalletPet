package com.example.walletpet

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.walletpet.core.ui.theme.WalletPetTheme
import com.example.walletpet.navigation.WalletPetApp

// FragmentActivity (y no ComponentActivity) porque la huella (BiometricPrompt) lo necesita.
class MainActivity : FragmentActivity() {
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
