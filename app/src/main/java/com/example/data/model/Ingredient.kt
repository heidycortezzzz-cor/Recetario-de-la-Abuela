package com.example.data.model

/**
 * Modelo para representar un ingrediente de una receta.
 *
 * NOTA DE DISEÑO (Punto propenso a error):
 * Guardar [amount] como Double permite escalar proporcionalmente las porciones sin
 * perder precisión decimal ni truncar cantidades pequeñas (como 0.5 cucharaditas).
 * Los ingredientes "al gusto" (como sal o pimienta) se representan con [amount] = 0.0,
 * para que el escalador no intente recalcular cantidades fijas innecesariamente.
 */
data class Ingredient(
    val name: String,
    val amount: Double = 0.0,
    val unit: String = ""
)
