package com.example.walletpet.presentation.gastos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.walletpet.R
import com.example.walletpet.core.ui.components.*
import com.example.walletpet.core.ui.theme.*
import com.example.walletpet.domain.model.Catalogo
import com.example.walletpet.domain.model.pesos

@Composable
fun GastosScreen(
    onVerReportes: () -> Unit,
    viewModel: GastosViewModel = viewModel(factory = GastosViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var verTodo by rememberSaveable { mutableStateOf(false) }   // false = Mes, true = Todo
    var dialogoNuevo by remember { mutableStateOf(false) }
    var guardado by remember { mutableStateOf(false) }

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

            val lista = if (verTodo) state.gastosTotales else state.gastosDelMes

            if (!state.cargando && lista.isEmpty()) {
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(lista, key = { it.first }) { (categoria, total) ->
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
            onGuardar = { esIngreso, monto, categoria ->
                val error = viewModel.agregarMovimiento(esIngreso, monto, categoria)
                if (error == null) guardado = true
                error
            }
        )
    }

    if (guardado) {
        DialogoAviso(null, "¡Movimiento guardado!", { guardado = false })
    }
}

// Botón "+": registrar un gasto o ingreso
@Composable
private fun DialogoNuevoMovimiento(
    onCerrar: () -> Unit,
    onGuardar: (esIngreso: Boolean, monto: String, categoria: String) -> String?
) {
    var esIngreso by remember { mutableStateOf(false) }
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
        CampoWallet(monto, { monto = it; error = null }, "Monto", decimal = true)
        if (!esIngreso) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Catalogo.categoriasGasto.forEach { c -> Chip(c, categoria == c) { categoria = c; error = null } }
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
                error = onGuardar(esIngreso, monto, categoria)
                if (error == null) onCerrar()
            }, Modifier.weight(1f))
        }
    }
}
