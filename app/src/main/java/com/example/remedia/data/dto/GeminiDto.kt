package com.example.remedia.data.dto

/**
 * DTO mínimos de la API de Gemini (generateContent). Solo pedimos texto: no
 * usamos funciones ni modo multimodal, así el contrato se mantiene pequeño.
 */

data class GeminiRequest(
    val contents: List<GeminiContenido>,
    val generationConfig: GeminiConfiguracion = GeminiConfiguracion()
)

data class GeminiContenido(
    val parts: List<GeminiParte>,
    val role: String = "user"
)

data class GeminiParte(val text: String)

data class GeminiConfiguracion(
    val temperature: Double = 0.1,
    val maxOutputTokens: Int = 80,
    val responseMimeType: String = "application/json"
)

data class GeminiRespuesta(
    val candidates: List<GeminiCandidato>? = null
)

data class GeminiCandidato(
    val content: GeminiContenido? = null
)
