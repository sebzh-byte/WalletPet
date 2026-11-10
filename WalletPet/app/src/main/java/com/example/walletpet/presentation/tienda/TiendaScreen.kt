package com.example.walletpet.presentation.tienda

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.walletpet.R
import com.example.walletpet.core.ui.components.BarraSuperior
import com.example.walletpet.core.ui.components.BotonClaro
import com.example.walletpet.core.ui.components.BotonOliva
import com.example.walletpet.core.ui.components.DialogoAviso
import com.example.walletpet.core.ui.components.DialogoWallet
import com.example.walletpet.core.ui.components.TextoDialogo
import com.example.walletpet.core.ui.components.TituloDialogo
import com.example.walletpet.core.ui.theme.BordeTan
import com.example.walletpet.core.ui.theme.Crema
import com.example.walletpet.core.ui.theme.Durazno
import com.example.walletpet.core.ui.theme.GrisBloq
import com.example.walletpet.core.ui.theme.GrisIcono
import com.example.walletpet.core.ui.theme.Naranja
import com.example.walletpet.core.ui.theme.Oliva
import com.example.walletpet.core.ui.theme.OlivaOscuro
import com.example.walletpet.domain.model.Catalogo
import com.example.walletpet.domain.model.Producto
import com.example.walletpet.domain.model.ReglasMascota
import com.example.walletpet.domain.model.ResultadoCompra

// Busca la imagen de cada producto. Si no tiene, muestra su emoji.
@Composable
private fun ImagenProducto(nombre: String, emoji: String, tamano: Dp) {
    val recurso = when (nombre) {
        Catalogo.CROQUETAS -> R.drawable.item_croquetas
        "Arenero" -> R.drawable.item_arenero
        "Cama" -> R.drawable.item_cama
        "Planta" -> R.drawable.item_planta
        "Cojín gatito" -> R.drawable.item_cojin_gato
        "Tapete osito" -> R.drawable.item_tapete_oso
        "Cojín nube" -> R.drawable.item_cojin_nube
        "Cojín sandía" -> R.drawable.item_cojin_sandia
        "Peluche gatito" -> R.drawable.item_peluche_gato
        else -> null
    }
    if (recurso != null) {
        Image(painterResource(recurso), contentDescription = nombre, modifier = Modifier.size(tamano))
    } else {
        Text(emoji, fontSize = (tamano.value * 0.7f).sp)
    }
}

