package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad principal para almacenar una receta en la base de datos local Room.
 *
 * PUNTOS CRÍTICOS DONDE ALGUIEN SUELE EQUIVOCARSE:
 * 1. [baseServings]: Siempre debe ser un entero estrictamente positivo (mínimo 1).
 *    Si fuese 0 o negativo, cualquier intento de calcular un factor de conversión
 *    (ej: target / base) produciría `Infinity`, `NaN` o división por cero.
 * 2. [photoUri]: En Android, los URIs de `PickVisualMedia` (`content://...`) pierden permisos
 *    cuando la aplicación se reinicia si no se solicita persistencia o no se copia el archivo.
 *    Por tanto, aquí almacenamos la ruta a una copia interna y permanente o un identificador local.
 */
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val baseServings: Int = 4,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
