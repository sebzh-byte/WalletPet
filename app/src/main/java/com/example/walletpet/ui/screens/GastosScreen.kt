package com.example.walletpet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletpet.R
import com.example.walletpet.data.AppState
import com.example.walletpet.data.Movimiento
import com.example.walletpet.data.Reporte
import com.example.walletpet.data.mesActual
import com.example.walletpet.data.pesos
import com.example.walletpet.ui.components.BarraSuperior
import com.example.walletpet.ui.components.BotonClaro
import com.example.walletpet.ui.components.BotonOliva
import com.example.walletpet.ui.components.Chip
import com.example.walletpet.ui.components.DialogoWallet
import com.example.walletpet.ui.components.Tarjeta
import com.example.walletpet.ui.components.TituloDialogo
import com.example.walletpet.ui.theme.Cafe
import com.example.walletpet.ui.theme.Crema
import com.example.walletpet.ui.theme.Naranja
import com.example.walletpet.ui.theme.Oliva
import com.example.walletpet.ui.theme.OlivaOscuro

@Composable
fun GastosScreen() {
    var pestana by remember { mutableStateOf(0) }   // 0 = Mes, 1 = Todo, 2 = Reporte
    var reporteAbierto by remember { mutableStateOf<Reporte?>(null) }
    var dialogoNuevo by remember { mutableStateOf(false) }
    val abierto = reporteAbierto

    Box(Modifier.fillMaxSize().background(Crema)) {
        // Fondo verde (solo en Mes y Todo)
        if (pestana != 2 && abierto == null) {
            Image(
                painter = painterResource(R.drawable.fondo_gastos),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(Modifier.fillMaxSize()) {
            BarraSuperior()

            if (abierto != null) {
                DetalleReporte(abierto) { reporteAbierto = null }
            } else {
                Tarjeta(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Text("Gastos", color = OlivaOscuro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip("Mes", pestana == 0) { pestana = 0 }
                        Chip("Todo", pestana == 1) { pestana = 1 }
                        Chip("Reporte", pestana == 2) { pestana = 2 }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Saldo: ${AppState.saldo.pesos()}", color = OlivaOscuro, fontSize = 13.sp)
                }

                if (pestana == 2) {
                    // Frame 8: lista de reportes
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(AppState.reportes) { r ->
                            Tarjeta(Modifier.fillMaxWidth().clickable { reporteAbierto = r }) {
                                Text(r.titulo, color = Naranja, fontWeight = FontWeight.Bold)
                                Text(r.resumen, color = OlivaOscuro, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // Frames 7 y 10: gastos por categoría
                    val lista = AppState.movimientos
                        .filter { !it.esIngreso && (pestana == 1 || it.mes == mesActual()) }
                        .groupBy { it.categoria }
                        .map { (categoria, movs) -> categoria to movs.sumOf { it.monto } }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(lista) { (categoria, total) ->
                            Tarjeta(Modifier.fillMaxWidth()) {
                                Text(categoria, color = OlivaOscuro, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(total.pesos(), color = Naranja, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        if (abierto == null && pestana != 2) {
            FloatingActionButton(
                onClick = { dialogoNuevo = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = Oliva,
                contentColor = Color.White
            ) { Text("+", fontSize = 26.sp) }
        }
    }

    if (dialogoNuevo) {
        DialogoNuevoMovimiento { dialogoNuevo = false }
    }
}

// Pantalla del Frame 9: detalle del reporte
@Composable
private fun DetalleReporte(r: Reporte, onVolver: () -> Unit) {
    var confirmar by remember { mutableStateOf(false) }
    var guardado by remember { mutableStateOf(false) }

    Tarjeta(Modifier.padding(16.dp).fillMaxWidth()) {
        Text("← Volver", color = Cafe, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onVolver() })
        Spacer(Modifier.height(8.dp))
        Text(r.titulo, color = Naranja, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            r.detalle, color = OlivaOscuro, fontSize = 13.sp,
            modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())
        )
        Spacer(Modifier.height(12.dp))
        BotonOliva("Descargar PDF", { confirmar = true }, Modifier.align(Alignment.CenterHorizontally))
    }

    // Frame 17
    if (confirmar) {
        DialogoWallet({ confirmar = false }) {
            TituloDialogo("Deseas descargar Reporte: ${r.titulo}?")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonClaro("Descargar", {
                    confirmar = false
                    guardado = true    // OJO: por ahora solo simula la descarga
                })
                BotonOliva("Cancelar", { confirmar = false })
            }
        }
    }

    // Frame 18
    if (guardado) {
        DialogoWallet({ guardado = false }) {
            TituloDialogo("¡Reporte guardado con éxito! 🐾")
            Spacer(Modifier.height(8.dp))
            Text("Tu gatito guardó una copia de tu avance en tu dispositivo. ¡Sigue así!", color = OlivaOscuro, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            BotonClaro("Entendido", { guardado = false })
        }
    }
}

// Botón "+" : registrar un gasto o ingreso (REQ-1 y REQ-2 del documento)
@Composable
private fun DialogoNuevoMovimiento(onCerrar: () -> Unit) {
    var esIngreso by remember { mutableStateOf(false) }
    var monto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val categorias = listOf("Transporte", "Alimentación", "Escuela y Papelería", "Entretenimiento", "Celular / Internet")

    DialogoWallet(onCerrar) {
        TituloDialogo("Nuevo movimiento")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Gasto", !esIngreso) { esIngreso = false }
            Chip("Ingreso", esIngreso) { esIngreso = true }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = monto, onValueChange = { monto = it; error = null },
            label = { Text("Monto") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            categorias.forEach { c -> Chip(c, categoria == c) { categoria = c; error = null } }
        }
        error?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, color = Naranja, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BotonClaro("Cancelar", onCerrar)
            BotonOliva("Guardar", {
                val v = monto.toDoubleOrNull()
                if (v == null || v <= 0) {
                    error = if (esIngreso) "Por favor, ingresa el monto del ingreso" else "Por favor, ingresa el monto del gasto"
                } else if (categoria.isEmpty()) {
                    error = "Por favor, elige una categoría"
                } else {
                    AppState.agregarMovimiento(Movimiento(categoria, v, esIngreso, mesActual()))
                    onCerrar()
                }
            })
        }
    }
}