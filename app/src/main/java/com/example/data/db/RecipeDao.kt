package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Recipe
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operaciones sobre la tabla de recetas.
 *
 * PUNTOS CRÍTICOS DONDE ALGUIEN SUELE EQUIVOCARSE:
 * 1. Olvidar marcar las funciones de mutación ([insertRecipe], [deleteRecipe]) como `suspend`.
 *    Room prohibe operaciones de escritura en el hilo principal (Main Thread).
 * 2. Para lectura reactiva, retornar [Flow] asegura que la UI se actualice automáticamente
 *    cuando se agregue o elimine una receta, sin necesidad de recargas manuales.
 */
@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    fun getRecipeById(id: Long): Flow<Recipe?>

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Long)

    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    suspend fun getAllRecipesSnapshot(): List<Recipe>

    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()
}
