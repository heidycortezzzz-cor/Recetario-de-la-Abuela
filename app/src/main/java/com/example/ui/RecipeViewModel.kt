package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Ingredient
import com.example.data.model.Recipe
import com.example.data.repository.RecipeRepository
import com.example.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Normalizer

/**
 * ViewModel principal de la aplicación.
 *
 * PUNTOS CRÍTICOS DONDE ALGUIEN SUELE EQUIVOCARSE:
 * 1. BÚSQUEDA POR INGREDIENTE: Si se realiza una simple comparación `contains` sin normalizar,
 *    una búsqueda de "limon" no encontraría "limón", o "Huevo" fallaría con "huevo".
 *    Usamos normalización Unicode NFD para eliminar tildes y diacríticos, y convertimos a minúsculas.
 * 2. RECALCULO DE PORCIONES EN TIEMPO REAL: El estado de [currentServings] debe mantenerse
 *    por separado de [baseServings] para nunca sobreescribir la receta original en base de datos
 *    a menos que el usuario explícitamente lo guarde.
 */
class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipeRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RecipeRepository(database.recipeDao())
        viewModelScope.launch {
            repository.ensureSampleData()
        }
    }

    // Texto de búsqueda por ingrediente ingresado por el usuario
    private val _searchIngredientQuery = MutableStateFlow("")
    val searchIngredientQuery: StateFlow<String> = _searchIngredientQuery.asStateFlow()

    // Flujo con todas las recetas almacenadas en Room
    val allRecipes: StateFlow<List<Recipe>> = repository.allRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Recetas filtradas reactivamente según el ingrediente buscado
    val filteredRecipes: StateFlow<List<Recipe>> = combine(allRecipes, _searchIngredientQuery) { recipes, query ->
        val cleanQuery = normalizeText(query)
        if (cleanQuery.isBlank()) {
            recipes
        } else {
            recipes.filter { recipe ->
                // Busca si al menos un ingrediente contiene el texto consultado
                recipe.ingredients.any { ingredient ->
                    normalizeText(ingredient.name).contains(cleanQuery)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Receta actualmente seleccionada en pantalla de detalle
    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    // Porciones seleccionadas para la conversión interactiva
    private val _currentServings = MutableStateFlow(4)
    val currentServings: StateFlow<Int> = _currentServings.asStateFlow()

    fun onSearchQueryChanged(newQuery: String) {
        _searchIngredientQuery.value = newQuery
    }

    fun clearSearch() {
        _searchIngredientQuery.value = ""
    }

    fun selectRecipe(recipe: Recipe) {
        _selectedRecipe.value = recipe
        _currentServings.value = recipe.baseServings.coerceAtLeast(1)
    }

    fun updateServings(newServings: Int) {
        // Garantizamos un mínimo de 1 porción para evitar divisiones inválidas
        _currentServings.value = newServings.coerceIn(1, 999)
    }

    fun incrementServings() {
        _currentServings.value = (_currentServings.value + 1).coerceAtMost(999)
    }

    fun decrementServings() {
        if (_currentServings.value > 1) {
            _currentServings.value -= 1
        }
    }

    /**
     * Guarda una nueva receta en la base de datos local.
     */
    fun saveNewRecipe(
        title: String,
        baseServings: Int,
        ingredients: List<Ingredient>,
        instructions: List<String>,
        photoUri: Uri?,
        samplePhotoKey: String?
    ) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            // Si el usuario eligió una foto de su galería, la copiamos al almacenamiento permanente
            val resolvedPhotoPath = if (photoUri != null) {
                ImageUtils.saveImageToInternalStorage(context, photoUri)
            } else if (!samplePhotoKey.isNullOrBlank()) {
                "sample:$samplePhotoKey"
            } else {
                null
            }

            val recipe = Recipe(
                title = title.trim(),
                baseServings = baseServings.coerceAtLeast(1),
                ingredients = ingredients.filter { it.name.isNotBlank() },
                instructions = instructions.filter { it.isNotBlank() },
                photoUri = resolvedPhotoPath
            )

            repository.insertRecipe(recipe)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
            if (_selectedRecipe.value?.id == recipe.id) {
                _selectedRecipe.value = null
            }
        }
    }

    companion object {
        /**
         * Normaliza texto para búsqueda: elimina mayúsculas y acentos (ej: "Limón" -> "limon").
         */
        fun normalizeText(text: String): String {
            if (text.isBlank()) return ""
            val nfdNormalized = Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
            val withoutDiacritics = nfdNormalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            return withoutDiacritics.lowercase()
        }
    }
}
