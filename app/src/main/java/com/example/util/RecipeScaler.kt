package com.example.util

import com.example.data.model.Ingredient
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Utilidad matemática para el cálculo y formateo de conversión de porciones en recetas.
 *
 * PUNTOS CRÍTICOS DONDE ALGUIEN SUELE EQUIVOCARSE:
 * 1. DIVISIÓN POR CERO: Si una receta tiene `baseServings = 0` o el usuario pide `0` porciones,
 *    una división ingenua `targetServings / baseServings` producirá `Double.POSITIVE_INFINITY` o
 *    `Double.NaN`. Usamos [coerceAtLeast(1)] para asegurar siempre denominadores válidos.
 * 2. NÚMEROS CON DECIMALES PERIÓDICOS O COMA FLOTANTE IMPRECISA: En punto flotante IEEE 754,
 *    `4 * 2.5` o `1 / 3` produce valores como `3.3333333333333335` o `0.30000000000000004`.
 *    Es fundamental redondear a un número razonable de decimales (1 o 2) y formatear números enteros
 *    sin el sufijo innecesario ".0" (ej: mostrar "10" en lugar de "10.0").
 * 3. INGREDIENTES SIN CANTIDAD NUMÉRICA ("AL GUSTO"): Sal, orégano o pimienta no deben
 *    multiplicarse absurdamente si su valor base es 0 o indefinido.
 */
object RecipeScaler {

    private val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))

    /**
     * Parsea un texto ingresado por el usuario de forma segura y tolerante a fallos.
     * Soporta enteros ("500"), decimales con coma o punto ("2,5", "2.5"),
     * fracciones simples ("1/2", "3/4") y números mixtos ("1 1/2").
     * Si el texto está vacío o es inválido, retorna 0.0 sin lanzar excepciones.
     */
    fun parseAmount(text: String): Double {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0.0

        // Caso número mixto: ej. "1 1/2"
        if (trimmed.contains(' ') && trimmed.contains('/')) {
            val spaceParts = trimmed.split("\\s+".toRegex())
            if (spaceParts.size == 2) {
                val whole = spaceParts[0].replace(',', '.').toDoubleOrNull() ?: 0.0
                val frac = parseAmount(spaceParts[1])
                return whole + frac
            }
        }

        // Caso fracción simple: ej. "1/2" o "3/4"
        if (trimmed.contains('/')) {
            val fractionParts = trimmed.split('/')
            if (fractionParts.size == 2) {
                val numerator = fractionParts[0].trim().replace(',', '.').toDoubleOrNull()
                val denominator = fractionParts[1].trim().replace(',', '.').toDoubleOrNull()
                if (numerator != null && denominator != null && denominator != 0.0) {
                    return numerator / denominator
                }
            }
        }

        // Caso decimal o entero estándar
        val normalized = trimmed.replace(',', '.')
        return normalized.toDoubleOrNull() ?: 0.0
    }

    /**
     * Calcula la nueva cantidad de un ingrediente en base a la relación de porciones.
     *
     * @param baseAmount Cantidad original en la receta base.
     * @param baseServings Número original de porciones (ej. 4).
     * @param targetServings Número deseado de porciones (ej. 10).
     * @return Cantidad escalada de forma segura.
     */
    fun scaleAmount(
        baseAmount: Double,
        baseServings: Int,
        targetServings: Int
    ): Double {
        // Protección contra división por cero y valores negativos o absurdos
        val safeBase = baseServings.coerceAtLeast(1)
        val safeTarget = targetServings.coerceAtLeast(1)

        // Si la cantidad original es 0 (ej: "sal al gusto"), se preserva 0.0
        if (baseAmount <= 0.0) {
            return 0.0
        }

        val scaleFactor = safeTarget.toDouble() / safeBase.toDouble()
        return baseAmount * scaleFactor
    }

    /**
     * Escala todos los ingredientes de una receta para un nuevo número de porciones.
     */
    fun scaleIngredients(
        ingredients: List<Ingredient>,
        baseServings: Int,
        targetServings: Int
    ): List<Pair<Ingredient, Double>> {
        val safeBase = baseServings.coerceAtLeast(1)
        val safeTarget = targetServings.coerceAtLeast(1)

        return ingredients.map { ingredient ->
            val scaledAmount = scaleAmount(
                baseAmount = ingredient.amount,
                baseServings = safeBase,
                targetServings = safeTarget
            )
            Pair(ingredient, scaledAmount)
        }
    }

    /**
     * Formatea una cantidad calculada para que sea legible y natural para una persona cocinando.
     * Convierte números exactos a enteros (ej: 2.0 -> "2", 10.0 -> "10"),
     * aproxima fracciones tradicionales si es muy cercano (ej: 0.5 -> "½", 1.5 -> "1 ½")
     * y redondea decimales arbitrarios sin desbordes.
     */
    fun formatAmount(amount: Double): String {
        if (amount <= 0.0) return ""

        // Verificar si es un número entero exacto o extremadamente cercano (ej: 4.000000001)
        val rounded = amount.roundToInt()
        if (abs(amount - rounded) < 0.001) {
            return rounded.toString()
        }

        val integerPart = amount.toInt()
        val fractionalPart = amount - integerPart

        // Detectar fracciones culinarias habituales
        val fractionString = when {
            abs(fractionalPart - 0.5) < 0.05 -> "½"
            abs(fractionalPart - 0.25) < 0.05 -> "¼"
            abs(fractionalPart - 0.75) < 0.05 -> "¾"
            abs(fractionalPart - 0.33) < 0.05 -> "⅓"
            abs(fractionalPart - 0.67) < 0.05 -> "⅔"
            else -> null
        }

        if (fractionString != null) {
            return if (integerPart > 0) "$integerPart $fractionString" else fractionString
        }

        // Formateo decimal limpio con máximo 2 cifras significativas
        val formatted = decimalFormat.format(amount)
        return formatted
    }

    /**
     * Presentación completa con unidad (ej: "250 g", "5 unidades", "1 ½ tazas", "Al gusto")
     */
    fun formatIngredientDisplay(amount: Double, unit: String): String {
        if (amount <= 0.0) {
            return if (unit.isNotBlank()) unit else "Al gusto"
        }
        val formattedNumber = formatAmount(amount)
        return if (unit.isNotBlank()) "$formattedNumber $unit" else formattedNumber
    }
}
