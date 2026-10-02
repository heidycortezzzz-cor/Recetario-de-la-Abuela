package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Utilidades para manejo seguro de fotos de recetas en Android.
 *
 * PUNTOS CRÍTICOS DONDE ALGUIEN SUELE EQUIVOCARSE:
 * 1. PERSISTENCIA DE URIs DE GALERÍA:
 *    Cuando un usuario selecciona una imagen usando el Photo Picker (`ActivityResultContracts.PickVisualMedia`),
 *    Android otorga un permiso transitorio de lectura a la URI (`content://...`).
 *    Si la aplicación se cierra, o el dispositivo se reinicia, intentar cargar ese URI almacenado
 *    arrojará un `SecurityException` porque el permiso temporal expiró.
 *    SOLUCIÓN ROBUSTA: Copiar los bytes de la imagen inmediatamente al directorio privado
 *    interno de la app (`context.filesDir/recipe_photos/`). De esta forma la imagen queda
 *    guardada de por vida, sin depender de permisos externos ni conexión.
 */
object ImageUtils {

    /**
     * Guarda una copia local de la imagen seleccionada y retorna la ruta absoluta del archivo local.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val photosDir = File(context.filesDir, "recipe_photos").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "recipe_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destinationFile = File(photosDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile.absolutePath
        } catch (_: Exception) {
            // En caso de fallo de I/O, retornamos null de forma segura
            null
        }
    }
}
