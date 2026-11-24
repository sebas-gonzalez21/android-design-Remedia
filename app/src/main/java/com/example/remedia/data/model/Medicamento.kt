package com.example.remedia.data.model

/**
 * Entidad del dominio: un medicamento tal como lo entiende la app,
 * ya limpio (sin nulos ni textos vacíos) y listo para mostrarse.
 *
 * Vive en la capa de datos. El repositorio la construye a partir del DTO de la API
 * y los ViewModels la entregan a las pantallas. Solo `val`, con id único (expediente).
 */
data class Medicamento(
    val expediente: String,
    val nombre: String,
    val laboratorio: String,
    val formaFarmaceutica: String,
    val vigente: Boolean,
    val ventaLibre: Boolean,
    val paraQueSirve: String,
    /** True cuando el texto fue redactado por Gemini a partir de los datos del CUM. */
    val paraQueSirveIA: Boolean = false,
    /** URLs propuestas por Gemini; la pantalla solo muestra una si responde una imagen. */
    val imagenes: List<String> = emptyList(),
    val principiosActivos: List<String>,
    val registroInvima: String,
    val precioMaximo: String?
)