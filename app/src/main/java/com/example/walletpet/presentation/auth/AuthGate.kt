package com.example.walletpet.presentation.auth

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.walletpet.R
import com.example.walletpet.core.ui.components.BotonOliva
import com.example.walletpet.core.ui.theme.*
import com.example.walletpet.integration.biometria.BiometricAuth

/**
 * Envuelve la app: solo muestra el contenido después de la huella / PIN.
 * Si el teléfono no tiene huella ni PIN configurado, deja pasar para no bloquear la app.
 */
@Composable
fun AuthGate(
    authViewModel: AuthViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val desbloqueada by authViewModel.desbloqueada.collectAsStateWithLifecycle()
    val mensaje by authViewModel.mensaje.collectAsStateWithLifecycle()
    val activity = remember(context) { context.buscarFragmentActivity() }

    fun pedirHuella() {
        if (activity == null || !BiometricAuth.disponible(context)) {
            authViewModel.alExito()
            return
        }
        BiometricAuth.autenticar(
            activity = activity,
            onExito = { authViewModel.alExito() },
            onError = { authViewModel.alError(it) }
        )
    }

    if (desbloqueada) {
        content()
    } else {
        LaunchedEffect(Unit) { pedirHuella() }
        PantallaBloqueo(mensaje = mensaje, onReintentar = { pedirHuella() })
    }
}

@Composable
private fun PantallaBloqueo(mensaje: String?, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Crema).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painterResource(R.drawable.gato_normal), contentDescription = null, Modifier.height(180.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            "Tu gatito está protegido",
            color = Naranja,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Confirma tu identidad con tu huella o PIN para ver tus ahorros.",
            color = OlivaOscuro,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        mensaje?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = RosaHambre, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(20.dp))
        BotonOliva("Desbloquear", onReintentar, Modifier.fillMaxWidth(0.6f).height(48.dp))
    }
}

private fun Context.buscarFragmentActivity(): FragmentActivity? {
    var actual = this
    while (actual is ContextWrapper) {
        if (actual is FragmentActivity) return actual
        actual = actual.baseContext
    }
    return null
}
