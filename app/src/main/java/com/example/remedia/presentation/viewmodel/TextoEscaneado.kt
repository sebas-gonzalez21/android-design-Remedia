package com.example.remedia.presentation.viewmodel

/**
 * Resultado confirmado del escÃ¡ner.
 *
 * [consulta] es lo que el usuario vio y aceptÃ³ (o escribiÃ³ a mano). [contexto]
 * guarda el texto completo del OCR para que la bÃºsqueda de respaldo (Gemini o
 * heurÃ­stica) pueda recuperar un medicamento aunque la extracciÃ³n haya elegido
 * una palabra equivocada.
 */
data class TextoEscaneado(
    val consulta: String,
    val contexto: String = consulta
)
