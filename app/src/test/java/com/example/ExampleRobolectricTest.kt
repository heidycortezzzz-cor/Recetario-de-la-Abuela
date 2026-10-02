package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Ingredient
import com.example.data.model.Recipe
import com.example.util.RecipeBackupManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Recetario de la Abuela", appName)
  }

  @Test
  fun `test backup export and import json with base servings`() {
    val originalRecipe = Recipe(
        id = 1,
        title = "Torta Frita de la Nonna",
        baseServings = 6,
        ingredients = listOf(
            Ingredient(name = "Harina 0000", amount = 500.0, unit = "g"),
            Ingredient(name = "Grasa vacuna", amount = 100.0, unit = "g"),
            Ingredient(name = "Salmuera tibia", amount = 200.0, unit = "ml")
        ),
        instructions = listOf(
            "Formar una corona con la harina y agregar la grasa derretida.",
            "Amasar hasta obtener una masa suave y dejar reposar 30 minutos.",
            "Estirar, cortar discos y freír en grasa caliente."
        )
    )

    val jsonString = RecipeBackupManager.exportRecipesToJson(listOf(originalRecipe))
    assertTrue(jsonString.contains("Torta Frita de la Nonna"))
    assertTrue(jsonString.contains("\"baseServings\": 6"))

    val parsed = RecipeBackupManager.parseRecipesFromJson(jsonString)
    assertEquals(1, parsed.size)
    assertEquals("Torta Frita de la Nonna", parsed[0].title)
    assertEquals(6, parsed[0].baseServings)
    assertEquals(3, parsed[0].ingredients.size)
    assertEquals(500.0, parsed[0].ingredients[0].amount, 0.001)
    assertEquals("Harina 0000", parsed[0].ingredients[0].name)
  }
}
