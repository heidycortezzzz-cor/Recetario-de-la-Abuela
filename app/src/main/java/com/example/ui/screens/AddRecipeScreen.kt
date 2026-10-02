package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Ingredient
import com.example.ui.RecipeViewModel

/**
 * Estado mutable para la fila de edición de un ingrediente.
 */
data class EditableIngredient(
    var name: String = "",
    var amountText: String = "",
    var unit: String = "g"
)

/**
 * Pantalla para guardar una nueva receta.
 *
 * PUNTOS CRÍTICOS DONDE ALGUIEN SUELE EQUIVOCARSE:
 * 1. PARSEO DE CANTIDADES:
 *    El usuario puede ingresar "500", "2.5", "2,5" o dejar el campo en blanco.
 *    Reemplazamos comas por puntos y usamos [toDoubleOrNull] con valor por defecto 0.0
 *    para evitar que la app crashee con [NumberFormatException].
 * 2. PERMISOS DE FOTO:
 *    Usamos el Photo Picker oficial de Android (`PickVisualMedia`) que no requiere permisos
 *    invasivos en el manifiesto (`READ_MEDIA_IMAGES` está prohibido en Play Store para apps generales).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    viewModel: RecipeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var baseServings by remember { mutableIntStateOf(4) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedSamplePhoto by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Lista mutable de ingredientes
    val ingredients = remember {
        mutableStateListOf(
            EditableIngredient(name = "", amountText = "", unit = "g"),
            EditableIngredient(name = "", amountText = "", unit = "unidades")
        )
    }

    // Lista mutable de pasos de preparación
    val steps = remember {
        mutableStateListOf("")
    }

    // Photo Picker oficial de Android (Cero permisos)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
            selectedSamplePhoto = null
        }
    }

    val commonUnits = listOf("g", "kg", "ml", "l", "tazas", "cda", "cdta", "unidades", "al gusto")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Escribir Nueva Receta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("add_recipe_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // ==========================================
            // SELECCIÓN DE FOTO DE LA RECETA
            // ==========================================
            Text(
                text = "Foto de la receta",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("recipe_photo_picker_box"),
                contentAlignment = Alignment.Center
            ) {
                when {
                    selectedPhotoUri != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(selectedPhotoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto elegida",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedPhotoUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Eliminar foto")
                        }
                    }
                    selectedSamplePhoto == "empanadas" -> {
                        Image(
                            painter = painterResource(id = R.drawable.recipe_empanadas),
                            contentDescription = "Empanadas",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    selectedSamplePhoto == "guiso" -> {
                        Image(
                            painter = painterResource(id = R.drawable.recipe_guiso),
                            contentDescription = "Guiso",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Toca para elegir foto de tu galería",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "O selecciona una imagen casera sugerida abajo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Opciones de fotos sugeridas si el usuario no tiene fotos en el dispositivo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSamplePhoto == "empanadas",
                    onClick = {
                        selectedSamplePhoto = "empanadas"
                        selectedPhotoUri = null
                    },
                    label = { Text("Foto Empanadas") }
                )
                FilterChip(
                    selected = selectedSamplePhoto == "guiso",
                    onClick = {
                        selectedSamplePhoto = "guiso"
                        selectedPhotoUri = null
                    },
                    label = { Text("Foto Cazuela/Guiso") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // NOMBRE DE LA RECETA
            // ==========================================
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = null
                },
                label = { Text("Nombre de la receta familiar") },
                placeholder = { Text("Ej. Milanesas a la napolitana de la nonna") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recipe_title_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // PORCIONES BASE
            // ==========================================
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Porciones base",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Para cuántas personas rinde la receta original",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedIconButton(
                            onClick = { if (baseServings > 1) baseServings-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos porciones")
                        }

                        Text(
                            text = "$baseServings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 14.dp)
                                .testTag("add_recipe_servings_text")
                        )

                        OutlinedIconButton(
                            onClick = { if (baseServings < 99) baseServings++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Más porciones")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // INGREDIENTES CON CANTIDAD Y UNIDAD
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ingredientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Con cantidad para recalcular",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ingredients.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Nombre del ingrediente
                            OutlinedTextField(
                                value = item.name,
                                onValueChange = {
                                    ingredients[index] = item.copy(name = it)
                                    errorMessage = null
                                },
                                label = { Text("Ingrediente #${index + 1}") },
                                placeholder = { Text("ej. Cebolla picada") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ingredient_name_input_$index"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            if (ingredients.size > 1) {
                                IconButton(
                                    onClick = { ingredients.removeAt(index) },
                                    modifier = Modifier.testTag("remove_ingredient_button_$index")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Quitar ingrediente",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Cantidad numérica
                            OutlinedTextField(
                                value = item.amountText,
                                onValueChange = {
                                    // Acepta números y coma/punto decimal
                                    ingredients[index] = item.copy(amountText = it)
                                },
                                label = { Text("Cantidad") },
                                placeholder = { Text("ej. 500") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .width(110.dp)
                                    .testTag("ingredient_amount_input_$index"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Selector de unidad
                            OutlinedTextField(
                                value = item.unit,
                                onValueChange = {
                                    ingredients[index] = item.copy(unit = it)
                                },
                                label = { Text("Unidad") },
                                placeholder = { Text("g, tazas, etc.") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ingredient_unit_input_$index"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // Chips de unidades rápidas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("g", "kg", "ml", "tazas", "cda", "unidades", "al gusto").forEach { u ->
                                FilterChip(
                                    selected = item.unit == u,
                                    onClick = { ingredients[index] = item.copy(unit = u) },
                                    label = { Text(u, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    ingredients.add(EditableIngredient(name = "", amountText = "", unit = "g"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("add_ingredient_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar otro ingrediente")
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ==========================================
            // PASOS DE PREPARACIÓN
            // ==========================================
            Text(
                text = "Pasos de preparación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            steps.forEachIndexed { index, stepText ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(top = 10.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = stepText,
                        onValueChange = { steps[index] = it },
                        label = { Text("Paso ${index + 1}") },
                        placeholder = { Text("Describe el paso de la receta...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("step_input_$index"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (steps.size > 1) {
                        IconButton(
                            onClick = { steps.removeAt(index) },
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .testTag("remove_step_button_$index")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Quitar paso",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { steps.add("") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("add_step_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar otro paso")
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ==========================================
            // BOTÓN PRINCIPAL DE GUARDAR RECETA
            // ==========================================
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Por favor ingresa un nombre para la receta."
                        return@Button
                    }

                    val validIngredients = ingredients.mapNotNull {
                        val trimmedName = it.name.trim()
                        if (trimmedName.isNotBlank()) {
                            val amount = com.example.util.RecipeScaler.parseAmount(it.amountText)
                            Ingredient(name = trimmedName, amount = amount, unit = it.unit.trim())
                        } else null
                    }

                    if (validIngredients.isEmpty()) {
                        errorMessage = "Por favor ingresa al menos un ingrediente para la receta."
                        return@Button
                    }

                    val validSteps = steps.map { it.trim() }.filter { it.isNotBlank() }

                    viewModel.saveNewRecipe(
                        title = title,
                        baseServings = baseServings,
                        ingredients = validIngredients,
                        instructions = if (validSteps.isEmpty()) listOf("Preparar con cariño y servir caliente.") else validSteps,
                        photoUri = selectedPhotoUri,
                        samplePhotoKey = selectedSamplePhoto
                    )

                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_recipe_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardar en el Recetario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
