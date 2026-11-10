package com.example.walletpet.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.walletpet.R
import com.example.walletpet.domain.model.PetMood

/**
 * Imagen del gato según su humor.
 *
 * CUANDO TENGAN LOS GIFs: pónganlos en res/drawable (ej. gato_feliz.gif, gato_hambre.gif,
 * gato_triste.gif) y cambien SOLO este "when". Coil ya está configurado para animarlos
 * (ver WalletPetApplication).
 */
@DrawableRes
fun PetMood.imagen(): Int = when (this) {
    PetMood.FELIZ -> R.drawable.gato_normal    // -> R.drawable.gato_feliz
    PetMood.NORMAL -> R.drawable.gato_normal
    PetMood.HAMBRE -> R.drawable.gato_normal   // -> R.drawable.gato_hambre
    PetMood.TRISTE -> R.drawable.gato_normal   // -> R.drawable.gato_triste
}

fun PetMood.descripcion(): String = when (this) {
    PetMood.FELIZ -> "Gatito feliz"
    PetMood.NORMAL -> "Gatito tranquilo"
    PetMood.HAMBRE -> "Gatito con hambre"
    PetMood.TRISTE -> "Gatito triste"
}

@Composable
fun PetView(mood: PetMood, modifier: Modifier = Modifier) {
    AsyncImage(
        model = mood.imagen(),
        contentDescription = mood.descripcion(),
        placeholder = painterResource(R.drawable.gato_normal),
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}
