package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.Recipe

/**
 * Base de datos principal de la aplicación mediante Room SQLite.
 *
 * PUNTOS CRÍTICOS DONDE ALGUIEN SUELE EQUIVOCARSE:
 * 1. Instanciar múltiples bases de datos concurrentes: debe usarse el patrón Singleton
 *    con [@Volatile] y [synchronized] para prevenir condiciones de carrera y fugas de memoria.
 * 2. Olvidar registrar la clase [Converters] en la anotación [@TypeConverters], lo que
 *    provocaría un error de compilación KSP al no saber cómo persistir las listas.
 */
@Database(entities = [Recipe::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recetario_abuela_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
