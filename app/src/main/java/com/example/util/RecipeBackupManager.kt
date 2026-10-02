package com.example.util

import com.example.data.model.Ingredient
import com.example.data.model.Recipe
import org.json.JSONArray
import org.json.JSONObject

/**
 * Gestor de Respaldo y Exportación a Archivo (Equivalente / Extensión de persistencia).
 *
 * En Android, mientras que en la web se usa `localStorage` para pares clave-valor de texto,
 * las aplicaciones modernas y seguras utilizan SQLite local (mediante Room).
 *
 * Esta clase permite serializar las recetas a formato JSON estándar para exportar
 * copias de seguridad a archivos externos y restaurarlas.
 */
object RecipeBackupManager {

    /**
     * Serializa una lista de recetas a un archivo o cadena JSON formateada y legible.
     * Guarda el nombre, porciones base, ingredientes con sus cantidades y pasos.
     */
    fun exportRecipesToJson(recipes: List<Recipe>): String {
        val rootArray = JSONArray()

        for (recipe in recipes) {
            val recipeObj = JSONObject()
            recipeObj.put("title", recipe.title)
            recipeObj.put("baseServings", recipe.baseServings)
            recipeObj.put("photoUri", recipe.photoUri ?: "")
            recipeObj.put("createdAt", recipe.createdAt)

            val ingredientsArray = JSONArray()
            for (ing in recipe.ingredients) {
                val ingObj = JSONObject()
                ingObj.put("name", ing.name)
                ingObj.put("amount", ing.amount)
                ingObj.put("unit", ing.unit)
                ingredientsArray.put(ingObj)
            }
            recipeObj.put("ingredients", ingredientsArray)

            val instructionsArray = JSONArray()
            for (step in recipe.instructions) {
                instructionsArray.put(step)
            }
            recipeObj.put("instructions", instructionsArray)

            rootArray.put(recipeObj)
        }

        // Retorna con sangría de 2 espacios para que sea legible al usuario
        return rootArray.toString(2)
    }

    /**
     * Parsea un texto JSON proveniente de un archivo de respaldo y reconstruye la lista de recetas.
     */
    fun parseRecipesFromJson(jsonString: String): List<Recipe> {
        val list = mutableListOf<Recipe>()
        try {
            val rootArray = JSONArray(jsonString.trim())
            for (i in 0 until rootArray.length()) {
                val obj = rootArray.getJSONObject(i)
                val title = obj.optString("title", "Receta sin nombre")
                val baseServings = obj.optInt("baseServings", 4).coerceAtLeast(1)
                val photoUri = obj.optString("photoUri", "").takeIf { it.isNotBlank() }

                val ingredientsList = mutableListOf<Ingredient>()
                val ingArray = obj.optJSONArray("ingredients")
                if (ingArray != null) {
                    for (j in 0 until ingArray.length()) {
                        val ingObj = ingArray.getJSONObject(j)
                        val name = ingObj.optString("name", "")
                        val amount = ingObj.optDouble("amount", 0.0)
                        val unit = ingObj.optString("unit", "")
                        if (name.isNotBlank()) {
                            ingredientsList.add(Ingredient(name = name, amount = amount, unit = unit))
                        }
                    }
                }

                val instructionsList = mutableListOf<String>()
                val instArray = obj.optJSONArray("instructions")
                if (instArray != null) {
                    for (k in 0 until instArray.length()) {
                        val step = instArray.getString(k)
                        if (step.isNotBlank()) {
                            instructionsList.add(step)
                        }
                    }
                }

                list.add(
                    Recipe(
                        title = title,
                        baseServings = baseServings,
                        ingredients = ingredientsList,
                        instructions = instructionsList,
                        photoUri = photoUri
                    )
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return list
    }
}
