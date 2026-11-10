package com.example.walletpet.presentation.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.walletpet.core.ui.components.*
import com.example.walletpet.core.ui.theme.*
import com.example.walletpet.domain.model.Meta
import com.example.walletpet.domain.model.ReglasMascota
import com.example.walletpet.domain.model.pesos
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onIrATienda: () -> Unit,
    onIrAMetas: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var mostrarEstado by rememberSaveable { mutableStateOf(false) }
    var metaParaAhorro by remember { mutableStateOf<Meta?>(null) }
    val pager = rememberPagerState(pageCount = { state.metas.size })

    Box(Modifier.fillMaxSize().background(Durazno)) {
        // Fondo del cuarto (cambia cuando se abre el panel de comida)
        Image(
            painter = painterResource(
                if (mostrarEstado) R.drawable.fondo_alimentar else R.drawable.fondo_home
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(Modifier.fillMaxSize().espacioBarraInferior()) {
            BarraSuperior(state.barra)
            Spacer(Modifier.height(10.dp))

            when {
                mostrarEstado -> PanelEstado(
                    hambre = state.hambre,
                    animo = state.animo,
                    croquetas = state.croquetas,
                    onAlimentar = viewModel::tocarAlimentar,
                    onTienda = onIrATienda
                )
                state.cargando -> Spacer(Modifier.height(140.dp))
                state.metas.isEmpty() -> TarjetaSinMetas(onIrAMetas)
                else -> {
                    // Carrusel: desliza para ver cada meta
                    HorizontalPager(
                        state = pager,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        pageSpacing = 10.dp
                    ) { pagina ->
                        state.metas.getOrNull(pagina)?.let { TarjetaMeta(it) }
                    }
                    IndicadorPaginas(
                        total = state.metas.size,
                        actual = pager.currentPage,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    BotonOliva(
                        "Registrar Ahorro",
                        { metaParaAhorro = state.metas.getOrNull(pager.currentPage) },
                        Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.55f).height(46.dp)
                    )
                }
            }

            // El gatito ocupa todo el espacio que sobra (por eso se ve más grande).
            // Tócalo para abrir o cerrar el panel de comida.
            Box(Modifier.weight(1f).fillMaxWidth()) {
                PetView(
                    mood = state.mood,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxHeight(0.84f)
                        .fillMaxWidth(0.9f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { mostrarEstado = !mostrarEstado }
                )
                Globo(
                    texto = state.mood.frase,
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 4.dp)
                )
            }
        }
    }

    // Registrar ahorro en la meta que se está viendo en el carrusel
    metaParaAhorro?.let { meta ->
        DialogoRegistrarAhorro(
            nombreMeta = meta.nombre,
            onCerrar = { metaParaAhorro = null },
            onGuardar = { texto -> viewModel.registrarAhorro(meta, texto) }
        )
    }

    DialogosHome(state.dialogo, viewModel, onIrATienda)
}

@Composable
private fun DialogosHome(dialogo: DialogoHome?, viewModel: HomeViewModel, onIrATienda: () -> Unit) {
    val cerrar = viewModel::cerrarDialogo
    when (dialogo) {
        null -> Unit
        is DialogoHome.Comer -> DialogoWallet(cerrar) {
            TituloDialogo("¡Hora de comer!")
            Spacer(Modifier.height(14.dp))
            TextoDialogo(
                "Tu gatito tiene un poco de hambre. ¿Quieres darle una porción de croquetas? " +
                    "Te quedan ${dialogo.croquetas}.",
                centrado = false
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonClaro("¡Sí, darle de comer!", viewModel::confirmarAlimentar, Modifier.weight(1f))
                BotonOliva("Tal vez después", viewModel::posponerComida, Modifier.weight(1f))
            }
        }
        DialogoHome.Lleno -> DialogoAviso(
            "¡Gatito con la pancita llena!",
            "Tu mascota ya comió lo suficiente por ahora y no tiene hambre. ¡Vuelve más tarde para consentirlo otra vez!",
            cerrar,
            "¡Entendido!"
        )
        DialogoHome.SinCroquetas -> DialogoWallet(cerrar) {
            TituloDialogo("¡Se acabaron las croquetas!")
            Spacer(Modifier.height(10.dp))
            Image(painterResource(R.drawable.item_croquetas), contentDescription = null, Modifier.size(64.dp))
            Spacer(Modifier.height(10.dp))
            TextoDialogo(
                "Compra una bolsa en la tienda. Cada bolsa trae ${ReglasMascota.PORCIONES_POR_BOLSA} porciones " +
                    "y darle de comer ya no te cuesta monedas.",
                centrado = false
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonClaro("Después", cerrar, Modifier.weight(1f))
                BotonOliva("Ir a la tienda", { cerrar(); onIrATienda() }, Modifier.weight(1f))
            }
        }
        is DialogoHome.Alimentado -> DialogoAviso(
            "¡Ñam, ñam! 🐾",
            if (dialogo.porcionesRestantes > 0) "Tu gatito comió feliz. Te quedan ${dialogo.porcionesRestantes} porciones de croquetas."
            else "Tu gatito comió feliz. Esa era la última porción: compra más croquetas en la tienda.",
            cerrar
        )
        is DialogoHome.AhorroGuardado -> {
            val texto = buildString {
                append("Ganaste ${dialogo.monedas} monedas.")
                if (dialogo.subioNivel) append(" ¡Subiste a nivel ${dialogo.nivel}!")
                if (dialogo.metaCumplida != null) append(" ¡Cumpliste tu meta \"${dialogo.metaCumplida}\"! 🎉")
            }
            DialogoAviso("¡Ahorro registrado!", texto, cerrar, "¡Genial!")
        }
        is DialogoHome.Mensaje -> {
            // Mensajes cortos que se cierran solos
            LaunchedEffect(dialogo) {
                delay(2500)
                cerrar()
            }
            DialogoWallet(cerrar) { TextoDialogo(dialogo.texto, centrado = false) }
        }
    }
}

@Composable
private fun Globo(texto: String, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(2.dp, Color.Black, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            texto,
            color = Color(0xFF5F6B2A),
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// Tarjeta verde de "Meta activa"
@Composable
private fun TarjetaMeta(meta: Meta) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)).background(VerdeCard)
            .border(1.dp, OlivaBorde, RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { meta.progreso },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                color = AnilloVerde,
                trackColor = Color(0xFFB8B8B8)
            )
            Box(
                Modifier.size(78.dp).clip(CircleShape).background(Color(0xFFF7FBE0)),
                contentAlignment = Alignment.Center
            ) {
                Text("${(meta.progreso * 100).toInt()}%", fontSize = 24.sp, color = OlivaOscuro)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (meta.completada) "¡Meta cumplida!" else "Meta activa",
                color = OlivaOscuro.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                meta.nombre,
                color = OlivaOscuro,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (meta.completada) "Ahorraste ${meta.ahorrado.pesos()}" else "Faltan ${meta.faltante.pesos()}",
                color = OlivaOscuro,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFC3CF6E))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text("${meta.ahorrado.pesos()} / ${meta.objetivo.pesos()}", color = Color.White, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun TarjetaSinMetas(onIrAMetas: () -> Unit) {
    Column(
        Modifier.padding(horizontal = 16.dp).fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)).background(VerdeCard)
            .border(1.dp, OlivaBorde, RoundedCornerShape(10.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Aún no tienes metas",
            color = OlivaOscuro,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Crea tu primera meta para empezar a ahorrar con tu gatito.",
            color = OlivaOscuro,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(12.dp))
        BotonOliva("Crear meta", onIrAMetas, Modifier.fillMaxWidth(0.6f).height(44.dp))
    }
}

