package com.example.walletpet.presentation.gastos

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.walletpet.R
import com.example.walletpet.core.ui.components.*
import com.example.walletpet.core.ui.theme.*
import com.example.walletpet.domain.model.Catalogo
import com.example.walletpet.domain.model.Movimiento
import com.example.walletpet.domain.model.pesos
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GastosScreen(
    onVerReportes: () -> Unit,
    viewModel: GastosViewModel = viewModel(factory = GastosViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var verTodo by rememberSaveable { mutableStateOf(false) }   // false = Mes, true = Todo
    var dialogoNuevo by remember { mutableStateOf(false) }
    var guardado by remember { mutableStateOf(false) }
    var gastoParaBorrar by remember { mutableStateOf<Movimiento?>(null) }

    // Avisos cortos del ViewModel (ej. "Se eliminó ...")
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { e ->
            if (e is EventoGastos.Mensaje) Toast.makeText(context, e.texto, Toast.LENGTH_SHORT).show()
        }
    }

    Box(Modifier.fillMaxSize().background(Crema)) {
        Image(
            painter = painterResource(R.drawable.fondo_gastos),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Capa verde transparente del diseño
        Box(
            Modifier.fillMaxSize().navigationBarsPadding()
                .padding(top = 190.dp, start = 12.dp, end = 12.dp, bottom = 80.dp)
                .background(Color(0xFF9DB04A).copy(alpha = 0.55f))
        )

        Column(Modifier.fillMaxSize().espacioBarraInferior()) {
            BarraSuperior(state.barra)

            Tarjeta(
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                colorBorde = OlivaBorde, grosor = 2.dp, radio = 14.dp
            ) {
                Text(
                    "Gastos",
                    color = OlivaOscuro,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Chip("Mes", !verTodo, Modifier.weight(1f)) { verTodo = false }
                    Chip("Todo", verTodo, Modifier.weight(1f)) { verTodo = true }
                    Chip("Reporte", false, Modifier.weight(1f)) { onVerReportes() }
                }
            }

            val vacio = if (verTodo) state.listaGastos.isEmpty() else state.gastosDelMes.isEmpty()
            if (!state.cargando && vacio) {
                Tarjeta(
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                    colorBorde = OlivaBorde, grosor = 2.dp, radio = 14.dp
                ) {
                    Text(
                        if (verTodo) "Aún no registras gastos." else "No hay gastos este mes.",
                        color = OlivaOscuro,
                        fontFamily = FuenteTitulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text("Toca el botón + para agregar uno.", color = OlivaOscuro, fontSize = 13.sp)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(if (verTodo) 10.dp else 14.dp)
            ) {
                if (verTodo) {
                    // "Todo": cada gasto con su nombre, categoría, fecha y monto
                    items(state.listaGastos, key = { it.id }) { gasto ->
                        TarjetaGasto(gasto, onBorrar = { gastoParaBorrar = gasto })
                    }
                } else {
                    // "Mes": resumen por categoría (como en el diseño)
                    items(state.gastosDelMes, key = { it.first }) { (categoria, total) ->
                        Tarjeta(
                            Modifier.fillMaxWidth(),
                            colorBorde = OlivaBorde, grosor = 2.dp, radio = 14.dp
                        ) {
                            Text(
                                categoria,
                                color = OlivaOscuro,
                                fontFamily = FuenteTitulo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(total.pesos(), color = Amarillo, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { dialogoNuevo = true },
            modifier = Modifier.align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 100.dp),
            containerColor = Oliva,
            contentColor = Color.White
        ) { Text("+", fontSize = 26.sp) }
    }

    if (dialogoNuevo) {
        DialogoNuevoMovimiento(
            onCerrar = { dialogoNuevo = false },
            onGuardar = { esIngreso, nombre, monto, categoria ->
                val error = viewModel.agregarMovimiento(esIngreso, nombre, monto, categoria)
                if (error == null) guardado = true
                error
            }
        )
    }

    if (guardado) {
        DialogoAviso(null, "¡Movimiento guardado!", { guardado = false })
    }

    gastoParaBorrar?.let { gasto ->
        DialogoWallet({ gastoParaBorrar = null }) {
            TituloDialogo("¿Eliminar este gasto?")
            Spacer(Modifier.height(8.dp))
            TextoDialogo("${gasto.nombre.ifEmpty { gasto.categoria }} · ${gasto.monto.pesos()}")
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonClaro("Cancelar", { gastoParaBorrar = null }, Modifier.weight(1f))
                BotonOliva("Eliminar", {
                    viewModel.eliminarMovimiento(gasto)
                    gastoParaBorrar = null
                }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TarjetaGasto(gasto: Movimiento, onBorrar: () -> Unit) {
    val fecha = remember(gasto.fechaMillis) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(gasto.fechaMillis))
    }
    Tarjeta(Modifier.fillMaxWidth(), colorBorde = OlivaBorde, grosor = 2.dp, radio = 14.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    // Los gastos viejos (sin nombre) muestran su categoría
                    gasto.nombre.ifEmpty { gasto.categoria },
                    color = OlivaOscuro,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${gasto.categoria} · $fecha", color = TextoGris, fontSize = 11.sp)
                Text(gasto.monto.pesos(), color = Amarillo, fontSize = 20.sp)
            }
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(GrisPista).clickable { onBorrar() },
                contentAlignment = Alignment.Center
            ) { Text("✕", color = GrisIcono, fontSize = 14.sp) }
        }
    }
}

// Botón "+": registrar un gasto (con nombre) o un ingreso
@Composable
private fun DialogoNuevoMovimiento(
    onCerrar: () -> Unit,
    onGuardar: (esIngreso: Boolean, nombre: String, monto: String, categoria: String) -> String?
) {
    var esIngreso by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    DialogoWallet(onCerrar) {
        TituloDialogo("Nuevo movimiento")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Gasto", !esIngreso) { esIngreso = false; error = null }
            Chip("Ingreso", esIngreso) { esIngreso = true; error = null }
        }
        Spacer(Modifier.height(10.dp))
        CampoWallet(
            nombre,
            { nombre = it; error = null },
            if (esIngreso) "Nombre (opcional, ej. Beca)" else "Nombre del gasto (ej. Tacos)"
        )
        Spacer(Modifier.height(8.dp))
        CampoWallet(monto, { monto = it; error = null }, "Monto", decimal = true)
        if (!esIngreso) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Categoría (opcional)",
                modifier = Modifier.fillMaxWidth(),
                color = OlivaOscuro.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(4.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Catalogo.categoriasGasto.forEach { c ->
                    // Tocar otra vez la misma categoría la quita
                    Chip(c, categoria == c) { categoria = if (categoria == c) "" else c; error = null }
                }
            }
        }
        error?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, color = Naranja, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonClaro("Cancelar", onCerrar, Modifier.weight(1f))
            BotonOliva("Guardar", {
                error = onGuardar(esIngreso, nombre, monto, categoria)
                if (error == null) onCerrar()
            }, Modifier.weight(1f))
        }
    }
}
