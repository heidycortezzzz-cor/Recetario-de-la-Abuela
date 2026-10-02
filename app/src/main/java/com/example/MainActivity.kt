package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Recipe
import com.example.ui.RecipeViewModel
import com.example.ui.screens.AddRecipeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RecipeDetailScreen
import com.example.ui.theme.MyApplicationTheme

/**
 * Destinos de navegación de la aplicación.
 */
sealed interface AppScreen {
    object Home : AppScreen
    data class Detail(val recipeId: Long) : AppScreen
    object Add : AppScreen
}

class MainActivity : ComponentActivity() {

    private val viewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RecetarioApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RecetarioApp(viewModel: RecipeViewModel) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    val allRecipes by viewModel.allRecipes.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            is AppScreen.Home -> {
                HomeScreen(
                    viewModel = viewModel,
                    onRecipeClick = { recipe ->
                        viewModel.selectRecipe(recipe)
                        currentScreen = AppScreen.Detail(recipe.id)
                    },
                    onAddRecipeClick = {
                        currentScreen = AppScreen.Add
                    }
                )
            }
            is AppScreen.Detail -> {
                // Obtenemos la versión reactiva más reciente de la receta
                val activeRecipe = allRecipes.find { it.id == screen.recipeId }
                if (activeRecipe != null) {
                    RecipeDetailScreen(
                        recipe = activeRecipe,
                        viewModel = viewModel,
                        onBack = {
                            currentScreen = AppScreen.Home
                        }
                    )
                } else {
                    // Si fue borrada o no se encuentra, regresamos al inicio
                    currentScreen = AppScreen.Home
                }
            }
            is AppScreen.Add -> {
                AddRecipeScreen(
                    viewModel = viewModel,
                    onBack = {
                        currentScreen = AppScreen.Home
                    }
                )
            }
        }
    }
}
