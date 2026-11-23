package com.example.remedia.presentation.viewmodel

import java.util.Locale

/**
 * Elige, entre todo lo que lee el OCR, la línea que probablemente es el nombre
 * del medicamento. Es lógica pura (sin Android ni ML Kit) para poder probarla
 * con JUnit local: la cámara solo entrega `List<String>` y aquí se decide qué
 * se muestra y qué se envía como consulta a la búsqueda.
 */
internal object TextoMedicamentoExtractor {

    private val SALTOS = Regex("[\\r\\n]+")
    private val ESPACIOS = Regex("\\s+")

    /** Texto que suele aparecer en la caja pero que no es el nombre del producto. */
    private val RUIDO = listOf(
        "invima", "registro sanitario", "registro invima", "lote", "vencimiento",
        "vence", "fecha", "código", "codigo", "cum", "laboratorio", "fabricante",
        "hecho en", "venta libre", "fórmula", "formula", "advertencia",
        "contraindicaciones", "conservar"
    )

    fun extraer(textos: List<String>): String? = textos.asSequence()
        .flatMap { it.split(SALTOS).asSequence() }
        .map { it.trim().replace(ESPACIOS, " ").trim() }
        .filter(::esCandidato)
        .maxByOrNull(::puntuacion)

    private fun esCandidato(texto: String): Boolean {
        if (texto.length !in 4..120) return false

        val minusculas = texto.lowercase(Locale.ROOT)
        if (RUIDO.any { minusculas.contains(it) }) return false

        val letras = texto.count { it.isLetter() }
        val digitos = texto.count { it.isDigit() }
        return letras >= 3 && letras >= digitos
    }

    /** Prefiere nombres largos y con varias palabras; penaliza los números sueltos. */
    private fun puntuacion(texto: String): Int {
        val letras = texto.count { it.isLetter() }
        val palabras = texto.split(' ').count { it.isNotBlank() }
        val digitos = texto.count { it.isDigit() }
        return letras + (palabras * 5) - (digitos * 2)
    }
}
