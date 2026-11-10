package com.example.walletpet.integration.biometria

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** INTEGRACIÓN: BIOMETRÍA. Huella / rostro, o el PIN del teléfono como alternativa. */
object BiometricAuth {
    private const val AUTENTICADORES = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    fun disponible(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(AUTENTICADORES) ==
            BiometricManager.BIOMETRIC_SUCCESS

    fun autenticar(
        activity: FragmentActivity,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        val ejecutor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, ejecutor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onExito()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())      // cancelado, demasiados intentos, etc.
            }
            // onAuthenticationFailed: huella no reconocida; el sistema deja reintentar solo
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear WalletPet")
            .setSubtitle("Confirma que eres tú para ver tus ahorros")
            .setAllowedAuthenticators(AUTENTICADORES)
            .build()
        prompt.authenticate(info)
    }
}
