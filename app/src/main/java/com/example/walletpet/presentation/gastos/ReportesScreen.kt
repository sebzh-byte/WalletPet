package com.example.walletpet.presentation.gastos

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.walletpet.core.ui.components.*
import com.example.walletpet.core.ui.theme.*
import com.example.walletpet.domain.model.ReporteMensual
import com.example.walletpet.domain.model.pesosConCentavos

// Lista de reportes (uno por mes, armados con los datos de Room)
@Composable
fun ReportesScreen(
    onAbrir: (String) -> Unit,
    onVolver: () -> Unit,
    viewModel: GastosViewModel = viewModel(factory = GastosViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(Crema).espacioBarraInferior()) {
        BarraSuperior(state.barra)
        Column(
            Modifier.weight(1f).padding(16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)).background(Color.White)
                .border(1.dp, BordeTan, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            EncabezadoReporte("Reportes", onVolver)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(BordeTan))
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (!state.cargando && state.reportes.isEmpty()) {
                    Text(
                        "Todavía no hay reportes. Registra ahorros o gastos y aquí aparecerá el resumen de cada mes.",
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = TextoGris,
                        fontSize = 13.sp
                    )
                }
                state.reportes.forEach { r ->
                    Column(Modifier.fillMaxWidth().clickable { onAbrir(r.clave) }.padding(vertical = 10.dp)) {
                        Text(r.titulo, color = Naranja, fontSize = 14.sp)
                        Text(
                            "Ahorraste ${r.totalAhorrado.pesosConCentavos()} en ${r.aportaciones} aportaciones. Toca para ver el detalle.",
                            color = TextoGris,
                            fontSize = 13.sp
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(BordeTan))
                }
            }
        }
    }
}

// Detalle de un reporte + descarga del PDF
@Composable
fun ReporteDetalleScreen(
    clave: String,
    onVolver: () -> Unit,
    viewModel: GastosViewModel = viewModel(factory = GastosViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirmar by remember { mutableStateOf(false) }
    var pdfListo by remember { mutableStateOf<Uri?>(null) }
    val reporte = state.reportes.find { it.clave == clave }

    // Eventos de una sola vez que manda el ViewModel
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { e ->
            when (e) {
                is EventoGastos.PdfListo -> pdfListo = e.uri
                is EventoGastos.Mensaje -> Toast.makeText(context, e.texto, Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(Modifier.fillMaxSize().background(Crema).espacioBarraInferior()) {
        BarraSuperior(state.barra)
        Column(
            Modifier.weight(1f).padding(16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)).background(Color.White)
                .border(1.dp, BordeTan, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            EncabezadoReporte(reporte?.titulo ?: "Reporte", onVolver)
            Spacer(Modifier.height(16.dp))
            Box(Modifier.weight(1f).fillMaxWidth()) {
                Text(
                    when {
                        reporte != null -> textoReporte(reporte)
                        state.cargando -> "Cargando…"
                        else -> "Este reporte ya no tiene datos."
                    },
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    color = TextoGris,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            if (reporte != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    BotonCafe("Descargar PDF", { confirmar = true })
                }
            }
        }
    }

    // ¿Deseas descargar?
    if (confirmar && reporte != null) {
        DialogoWallet({ confirmar = false }) {
            TituloDialogo("¿Deseas descargar el reporte de ${reporte.titulo}?", centrado = false, tamano = 20)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonCafeClaro("Descargar", {
                    confirmar = false
                    viewModel.exportarPdf(reporte.clave)
                }, Modifier.weight(1f))
                BotonCafe("Cancelar", { confirmar = false }, Modifier.weight(1f))
            }
        }
    }

    // Reporte guardado
    pdfListo?.let { uri ->
        DialogoWallet({ pdfListo = null }) {
            TituloDialogo("¡Reporte guardado con éxito! 🐾", centrado = false, tamano = 20)
            Spacer(Modifier.height(8.dp))
            Text(
                "Tu gatito guardó una copia de tu avance en tu dispositivo. ¡Sigue así!",
                color = TextoGris,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonClaro("Compartir", { compartirPdf(context, uri) }, Modifier.weight(1f))
                BotonOliva("Abrir PDF", {
                    pdfListo = null
                    abrirPdf(context, uri)
                }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EncabezadoReporte(titulo: String, onVolver: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            titulo,
            modifier = Modifier.weight(1f),
            color = Naranja,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            "‹ Volver",
            modifier = Modifier.clickable { onVolver() }.padding(4.dp),
            color = OlivaOscuro,
            fontSize = 13.sp
        )
    }
}

private fun textoReporte(r: ReporteMensual): String = buildString {
    append("Durante ${r.titulo} ahorraste un total de ${r.totalAhorrado.pesosConCentavos()} MXN ")
    append("en ${r.aportaciones} aportaciones.\n\n")
    if (r.porMeta.isNotEmpty()) {
        append("Ahorro por meta:\n")
        r.porMeta.forEach { (meta, monto) -> append("• $meta: ${monto.pesosConCentavos()}\n") }
        append("\n")
    }
    append("Gastos registrados: ${r.gastos.pesosConCentavos()} MXN\n")
    append("Ingresos registrados: ${r.ingresos.pesosConCentavos()} MXN\n\n")
    if (r.aportaciones > 0) {
        append("Promedio por aportación: ${(r.totalAhorrado / r.aportaciones).pesosConCentavos()} MXN. ")
        append("¡Tu gatito está orgulloso de ti!")
    } else {
        append("Este mes no registraste ahorros. ¡Tu gatito te espera para empezar!")
    }
}

private fun abrirPdf(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, "application/pdf")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No tienes una app para ver PDF. Usa \"Compartir\" para enviarlo.", Toast.LENGTH_LONG).show()
    }
}

private fun compartirPdf(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND)
        .setType("application/pdf")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Compartir reporte").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
