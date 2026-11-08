package com.example.walletpet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletpet.R
import com.example.walletpet.data.AppState
import com.example.walletpet.data.Producto
import com.example.walletpet.ui.components.BarraSuperior
import com.example.walletpet.ui.components.BotonClaro
import com.example.walletpet.ui.components.BotonOliva
import com.example.walletpet.ui.components.DialogoWallet
import com.example.walletpet.ui.components.TextoDialogo
import com.example.walletpet.ui.components.TituloDialogo
import com.example.walletpet.ui.theme.Cafe
import com.example.walletpet.ui.theme.Crema
import com.example.walletpet.ui.theme.GrisCard
import com.example.walletpet.ui.theme.OlivaOscuro

// Busca la imagen de cada producto. Si no tiene, muestra su emoji.
@Composable
private fun ImagenProducto(nombre: String, emoji: String, tamano: Dp) {
    val recurso = when (nombre) {
        "Croquetas" -> R.drawable.item_croquetas
        "Arenero" -> R.drawable.item_arenero
        "Cama" -> R.drawable.item_cama
        "Planta" -> R.drawable.item_planta
        else -> null
    }
    if (recurso != null) {
        Image(
            painter = painterResource(recurso),
            contentDescription = nombre,
            modifier = Modifier.size(tamano)
        )
    } else {
        Text(emoji, fontSize = (tamano.value * 0.75f).sp)
    }
}

@Composable
fun TiendaScreen() {
    var seleccionado by remember { mutableStateOf<Producto?>(null) }
    var bloqueado by remember { mutableStateOf(false) }
    var sinPuntos by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Crema)) {
        BarraSuperior()

        // Imagen de arriba: el cuarto del gatito
        Image(
            painter = painterResource(R.drawable.fondo_tienda),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(160.dp),
            contentScale = ContentScale.Crop
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(AppState.productos) { p ->
                val candado = p.nivelRequerido > AppState.nivel
                val yaLoTengo = p.nombre in AppState.comprados
                Card(
                    Modifier.aspectRatio(1f).clickable {
                        if (candado) {
                            bloqueado = true
                        } else if (!yaLoTengo) {
                            seleccionado = p
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (candado) GrisCard else Color.White)
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (candado) {
                            Image(
                                painter = painterResource(R.drawable.ic_candado),
                                contentDescription = "Bloqueado",
                                modifier = Modifier.size(36.dp)
                            )
                            Text("Nvl ${p.nivelRequerido}", color = OlivaOscuro, fontSize = 12.sp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(R.drawable.moneda),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text("${p.precio}", color = Cafe, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            ImagenProducto(p.nombre, p.emoji, 48.dp)
                            if (yaLoTengo) Text("✔ Tuyo", color = OlivaOscuro, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // Frames 12, 15, 20, 22: confirmar compra
    seleccionado?.let { p ->
        DialogoWallet({ seleccionado = null }) {
            TituloDialogo("Confirmar compra 🐾")
            Spacer(Modifier.height(8.dp))
            ImagenProducto(p.nombre, p.emoji, 80.dp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonClaro("Sí \$${p.precio}", {
                    seleccionado = null
                    if (!AppState.comprar(p)) sinPuntos = true
                })
                BotonOliva("No, gracias", { seleccionado = null })
            }
            Spacer(Modifier.height(8.dp))
            TextoDialogo("¿Estás seguro de que deseas adquirir este producto para tu mascota?")
        }
    }

    // Frames 23 y 24: objeto bloqueado
    if (bloqueado) {
        DialogoWallet({ bloqueado = false }) {
            TituloDialogo("¡Objeto bloqueado!")
            Spacer(Modifier.height(8.dp))
            Image(
                painter = painterResource(R.drawable.ic_candado),
                contentDescription = "Bloqueado",
                modifier = Modifier.size(60.dp)
            )
            Spacer(Modifier.height(8.dp))
            BotonOliva("Entendido", { bloqueado = false })
            Spacer(Modifier.height(8.dp))
            TextoDialogo("Aún no puedes comprar este objeto. ¡Sube de nivel para poder conseguirlo!")
        }
    }

    if (sinPuntos) {
        DialogoWallet({ sinPuntos = false }) {
            TextoDialogo("No tienes puntos de ahorro suficientes para comprar esto. ¡Sigue ahorrando!")
            Spacer(Modifier.height(12.dp))
            BotonClaro("Entendido", { sinPuntos = false })
        }
    }
}