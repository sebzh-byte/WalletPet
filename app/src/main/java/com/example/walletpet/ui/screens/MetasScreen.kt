package com.example.walletpet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.walletpet.ui.theme.Crema
import com.example.walletpet.ui.theme.GrisCard
import com.example.walletpet.ui.theme.Oliva
import com.example.walletpet.ui.theme.OlivaOscuro

@Composable
fun MetasScreen() {
    var dialogoNueva by remember { mutableStateOf(false) }
    var creada by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Crema)) {
        BarraSuperior()

        // Imagen de arriba: el gatito en la rama
        Image(
            painter = painterResource(R.drawable.fondo_metas),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentScale = ContentScale.Crop
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(AppState.metas) { m ->
                Tarjeta(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(m.nombre, color = OlivaOscuro, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${m.ahorrado.pesos()}/${m.objetivo.pesos()}", color = OlivaOscuro, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { m.progreso },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = Oliva, trackColor = GrisCard
                    )
                    Text(
                        "${(m.progreso * 100).toInt()}%",
                        color = OlivaOscuro, fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
            item {
                BotonOliva("Agregar nueva meta", { dialogoNueva = true }, Modifier.fillMaxWidth())
            }
        }
    }

    // Frame 11: Nueva meta
    if (dialogoNueva) {
        var nombre by remember { mutableStateOf("") }
        var objetivo by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        DialogoWallet({ dialogoNueva = false }) {
            TituloDialogo("Nueva meta")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it; error = null },
                label = { Text("Nombre de la meta") }, singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = objetivo, onValueChange = { objetivo = it; error = null },
                label = { Text("Monto objetivo") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = error != null,
                supportingText = { error?.let { Text(it) } }
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonClaro("Cancelar", { dialogoNueva = false })
                BotonOliva("Crear meta", {
                    val v = objetivo.toDoubleOrNull()
                    if (nombre.isBlank()) {
                        error = "Por favor, ingresa el nombre de la meta"
                    } else if (v == null || v <= 0) {
                        error = "Por favor, ingresa un monto válido"
                    } else {
                        AppState.crearMeta(nombre.trim(), v)
                        dialogoNueva = false
                        creada = true
                    }
                })
            }
        }
    }

    // Frames 16 y 19: meta creada
    if (creada) {
        DialogoWallet({ creada = false }) {
            TituloDialogo("¡Nueva meta creada!")
            Spacer(Modifier.height(8.dp))
            TextoDialogo("Tu meta se ha registrado con éxito. ¡A partir de hoy tu gatito te acompañará a lograrla paso a paso!")
            Spacer(Modifier.height(12.dp))
            BotonClaro("¡Comenzar a ahorrar!", { creada = false })
        }
    }
}

