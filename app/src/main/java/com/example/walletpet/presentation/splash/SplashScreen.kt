package com.example.walletpet.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletpet.R
import com.example.walletpet.core.ui.theme.VerdeSplash1
import com.example.walletpet.core.ui.theme.VerdeSplash2
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTermina: () -> Unit) {
    // Espera 2 segundos y avisa que ya terminó
    LaunchedEffect(Unit) {
        delay(2000)
        onTermina()
    }
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(VerdeSplash1, VerdeSplash2))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.gato_normal),
                contentDescription = "WalletPet",
                modifier = Modifier.height(220.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("WALLET PET", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
