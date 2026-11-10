package com.example.walletpet.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.walletpet.R
import com.example.walletpet.core.ui.theme.*

/** Datos que muestra la barra de arriba (nivel, experiencia y monedas). */
data class EstadoBarra(val nivel: Int = 1, val xp: Float = 0f, val monedas: Int = 0)

/** Deja espacio abajo para que la barra de navegación no tape el contenido. */
fun Modifier.espacioBarraInferior(): Modifier =
    this.navigationBarsPadding().padding(bottom = 86.dp)

/** Barra de progreso simple con las esquinas redondeadas. */
@Composable
fun BarraProgreso(
    progreso: Float,
    color: Color,
    modifier: Modifier = Modifier,
    pista: Color = GrisPista,
    alto: Dp = 12.dp
) {
    Box(
        modifier.fillMaxWidth().height(alto)
            .clip(RoundedCornerShape(alto / 2)).background(pista)
    ) {
        Box(
            Modifier.fillMaxWidth(progreso.coerceIn(0f, 1f)).fillMaxHeight().background(color)
        )
    }
}

/** Barra de arriba: nivel + monedas. */
@Composable
fun BarraSuperior(estado: EstadoBarra, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.weight(1f).height(42.dp)
                .clip(RoundedCornerShape(8.dp)).background(CremaPill)
                .border(1.dp, BordeTan, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nvl ${estado.nivel}", color = Naranja, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            BarraProgreso(estado.xp, Moneda, Modifier.weight(1f), alto = 6.dp)
        }
        Row(
            Modifier.width(125.dp).height(42.dp)
                .clip(RoundedCornerShape(8.dp)).background(CremaPill)
                .border(1.dp, BordeTan, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painterResource(R.drawable.moneda), contentDescription = "Monedas", Modifier.size(30.dp))
            Spacer(Modifier.weight(1f))
            Text("${estado.monedas}", color = Naranja, fontSize = 20.sp)
        }
    }
}

/** Barra de abajo. La pestaña elegida deja un "hueco" donde se ve el fondo, como en el diseño. */
@Composable
fun BarraInferior(seleccion: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val iconos = listOf(
        R.drawable.ic_home to "Inicio",
        R.drawable.ic_pata to "Tienda",
        R.drawable.ic_libreta to "Metas",
        R.drawable.ic_cartera to "Gastos"
    )
    Box(
        modifier.fillMaxWidth().navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp).height(70.dp)
    ) {
        Box(
            Modifier.matchParentSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawBehind {
                    drawRoundRect(color = BarraNav, cornerRadius = CornerRadius(16.dp.toPx()))
                    drawRoundRect(
                        color = BordeTan,
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    val ancho = size.width / 4f
                    val anchoHueco = 84.dp.toPx()
                    val x = ancho * seleccion + (ancho - anchoHueco) / 2f
                    val radio = CornerRadius(42.dp.toPx())
                    val hueco = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = x,
                                top = 0f,
                                right = x + anchoHueco,
                                bottom = size.height - 3.dp.toPx(),
                                topLeftCornerRadius = CornerRadius.Zero,
                                topRightCornerRadius = CornerRadius.Zero,
                                bottomRightCornerRadius = radio,
                                bottomLeftCornerRadius = radio
                            )
                        )
                    }
                    drawPath(hueco, color = Color.Black, blendMode = BlendMode.Clear)
                }
        )
        Row(Modifier.matchParentSize()) {
            iconos.forEachIndexed { i, (icono, nombre) ->
                Box(
                    Modifier.weight(1f).fillMaxHeight().clickable { onSelect(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(painterResource(icono), contentDescription = nombre, Modifier.size(40.dp))
                }
            }
        }
    }
}

/** Puntitos debajo de un carrusel para saber en qué página vas. */
@Composable
fun IndicadorPaginas(total: Int, actual: Int, modifier: Modifier = Modifier) {
    if (total <= 1) return
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(
                Modifier.size(if (i == actual) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (i == actual) OlivaOscuro else OlivaOscuro.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
fun BotonOliva(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = habilitado,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Oliva, contentColor = Color.White)
    ) { Text(texto, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center) }
}

@Composable
fun BotonClaro(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        border = BorderStroke(1.5.dp, Oliva),
        colors = ButtonDefaults.buttonColors(containerColor = OlivaClaro, contentColor = OlivaOscuro)
    ) { Text(texto, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center) }
}

