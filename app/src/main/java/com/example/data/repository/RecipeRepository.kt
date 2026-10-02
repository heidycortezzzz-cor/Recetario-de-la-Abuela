package com.example.data.repository

import com.example.data.db.RecipeDao
import com.example.data.model.Ingredient
import com.example.data.model.Recipe
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que desacopla la fuente de datos (Room) del ViewModel y la UI.
 *
 * PUNTOS CRÍTICOS DONDE ALGUIEN SUELE EQUIVOCARSE:
 * 1. Sembrado inicial de datos (Seed Data): Al iniciar la base de datos por primera vez,
 *    si se ejecuta la comprobación de conteo en el hilo principal se congelaría la pantalla.
 *    Por ello [ensureSampleData] se ejecuta de forma asíncrona suspendida.
 */
class RecipeRepository(private val recipeDao: RecipeDao) {

    val allRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()

    fun getRecipeById(id: Long): Flow<Recipe?> = recipeDao.getRecipeById(id)

    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insertRecipe(recipe)

    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)

    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)

    suspend fun deleteRecipeById(id: Long) = recipeDao.deleteRecipeById(id)

    suspend fun deleteAllRecipes() = recipeDao.deleteAllRecipes()

    /**
     * Exporta todas las recetas guardadas a una cadena JSON para respaldo en archivo.
     */
    suspend fun exportAllToJson(): String {
        val recipes = recipeDao.getAllRecipesSnapshot()
        return com.example.util.RecipeBackupManager.exportRecipesToJson(recipes)
    }

    /**
     * Importa recetas desde una cadena JSON y las guarda en la base de datos local.
     */
    suspend fun importFromJson(jsonString: String): Int {
        val parsed = com.example.util.RecipeBackupManager.parseRecipesFromJson(jsonString)
        for (recipe in parsed) {
            recipeDao.insertRecipe(recipe)
        }
        return parsed.size
    }

    /**
     * Si el recetario está vacío, precarga 2 recetas tradicionales queridas de la abuela
     * para que el usuario pueda probar de inmediato la conversión de porciones y la búsqueda.
     */
    suspend fun ensureSampleData() {
        val count = recipeDao.getRecipeCount()
        if (count == 0) {
            val sampleRecipes = listOf(
                Recipe(
                    title = "Empanadas Criollas de la Abuela",
                    baseServings = 4,
                    photoUri = "sample:empanadas",
                    ingredients = listOf(
                        Ingredient(name = "Carne picada a cuchillo", amount = 500.0, unit = "g"),
                        Ingredient(name = "Cebolla blanca", amount = 500.0, unit = "g"),
                        Ingredient(name = "Huevos duros", amount = 3.0, unit = "unidades"),
                        Ingredient(name = "Aceitunas verdes", amount = 100.0, unit = "g"),
                        Ingredient(name = "Grasa de pella o manteca", amount = 80.0, unit = "g"),
                        Ingredient(name = "Pimentón dulce y comino", amount = 0.0, unit = "al gusto"),
                        Ingredient(name = "Tapas de empanada", amount = 12.0, unit = "unidades")
                    ),
                    instructions = listOf(
                        "Picar las cebollas bien finas y rehogarlas en la grasa caliente hasta que estén tiernas y transparentes.",
                        "Agregar la carne picada a fuego vivo, cocinar apenas unos minutos para que quede jugosa y condimentar con sal, pimentón y una pizca de comino.",
                        "Retirar del fuego y enfriar completamente el picadillo en la heladera, idealmente hasta el día siguiente.",
                        "Picar los huevos duros y las aceitunas e incorporarlos suavemente al relleno frío.",
                        "Rellenar las masas de empanada, realizar el tradicional repulgue y hornear en horno fuerte a 220°C hasta que la masa dore."
                    )
                ),
                Recipe(
                    title = "Guiso Casero de Lentejas",
                    baseServings = 4,
                    photoUri = "sample:guiso",
                    ingredients = listOf(
                        Ingredient(name = "Lentejas secas", amount = 400.0, unit = "g"),
                        Ingredient(name = "Panceta ahumada", amount = 150.0, unit = "g"),
                        Ingredient(name = "Chorizo colorado", amount = 1.0, unit = "unidad"),
                        Ingredient(name = "Cebolla", amount = 2.0, unit = "unidades"),
                        Ingredient(name = "Zanahorias en rodajas", amount = 2.0, unit = "unidades"),
                        Ingredient(name = "Papas medianas", amount = 2.0, unit = "unidades"),
                        Ingredient(name = "Puré de tomate", amount = 500.0, unit = "ml"),
                        Ingredient(name = "Caldo de verduras caliente", amount = 1000.0, unit = "ml"),
                        Ingredient(name = "Hojas de laurel", amount = 2.0, unit = "unidades"),
                        Ingredient(name = "Sal, pimienta y orégano", amount = 0.0, unit = "al gusto")
                    ),
                    instructions = listOf(
                        "En una olla de fondo grueso, dorar la panceta en cubos y las rodajas de chorizo colorado sin agregar aceite extra.",
                        "Sumar las cebollas picadas y las zanahorias; sofreír a fuego medio durante 8 minutos.",
                        "Verter el puré de tomate, las lentejas enjuagadas, las hojas de laurel y el caldo caliente.",
                        "Tapar la olla y cocinar a fuego lento durante 35 minutos revolviendo de vez en cuando.",
                        "Agregar las papas en cubos medianos y cocinar 15 minutos más hasta que estén tiernas y el guiso bien espeso y reconfortante."
                    )
                )
            )

            for (recipe in sampleRecipes) {
                recipeDao.insertRecipe(recipe)
            }
        }
    }
}
