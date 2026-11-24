package com.example.remedia.presentation.viewmodel

/**
 * Resultado confirmado del escáner.
 *
 * [consulta] es lo que el usuario vio y aceptó (o escribió a mano). [contexto]
 * guarda el texto completo del OCR para que la búsqueda de respaldo (Gemini o
 * heurística) pueda recuperar un medicamento aunque la extracción haya elegido
 * una palabra equivocada.
 */
data class TextoEscaneado(
    val consulta: String,
    val contexto: String = consulta
)