/** Botón café (reportes). */
@Composable
fun BotonCafe(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        border = BorderStroke(1.5.dp, Marron),
        colors = ButtonDefaults.buttonColors(containerColor = BotonCafe, contentColor = Color.White)
    ) { Text(texto, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
}

@Composable
fun BotonCafeClaro(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        border = BorderStroke(1.5.dp, Marron),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5E9DC), contentColor = Marron)
    ) { Text(texto, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
}

@Composable
fun Chip(texto: String, activo: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(10.dp))
            .background(if (activo) Oliva else GrisPista)
            .border(1.dp, if (activo) OlivaBorde else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, color = if (activo) Color.White else GrisIcono, fontSize = 13.sp)
    }
}

/** Tarjeta blanca con borde, la que se repite por toda la app. */
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    colorBorde: Color = BordeTan,
    grosor: Dp = 1.dp,
    radio: Dp = 10.dp,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(radio),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(grosor, colorBorde)
    ) {
        Column(Modifier.padding(14.dp), content = contenido)
    }
}

/** Ventanita emergente. */
@Composable
fun DialogoWallet(onCerrar: () -> Unit, contenido: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onCerrar) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BordeTan)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = contenido
            )
        }
    }
}

/** Hoja que sube desde abajo (la de "Nueva meta"). */
@Composable
fun HojaInferior(onCerrar: () -> Unit, contenido: @Composable ColumnScope.() -> Unit) {
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.BottomCenter) {
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(24.dp),
                content = contenido
            )
        }
    }
}

@Composable
fun TituloDialogo(texto: String, centrado: Boolean = true, tamano: Int = 18) {
    Text(
        texto,
        modifier = Modifier.fillMaxWidth(),
        color = Naranja,
        fontFamily = FuenteTitulo,
        fontWeight = FontWeight.Bold,
        fontSize = tamano.sp,
        textAlign = if (centrado) TextAlign.Center else TextAlign.Start
    )
}

@Composable
fun TextoDialogo(texto: String, centrado: Boolean = true) {
    Text(
        texto,
        modifier = Modifier.fillMaxWidth(),
        color = OlivaOscuro,
        fontFamily = FuenteTitulo,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        textAlign = if (centrado) TextAlign.Center else TextAlign.Start
    )
}

/** Mensaje corto con un solo botón. */
@Composable
fun DialogoAviso(titulo: String?, texto: String, onCerrar: () -> Unit, boton: String = "Entendido") {
    DialogoWallet(onCerrar) {
        if (titulo != null) {
            TituloDialogo(titulo)
            Spacer(Modifier.height(12.dp))
        }
        TextoDialogo(texto, centrado = titulo == null)
        Spacer(Modifier.height(14.dp))
        BotonClaro(boton, onCerrar)
    }
}

/** Campo de texto con el estilo del diseño (fondo verde clarito, sin línea). */
@Composable
fun CampoWallet(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    decimal: Boolean = false,
    error: String? = null
) {
    TextField(
        value = valor,
        onValueChange = onCambio,
        placeholder = { Text(etiqueta, color = OlivaOscuro.copy(alpha = 0.5f)) },
        singleLine = true,
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Text
        ),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = FondoCampo,
            unfocusedContainerColor = FondoCampo,
            errorContainerColor = FondoCampo,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedTextColor = OlivaOscuro,
            unfocusedTextColor = OlivaOscuro,
            errorTextColor = OlivaOscuro
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Diálogo para registrar ahorro en una meta (se usa en Inicio y en Metas). */
@Composable
fun DialogoRegistrarAhorro(
    nombreMeta: String,
    onCerrar: () -> Unit,
    onGuardar: (String) -> String?     // regresa el error, o null si se guardó bien
) {
    var monto by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    DialogoWallet(onCerrar) {
        TituloDialogo("Registrar ahorro")
        Spacer(Modifier.height(4.dp))
        TextoDialogo(nombreMeta)
        Spacer(Modifier.height(14.dp))
        CampoWallet(
            valor = monto,
            onCambio = { monto = it; error = null },
            etiqueta = "Monto",
            decimal = true,
            error = error
        )
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonClaro("Cancelar", onCerrar, Modifier.weight(1f))
            BotonOliva("Guardar", {
                error = onGuardar(monto)
                if (error == null) onCerrar()
            }, Modifier.weight(1f))
        }
    }
}
