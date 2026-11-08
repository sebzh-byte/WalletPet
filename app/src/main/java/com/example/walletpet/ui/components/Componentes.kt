package com.example.walletpet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.walletpet.R
import com.example.walletpet.data.AppState
import com.example.walletpet.ui.theme.BarraNav
import com.example.walletpet.ui.theme.Cafe
import com.example.walletpet.ui.theme.Crema
import com.example.walletpet.ui.theme.GrisCard
import com.example.walletpet.ui.theme.Moneda
import com.example.walletpet.ui.theme.Naranja
import com.example.walletpet.ui.theme.Oliva
import com.example.walletpet.ui.theme.OlivaClaro
import com.example.walletpet.ui.theme.OlivaOscuro

/** Barra de arriba: nivel + monedas. Sale en casi todas las pantallas. */
@Composable
fun BarraSuperior(modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.clip(RoundedCornerShape(12.dp)).background(Crema)
                .border(1.dp, Cafe, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nvl ${AppState.nivel}", color = OlivaOscuro, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { AppState.xp },
                modifier = Modifier.width(90.dp),
                color = Moneda,
                trackColor = GrisCard
            )
        }
        Row(
            Modifier.clip(RoundedCornerShape(12.dp)).background(Crema)
                .border(1.dp, Cafe, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painterResource(R.drawable.moneda), contentDescription = null, Modifier.size(22.dp))
            Spacer(Modifier.width(6.dp))
            Text("${AppState.monedas}", color = OlivaOscuro, fontWeight = FontWeight.Bold)
        }
    }
}

/** Barra de abajo con las 4 pestañas (iconos de tu Figma). */
@Composable
fun BarraInferior(seleccion: Int, onSelect: (Int) -> Unit) {
    val iconos = listOf(
        R.drawable.ic_home,
        R.drawable.ic_pata,
        R.drawable.ic_libreta,
        R.drawable.ic_cartera
    )
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(10.dp)
            .clip(RoundedCornerShape(24.dp)).background(BarraNav).padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        iconos.forEachIndexed { i, icono ->
            Box(
                Modifier.size(52.dp).clip(CircleShape)
                    .background(if (i == seleccion) Crema else Color.Transparent)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(icono), contentDescription = null, Modifier.size(30.dp))
            }
        }
    }
}

@Composable
fun BotonOliva(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Oliva, contentColor = Color.White)
    ) { Text(texto, fontWeight = FontWeight.Bold) }
}

@Composable
fun BotonClaro(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Oliva),
        colors = ButtonDefaults.buttonColors(containerColor = OlivaClaro, contentColor = OlivaOscuro)
    ) { Text(texto, fontWeight = FontWeight.Bold) }
}

@Composable
fun Chip(texto: String, activo: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(50))
            .background(if (activo) Oliva else GrisCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            texto,
            color = if (activo) Color.White else OlivaOscuro,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

/** Tarjeta blanca con borde café, la que se repite por toda la app. */
@Composable
fun Tarjeta(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Cafe)
    ) {
        Column(Modifier.padding(14.dp), content = contenido)
    }
}

/** Ventanita emergente (los diálogos de los Frames 12 al 24). */
@Composable
fun DialogoWallet(onCerrar: () -> Unit, contenido: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onCerrar) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Cafe)
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = contenido
            )
        }
    }
}

@Composable
fun TituloDialogo(texto: String) {
    Text(texto, color = Naranja, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
}

@Composable
fun TextoDialogo(texto: String) {
    Text(texto, color = OlivaOscuro, fontSize = 14.sp, textAlign = TextAlign.Center)
}

