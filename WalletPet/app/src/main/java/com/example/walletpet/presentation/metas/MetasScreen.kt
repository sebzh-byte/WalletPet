package com.example.walletpet.presentation.metas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.walletpet.R
import com.example.walletpet.core.common.UiState
import com.example.walletpet.core.ui.components.*
import com.example.walletpet.core.ui.theme.*
import com.example.walletpet.domain.model.Meta
import com.example.walletpet.domain.model.TipoCambio
import com.example.walletpet.domain.model.pesos
import com.example.walletpet.domain.model.pesosConCentavos

@Composable
fun MetasScreen(viewModel: MetasViewModel = viewModel(factory = MetasViewModel.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dialogoNueva by remember { mutableStateOf(false) }
    var metaParaAhorro by remember { mutableStateOf<Meta?>(null) }
    var metaParaBorrar by remember { mutableStateOf<Meta?>(null) }
    var irAlFinal by remember { mutableStateOf(false) }
    val pager = rememberPagerState(pageCount = { state.metas.size })

    // Cuando se crea una meta, el carrusel se mueve a la nueva
    LaunchedEffect(state.metas.size) {
        if (irAlFinal && state.metas.isNotEmpty()) {
            pager.animateScrollToPage(state.metas.lastIndex)
            irAlFinal = false
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Durazno, Crema)))) {
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .espacioBarraInferior()
        ) {
            // Imagen del gatito en la rama, con la barra de nivel encima y el botón "+"
            Box(Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(R.drawable.fondo_metas),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 24.dp, start = 12.dp, end = 12.dp)
                        .height(190.dp),
                    contentScale = ContentScale.Crop
                )
                BarraSuperior(state.barra)
                Box(
                    Modifier.align(Alignment.BottomEnd)
                        .padding(end = 24.dp)
                        .offset(y = 18.dp)
                        .size(40.dp).clip(CircleShape).background(OlivaOscuro)
                        .clickable { dialogoNueva = true },
                    contentAlignment = Alignment.Center
                ) { Text("+", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(28.dp))

            // Datos de la API (tipo de cambio en vivo)
            TarjetaTipoCambio(
                total = state.totalAhorrado,
                estado = state.tipoCambio,
                moneda = state.monedaDestino,
                onMoneda = { viewModel.cargarTipoCambio(it) },
                onReintentar = { viewModel.cargarTipoCambio() }
            )

            Spacer(Modifier.height(14.dp))

            if (!state.cargando && state.metas.isEmpty()) {
                Tarjeta(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), radio = 8.dp) {
                    Text(
                        "Aún no tienes metas",
                        color = OlivaOscuro,
                        fontFamily = FuenteTitulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text("Toca \"Agregar nueva meta\" para crear la primera.", color = OlivaOscuro, fontSize = 13.sp)
                }
            } else {
                // Carrusel de metas: ya no se desborda aunque tengas muchas
                HorizontalPager(
                    state = pager,
                    contentPadding = PaddingValues(horizontal = 28.dp),
                    pageSpacing = 12.dp
                ) { pagina ->
                    val meta = state.metas.getOrNull(pagina) ?: return@HorizontalPager
                    TarjetaMetaGrande(
                        meta = meta,
                        tasa = state.tasa,
                        moneda = state.monedaDestino,
                        onAhorrar = { metaParaAhorro = meta },
                        onEliminar = { metaParaBorrar = meta }
                    )
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IndicadorPaginas(state.metas.size, pager.currentPage)
                    if (state.metas.size > 1) {
                        Spacer(Modifier.width(10.dp))
                        Text("${pager.currentPage + 1} de ${state.metas.size}", color = OlivaOscuro, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            BotonOliva(
                "Agregar nueva meta",
                { dialogoNueva = true },
                Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(52.dp)
            )
        }
    }

    // Hoja "Nueva meta"
    if (dialogoNueva) {
        var nombre by remember { mutableStateOf("") }
        var objetivo by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        HojaInferior({ dialogoNueva = false }) {
            Text(
                "Nueva meta",
                color = OlivaOscuro,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(18.dp))
            CampoWallet(nombre, { nombre = it; error = null }, "Nombre de la meta (ej. Audífonos)")
            Spacer(Modifier.height(10.dp))
            CampoWallet(objetivo, { objetivo = it; error = null }, "Precio / monto objetivo", decimal = true, error = error)
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonClaro("Cancelar", { dialogoNueva = false }, Modifier.weight(1f).height(50.dp))
                BotonOliva("Crear meta", {
                    error = viewModel.crearMeta(nombre, objetivo)
                    if (error == null) {
                        dialogoNueva = false
                        irAlFinal = true
                    }
                }, Modifier.weight(1f).height(50.dp))
            }
        }
    }

    metaParaAhorro?.let { meta ->
        DialogoRegistrarAhorro(
            nombreMeta = meta.nombre,
            onCerrar = { metaParaAhorro = null },
            onGuardar = { texto -> viewModel.registrarAhorro(meta, texto) }
        )
    }

    metaParaBorrar?.let { meta ->
        DialogoWallet({ metaParaBorrar = null }) {
            TituloDialogo("¿Eliminar \"${meta.nombre}\"?")
            Spacer(Modifier.height(10.dp))
            TextoDialogo("También se borrarán los ahorros registrados en esta meta. Esta acción no se puede deshacer.")
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonClaro("Cancelar", { metaParaBorrar = null }, Modifier.weight(1f))
                BotonOliva("Eliminar", {
                    viewModel.eliminarMeta(meta)
                    metaParaBorrar = null
                }, Modifier.weight(1f))
            }
        }
    }

    when (val m = state.mensaje) {
        null -> Unit
        MensajeMetas.MetaCreada -> DialogoAviso(
            "¡Nueva meta creada!",
            "Tu meta se ha registrado con éxito. ¡A partir de hoy tu gatito te acompañará a lograrla paso a paso!",
            viewModel::cerrarMensaje,
            "¡Comenzar a ahorrar!"
        )
        is MensajeMetas.AhorroGuardado -> DialogoAviso(
            "¡Ahorro registrado!",
            "Ganaste ${m.monedas} monedas." + if (m.subioNivel) " ¡Subiste a nivel ${m.nivel}!" else "",
            viewModel::cerrarMensaje,
            "¡Genial!"
        )
        is MensajeMetas.MetaEliminada -> DialogoAviso(
            null, "Se eliminó la meta \"${m.nombre}\".", viewModel::cerrarMensaje
        )
    }
}

