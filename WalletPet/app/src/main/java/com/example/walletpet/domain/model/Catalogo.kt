package com.example.walletpet.domain.model

/** Productos de la tienda. */
object Catalogo {
    const val CROQUETAS = "Croquetas"

    val productos = listOf(
        Producto(CROQUETAS, "🥫", 100, 1, consumible = true),
        Producto("Arenero", "🧺", 150, 1),
        Producto("Cama", "🛏️", 175, 1),
        Producto("Planta", "🪴", 200, 1),
        Producto("Cojín gatito", "🎁", 300, 3),
        Producto("Tapete osito", "🎁", 320, 3),
        Producto("Cojín nube", "🎁", 400, 4),
        Producto("Cojín sandía", "🎁", 420, 4),
        Producto("Peluche gatito", "🎁", 450, 4)
    )

    val categoriasGasto = listOf(
        "Transporte", "Alimentación", "Escuela y Papelería", "Entretenimiento", "Celular / Internet", OTROS
    )
    const val OTROS = "Otros"   // si el usuario no elige categoría
}