@Composable
fun TiendaScreen(viewModel: TiendaViewModel = viewModel(factory = TiendaViewModel.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var seleccionado by remember { mutableStateOf<Producto?>(null) }
    var bloqueado by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Durazno, Crema)))) {
        Column(Modifier.fillMaxSize()) {
            // Imagen del cuarto con la barra de nivel encima y el gatito echado en el tapete
            Box {
                Image(
                    painter = painterResource(R.drawable.fondo_tienda),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentScale = ContentScale.Crop
                )
                BarraSuperior(state.barra)
                Image(
                    painter = painterResource(R.drawable.gato_echado),
                    contentDescription = "Gatito echado",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)   // abajo, al centro (sobre el tapete)
                        .offset(x = 20.dp, y = (-20).dp) // x: + derecha / - izquierda · y: - sube / + baja
                        .height(120.dp)                  // tamaño del gato (más grande = número más alto)
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.productos, key = { it.producto.nombre }) { item ->
                    val p = item.producto
                    val candado = item.bloqueado
                    Box(
                        Modifier.aspectRatio(0.88f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (candado) GrisBloq else Color.White)
                            .border(1.dp, if (candado) Color(0xFFCFCFCF) else BordeTan, RoundedCornerShape(8.dp))
                            .clickable {
                                if (candado) bloqueado = true
                                else if (!item.loTiene) seleccionado = p
                            }
                            .padding(8.dp)
                    ) {
                        if (candado) {
                            Text(
                                "Nvl ${p.nivelRequerido}",
                                modifier = Modifier.align(Alignment.TopCenter),
                                color = GrisIcono,
                                fontSize = 13.sp
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_candado),
                                contentDescription = "Bloqueado",
                                modifier = Modifier.align(Alignment.Center).size(46.dp)
                            )
                        } else {
                            Row(Modifier.align(Alignment.TopStart), verticalAlignment = Alignment.CenterVertically) {
                                Image(painterResource(R.drawable.moneda), contentDescription = null, Modifier.size(18.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("${p.precio}", color = Naranja, fontSize = 12.sp)
                            }
                            Box(Modifier.align(Alignment.Center)) {
                                ImagenProducto(p.nombre, p.emoji, 64.dp)
                            }
                            if (item.loTiene) {
                                Text(
                                    "✔",
                                    modifier = Modifier.align(Alignment.BottomEnd),
                                    color = OlivaOscuro,
                                    fontSize = 14.sp
                                )
                            }
                            // Las croquetas muestran cuántas porciones te quedan
                            if (p.consumible && state.croquetas > 0) {
                                Text(
                                    "x${state.croquetas}",
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                        .clip(RoundedCornerShape(6.dp)).background(Oliva)
                                        .padding(horizontal = 6.dp, vertical = 1.dp),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmar compra
    seleccionado?.let { p ->
        DialogoWallet({ seleccionado = null }) {
            TituloDialogo("Confirmar compra 🐾")
            Spacer(Modifier.height(10.dp))
            ImagenProducto(p.nombre, p.emoji, 90.dp)
            if (p.consumible) {
                Spacer(Modifier.height(6.dp))
                TextoDialogo("Trae ${ReglasMascota.PORCIONES_POR_BOLSA} porciones para darle de comer a tu gatito sin gastar monedas.")
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonClaro("Sí \$${p.precio}", {
                    seleccionado = null
                    viewModel.comprar(p)
                }, Modifier.weight(1f).height(48.dp))
                BotonOliva("No, gracias", { seleccionado = null }, Modifier.weight(1f).height(48.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "¿Estás seguro de que deseas adquirir este producto para tu mascota?",
                color = OlivaOscuro,
                fontSize = 10.sp
            )
        }
    }

    // Objeto bloqueado
    if (bloqueado) {
        DialogoWallet({ bloqueado = false }) {
            TituloDialogo("¡Objeto bloqueado!")
            Spacer(Modifier.height(8.dp))
            Image(
                painter = painterResource(R.drawable.ic_candado),
                contentDescription = "Bloqueado",
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(8.dp))
            BotonOliva("Entendido", { bloqueado = false }, Modifier.fillMaxWidth(0.7f).height(44.dp))
            Spacer(Modifier.height(10.dp))
            TextoDialogo("Aún no puedes comprar este objeto. ¡Sube de nivel para poder conseguirlo!")
        }
    }

    // Resultado de la compra (lo decide el ViewModel/Repositorio)
    when (val r = state.resultado) {
        null -> Unit
        is ResultadoCompra.Comprado -> DialogoAviso(
            "¡Compra realizada! 🐾",
            if (r.producto.consumible) "Ahora tienes ${r.croquetas} porciones de croquetas. Dale de comer desde el inicio tocando a tu gatito."
            else "${r.producto.nombre} ya es de tu gatito.",
            viewModel::cerrarResultado
        )
        ResultadoCompra.SinMonedas -> DialogoAviso(
            null, "No tienes monedas suficientes para comprar esto. ¡Sigue ahorrando!", viewModel::cerrarResultado
        )
        ResultadoCompra.Bloqueado -> DialogoAviso(
            "¡Objeto bloqueado!", "Sube de nivel para poder conseguirlo.", viewModel::cerrarResultado
        )
        ResultadoCompra.YaLoTienes -> DialogoAviso(
            null, "Tu gatito ya tiene este objeto.", viewModel::cerrarResultado
        )
    }
}