@Composable
private fun TarjetaMetaGrande(
    meta: Meta,
    tasa: Double?,
    moneda: String,
    onAhorrar: () -> Unit,
    onEliminar: () -> Unit
) {
    Tarjeta(Modifier.fillMaxWidth(), colorBorde = OlivaBorde, grosor = 2.dp, radio = 14.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                meta.nombre,
                modifier = Modifier.weight(1f),
                color = OlivaOscuro,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("${(meta.progreso * 100).toInt()}%", color = OlivaOscuro, fontSize = 14.sp)
        }
        Spacer(Modifier.height(10.dp))
        BarraProgreso(meta.progreso, if (meta.completada) AnilloVerde else BarraMeta, alto = 14.dp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Ahorrado: ${meta.ahorrado.pesos()}", color = OlivaOscuro, fontSize = 12.sp)
            Text("Meta: ${meta.objetivo.pesos()}", color = OlivaOscuro, fontSize = 12.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            if (meta.completada) "¡Meta cumplida! 🎉" else "Te faltan ${meta.faltante.pesos()}",
            color = Naranja,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        if (tasa != null) {
            Text(
                "Precio en $moneda: ${(meta.objetivo * tasa).pesosConCentavos()}",
                color = TextoGris,
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonOliva("Registrar ahorro", onAhorrar, Modifier.weight(1f).height(44.dp), habilitado = !meta.completada)
            BotonClaro("Eliminar", onEliminar, Modifier.weight(0.7f).height(44.dp))
        }
    }
}

/** Tarjeta que muestra los datos de la API: carga, éxito, sin datos y error con "Reintentar". */
@Composable
private fun TarjetaTipoCambio(
    total: Double,
    estado: UiState<TipoCambio>,
    moneda: String,
    onMoneda: (String) -> Unit,
    onReintentar: () -> Unit
) {
    Tarjeta(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), radio = 10.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tu ahorro total", color = OlivaOscuro.copy(alpha = 0.8f), fontSize = 12.sp)
                Text(
                    "${total.pesosConCentavos()} MXN",
                    color = OlivaOscuro,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Chip("USD", moneda == "USD") { onMoneda("USD") }
            Spacer(Modifier.width(6.dp))
            Chip("EUR", moneda == "EUR") { onMoneda("EUR") }
        }
        Spacer(Modifier.height(8.dp))
        when (estado) {
            UiState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), color = Oliva, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Consultando tipo de cambio…", color = TextoGris, fontSize = 12.sp)
            }
            is UiState.Success -> {
                Text(
                    "≈ ${(total * estado.data.tasa).pesosConCentavos()} ${estado.data.destino}",
                    color = Naranja,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "1 ${estado.data.base} = ${"%.4f".format(estado.data.tasa)} ${estado.data.destino} (tipo de cambio en vivo)",
                    color = TextoGris,
                    fontSize = 11.sp
                )
            }
            UiState.Empty -> FilaReintentar("No hay datos de tipo de cambio por ahora.", onReintentar)
            is UiState.Error -> FilaReintentar(estado.mensaje, onReintentar)
        }
    }
}

@Composable
private fun FilaReintentar(texto: String, onReintentar: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(texto, color = TextoGris, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        BotonClaro("Reintentar", onReintentar)
    }
}
