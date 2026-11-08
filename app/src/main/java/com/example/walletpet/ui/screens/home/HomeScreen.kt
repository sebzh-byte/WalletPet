package com.example.walletpet.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletpet.R
import com.example.walletpet.data.AppState
import com.example.walletpet.data.pesos
import com.example.walletpet.ui.components.BarraSuperior
import com.example.walletpet.ui.components.BotonClaro
import com.example.walletpet.ui.components.BotonOliva
import com.example.walletpet.ui.components.DialogoWallet
import com.example.walletpet.ui.components.Tarjeta
import com.example.walletpet.ui.components.TextoDialogo
import com.example.walletpet.ui.components.TituloDialogo
import com.example.walletpet.ui.theme.Durazno
import com.example.walletpet.ui.theme.GrisCard
import com.example.walletpet.ui.theme.Oliva
import com.example.walletpet.ui.theme.OlivaOscuro

@Composable
fun HomeScreen() {
    var mostrarEstado by remember { mutableStateOf(false) }
    var dialogoComer by remember { mutableStateOf(false) }
    var dialogoLleno by remember { mutableStateOf(false) }
    var dialogoAhorro by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf<String?>(null) }
    val meta = AppState.metas.firstOrNull()

    Box(Modifier.fillMaxSize().background(Durazno)) {
        // Fondo: cambia cuando se abre el panel de comida (Frame 4)
        Image(
            painter = painterResource(
                if (mostrarEstado) R.drawable.fondo_alimentar else R.drawable.fondo_home
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(Modifier.fillMaxSize()) {
            BarraSuperior()

            if (mostrarEstado) {
                // Frame 4: panel de estado de la mascota
                Tarjeta(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Text("Comida", color = OlivaOscuro, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { AppState.comida },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = Oliva, trackColor = GrisCard
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Ánimo", color = OlivaOscuro, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { AppState.animo },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = Oliva, trackColor = GrisCard
                    )
                    Spacer(Modifier.height(10.dp))
                    BotonOliva("Alimentar", {
                        if (AppState.comida >= 0.8f) {
                            dialogoLleno = true
                        } else {
                            dialogoComer = true
                        }
                    })
                }
            } else if (meta != null) {
                // Frame 1: meta activa
                Card(
                    Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Oliva.copy(alpha = 0.35f))
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { meta.progreso },
                                modifier = Modifier.size(90.dp),
                                strokeWidth = 8.dp, color = Oliva, trackColor = Color.White
                            )
                            Text(
                                "${(meta.progreso * 100).toInt()}%",
                                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OlivaOscuro
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Meta activa", color = OlivaOscuro, fontSize = 12.sp)
                            Text(meta.nombre, color = OlivaOscuro, fontWeight = FontWeight.Bold)
                            Text("Faltan ${(meta.objetivo - meta.ahorrado).pesos()}", color = OlivaOscuro, fontSize = 12.sp)
                        }
                    }
                }
                BotonOliva(
                    "Registrar Ahorro", { dialogoAhorro = true },
                    Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Oliva)
                        .clickable { mostrarEstado = !mostrarEstado },
                    contentAlignment = Alignment.Center
                ) { Text("📊", fontSize = 20.sp) }
            }

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text("Hola, meow", Modifier.padding(12.dp), color = OlivaOscuro, fontWeight = FontWeight.Bold)
                }
                // Tu gatito. Cuando tengas gato_triste, cámbialo aquí abajo.
                Image(
                    painter = painterResource(R.drawable.gato_normal),
                    contentDescription = "Gatito",
                    modifier = Modifier.height(260.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    // Frame 13: ¡Hora de comer!
    if (dialogoComer) {
        DialogoWallet({ dialogoComer = false }) {
            TituloDialogo("¡Hora de comer!")
            Spacer(Modifier.height(8.dp))
            TextoDialogo("Tu gatito tiene un poco de hambre. ¿Quieres alimentarlo usando tus puntos de ahorro?")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonClaro("¡Sí, darle de comer!", {
                    dialogoComer = false
                    if (!AppState.alimentar()) aviso = "No tienes puntos de ahorro suficientes para darle de comer."
                })
                BotonOliva("Tal vez después", {
                    dialogoComer = false
                    aviso = "Tu gatito esperará pacientemente a su próxima comida"
                })
            }
        }
    }

    // Frame 14: ya está lleno
    if (dialogoLleno) {
        DialogoWallet({ dialogoLleno = false }) {
            TituloDialogo("¡Gatito con la pancita llena!")
            Spacer(Modifier.height(8.dp))
            TextoDialogo("Tu mascota ya comió lo suficiente por ahora y no tiene hambre. ¡Vuelve más tarde para consentirlo otra vez!")
            Spacer(Modifier.height(12.dp))
            BotonClaro("¡Entendido!", { dialogoLleno = false })
        }
    }

    // Frame 21: mensajes cortos
    aviso?.let { texto ->
        DialogoWallet({ aviso = null }) {
            TextoDialogo(texto)
            Spacer(Modifier.height(12.dp))
            BotonClaro("Entendido", { aviso = null })
        }
    }

    // Registrar ahorro (con validación de letras, REQ-2)
    if (dialogoAhorro && meta != null) {
        var monto by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        DialogoWallet({ dialogoAhorro = false }) {
            TituloDialogo("Registrar ahorro")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it; error = null },
                label = { Text("Monto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = error != null,
                supportingText = { error?.let { Text(it) } }
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonClaro("Cancelar", { dialogoAhorro = false })
                BotonOliva("Guardar", {
                    val v = monto.toDoubleOrNull()
                    if (v == null || v <= 0) {
                        error = "Por favor, ingresa un monto válido"
                    } else {
                        AppState.registrarAhorro(meta.id, v)
                        dialogoAhorro = false
                    }
                })
            }
        }
    }
}