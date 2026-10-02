package com.example

import com.example.data.model.Ingredient
import com.example.ui.RecipeViewModel
import com.example.util.RecipeScaler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias de los Criterios de Aceptación:
 * 1. Conversión de porciones (ej. de 4 a 10 personas).
 * 2. Ausencia de fallos de división por cero o excepciones aritméticas.
 * 3. Búsqueda insensible a mayúsculas y acentos.
 */
class ExampleUnitTest {

    @Test
    fun testPortionScaling_from4to10() {
        // Criterio de aceptación explícito del usuario: de 4 a 10 personas
        val baseServings = 4
        val targetServings = 10

        // Caso 1: 500 gramos de carne -> debe dar 1250 gramos
        val scaledMeat = RecipeScaler.scaleAmount(500.0, baseServings, targetServings)
        assertEquals(1250.0, scaledMeat, 0.001)
        assertEquals("1250", RecipeScaler.formatAmount(scaledMeat))
        assertEquals("1250 g", RecipeScaler.formatIngredientDisplay(scaledMeat, "g"))

        // Caso 2: 2 huevos -> debe dar 5 huevos
        val scaledEggs = RecipeScaler.scaleAmount(2.0, baseServings, targetServings)
        assertEquals(5.0, scaledEggs, 0.001)
        assertEquals("5", RecipeScaler.formatAmount(scaledEggs))

        // Caso 3: ingrediente al gusto (amount = 0.0) -> se preserva 0.0 y no crashea
        val scaledSalt = RecipeScaler.scaleAmount(0.0, baseServings, targetServings)
        assertEquals(0.0, scaledSalt, 0.001)
        assertEquals("al gusto", RecipeScaler.formatIngredientDisplay(scaledSalt, "al gusto"))
    }

    @Test
    fun testPortionScaling_zeroAndEdgeCases() {
        // Validación de robustez: porciones en 0 no deben lanzar división por cero ni Infinity
        val scaledZeroBase = RecipeScaler.scaleAmount(100.0, baseServings = 0, targetServings = 4)
        assertTrue(scaledZeroBase.isFinite())
        assertEquals(400.0, scaledZeroBase, 0.001)

        val scaledZeroTarget = RecipeScaler.scaleAmount(100.0, baseServings = 4, targetServings = 0)
        assertTrue(scaledZeroTarget.isFinite())
        assertEquals(25.0, scaledZeroTarget, 0.001)
    }

    @Test
    fun testSearchIngredientNormalization() {
        // Normalización para búsqueda por ingrediente con y sin acentos
        val query1 = "huevo"
        val query2 = "limon"
        val ingredient1 = "Huevos frescos de campo"
        val ingredient2 = "Jugo de Limón exprimido"

        assertTrue(RecipeViewModel.normalizeText(ingredient1).contains(RecipeViewModel.normalizeText(query1)))
        assertTrue(RecipeViewModel.normalizeText(ingredient2).contains(RecipeViewModel.normalizeText(query2)))
    }

    @Test
    fun testDefensiveParsing_negativeNumbersAndInvalidText() {
        // QA Test 2: Texto donde debería ir un número
        assertEquals(0.0, RecipeScaler.parseAmount("mucho"), 0.001)
        assertEquals(0.0, RecipeScaler.parseAmount("abc"), 0.001)
        assertEquals(0.0, RecipeScaler.parseAmount(""), 0.001)
        assertEquals(0.0, RecipeScaler.parseAmount("   "), 0.001)

        // QA Test 3: Números negativos (deben limitarse defensivamente a 0.0)
        assertEquals(0.0, RecipeScaler.parseAmount("-500"), 0.001)
        assertEquals(0.0, RecipeScaler.parseAmount("-2.5"), 0.001)

        // QA Test 4: Porciones negativas o cero en escalador
        val negativeScaled = RecipeScaler.scaleAmount(100.0, baseServings = -4, targetServings = -10)
        assertTrue(negativeScaled >= 0.0)
        assertTrue(negativeScaled.isFinite())
    }
}
