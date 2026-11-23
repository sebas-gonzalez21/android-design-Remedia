package com.example.remedia.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Contrato de persistencia de las búsquedas recientes (sin Android en la firma). */
interface RecientesDataSource {
    val recientes: Flow<List<String>>
    suspend fun guardar(recientes: List<String>)
}

private val Context.recientesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "recientes"
)

/**
 * Guarda la lista como JSON (no como Set de preferencias) para conservar el orden
 * de más reciente a más antiguo. Un dato corrupto se trata como historial vacío.
 */
class RecientesDataStore(private val context: Context) : RecientesDataSource {

    private val clave = stringPreferencesKey("historial_json")
    private val json = Json { ignoreUnknownKeys = true }

    override val recientes: Flow<List<String>> = context.recientesDataStore.data.map { preferencias ->
        val contenido = preferencias[clave] ?: "[]"
        try {
            json.decodeFromString<List<String>>(contenido)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun guardar(recientes: List<String>) {
        context.recientesDataStore.edit { preferencias ->
            preferencias[clave] = json.encodeToString(recientes)
        }
    }
}