// Panel de Hambre, Ánimo y croquetas
@Composable
private fun PanelEstado(
    hambre: Float,
    animo: Float,
    croquetas: Int,
    onAlimentar: () -> Unit,
    onTienda: () -> Unit
) {
    Column(
        Modifier.padding(horizontal = 14.dp).fillMaxWidth()
            .background(Color.White.copy(alpha = 0.85f))
            .border(1.dp, BordeTan)
            .padding(14.dp)
    ) {
        FilaEstado("Hambre", hambre, RosaHambre)
        Spacer(Modifier.height(12.dp))
        FilaEstado("Ánimo", animo, VerdeAnimo)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.item_croquetas), contentDescription = null, Modifier.size(30.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (croquetas == 1) "1 porción de croquetas" else "$croquetas porciones de croquetas",
                color = OlivaOscuro,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonOliva("Alimentar", onAlimentar, Modifier.width(170.dp).height(46.dp))
            Spacer(Modifier.width(24.dp))
            Box(
                Modifier.size(54.dp).clip(CircleShape).background(Oliva).clickable { onTienda() },
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(R.drawable.ic_pata), contentDescription = "Ir a la tienda", Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun FilaEstado(nombre: String, valor: Float, colorBarra: Color) {
    Row(
        Modifier.fillMaxWidth().background(VerdeFila).border(1.dp, OlivaBorde)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            nombre,
            modifier = Modifier.width(64.dp),
            color = OlivaOscuro,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        BarraProgreso(valor, colorBarra, Modifier.weight(1f), pista = Color(0xFFF7F6D8), alto = 8.dp)
    }
}
