package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import java.io.File

/**
 * Componente unificado para renderizar fotos de recetas.
 * Maneja fotos locales guardadas en almacenamiento interno, fotos de muestra precargadas
 * y estados de carga o ausencia de foto con un placeholder acogedor.
 */
@Composable
fun RecipeImage(
    photoUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    when {
        // Foto de muestra precargada
        photoUri == "sample:empanadas" -> {
            Image(
                painter = painterResource(id = R.drawable.recipe_empanadas),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        photoUri == "sample:guiso" -> {
            Image(
                painter = painterResource(id = R.drawable.recipe_guiso),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        // Archivo local guardado
        !photoUri.isNullOrBlank() && File(photoUri).exists() -> {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(photoUri))
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        // Fallback genérico cálido
        else -> {
            Box(
                modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}
