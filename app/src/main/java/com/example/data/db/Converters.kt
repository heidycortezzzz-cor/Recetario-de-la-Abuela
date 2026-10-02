package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.Ingredient
import org.json.JSONArray
import org.json.JSONObject

/**
 * Convertidores de tipos para Room Database.
 * Room no puede almacenar listas complejas (`List<Ingredient>` o `List<String>`)
 * directamente en columnas de SQLite primitivas.
 *
 * PUNTOS CRÍTICOS DONDE ALGUIEN SUELE EQUIVOCARSE:
 * 1. Confiar en serialización que dependa de librerías externas o reflexión que Proguard/R8
 *    pueda ofuscar y romper en Release. Usar `org.json` de Android nativo es 100% estable.
 * 2. No manejar cadenas nulas o malformadas: si la columna viene vacía o corrupta,
 *    debe retornar una lista vacía en lugar de arrojar una excepción y crashear la app.
 */
class Converters {

    @TypeConverter
    fun fromIngredientsList(ingredients: List<Ingredient>?): String {
        if (ingredients.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        for (item in ingredients) {
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("amount", item.amount)
            obj.put("unit", item.unit)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toIngredientsList(jsonString: String?): List<Ingredient> {
        if (jsonString.isNullOrBlank()) return emptyList()
        val list = mutableListOf<Ingredient>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val name = obj.optString("name", "")
                val amount = obj.optDouble("amount", 0.0)
                val unit = obj.optString("unit", "")
                list.add(Ingredient(name = name, amount = amount, unit = unit))
            }
        } catch (_: Exception) {
            // Protección ante datos corruptos o versiones previas
            return emptyList()
        }
        return list
    }

    @TypeConverter
    fun fromInstructionsList(instructions: List<String>?): String {
        if (instructions.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        for (step in instructions) {
            array.put(step)
        }
        return array.toString()
    }

    @TypeConverter
    fun toInstructionsList(jsonString: String?): List<String> {
        if (jsonString.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return list
    }
}
