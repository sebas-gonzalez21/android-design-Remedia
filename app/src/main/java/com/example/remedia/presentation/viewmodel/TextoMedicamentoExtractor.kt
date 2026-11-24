package com.example.remedia.presentation.viewmodel

import java.text.Normalizer
import java.util.Locale

/**
 * Elige, entre todo lo que lee el OCR, el texto que probablemente es el nombre
 * del medicamento. Es lÃ³gica pura (sin Android ni ML Kit) para poder probarla
 * con JUnit local: la cÃ¡mara solo entrega `List<String>` y aquÃ­ se decide quÃ©
 * se muestra y quÃ© se envÃ­a como consulta a la bÃºsqueda.
 *
 * El OCR de una caja mezcla el nombre con dosis, forma farmacÃ©utica, registros
 * y texto de otras caras de la caja. Por eso no basta con la lÃ­nea mÃ¡s larga:
 * se separa por separadores visuales, se descarta el ruido y se premian los
 * terminaciones tÃ­picas de principios activos (`-ina`, `-ol`, `-pril`, ...).
 */
internal object TextoMedicamentoExtractor {

    private data class Candidato(val texto: String, val puntos: Int)

    private val SALTOS = Regex("[\\r\\n]+")
    /** El OCR suele leer `|` como `I` y tambiÃ©n separa con `/` o `Â·`. */
    private val SEPARADORES = Regex("\\s*(?:[|/Â·â€¢]|\\bI\\b)\\s*", RegexOption.IGNORE_CASE)
    private val ESPACIOS = Regex("\\s+")
    private val DIACRITICAS = Regex("\\p{M}+")
    private val NO_ALFANUMERICO = Regex("[^A-Z0-9 ]")
    private val CONSONANTES_LARGAS = Regex("[BCDFGHJKLMNPQRSTVWXYZ]{3,}")

    /** Terminaciones frecuentes en nombres de principios activos. */
    private val SUFIJOS_FARMACO = listOf(
        "INA", "OL", "OLOL", "PRIL", "SARTAN", "CINA", "MICINA",
        "ZEPAM", "FENO", "TINA", "PIDO", "FLOXACINO", "CICLOVIR", "STATINA"
    )

    /** Si aparece una de estas palabras, el segmento es de la ficha, no del nombre. */
    private val RUIDO_FUERTE = setOf(
        "INVIMA", "REGISTRO", "SANITARIO", "LOTE", "VENCE", "VENCIMIENTO",
        "FECHA", "CODIGO", "CUM", "LABORATORIO", "FABRICANTE",
        "ADVERTENCIA", "CONTRAINDICACIONES", "CONSERVAR"
    )

    /** Dosis, forma farmacÃ©utica y conectores que no identifican el producto. */
    private val RUIDO = setOf(
        "ALVIO", "DOLOR", "GENERAL", "TABLETA", "TABLETAS", "CAPSULA", "CAPSULAS",
        "COMPRIMIDO", "GRANULADO", "POLVO", "SOLUCION", "INYECTABLE", "GOTAS",
        "CREMA", "UNGUENTO", "JARABE", "SUSPENSION", "CAJA", "FRASCO", "SOBRE",
        "BLISTER", "MG", "ML", "MCG", "UI", "DEL", "LAS", "LOS", "CON", "PARA",
        "POR", "TA", "RE", "DE", "Y",
        // Palabras que el OCR suele inventar al leer "medicamentos" o la cabecera.
        "MEDICAMENTOS", "MEDICAMENTO", "MEDICAMENT", "MEDCANERTS", "MEDCAMENTS",
        "INCE", "FARMACIA", "FARMACEUTICA", "LABORATORIOS", "VISION", "LECTURA"
    )

    /**
     * @param anterior texto ya mostrado en el visor. Se conserva si el nuevo
     * candidato es peor; asÃ­ el nombre no parpadea entre frames del OCR.
     */
    fun extraer(textos: List<String>, anterior: String = ""): String? {
        val mejor = textos.asSequence()
            .flatMap { it.split(SALTOS).asSequence() }
            .flatMap { it.split(SEPARADORES).asSequence() }
            .map(::analizar)
            .filter { it.puntos > 0 }
            .maxByOrNull { it.puntos }
            ?: return null

        val previo = anterior.takeIf { it.isNotBlank() }?.let(::analizar)?.takeIf { it.puntos > 0 }
        return when {
            previo == null || mejor.puntos > previo.puntos -> mejor.texto
            else -> previo.texto
        }
    }

    private fun analizar(texto: String): Candidato {
        val normalizado = normalizar(texto)
        if (normalizado.isBlank() || normalizado.length > 120) return Candidato("", 0)

        val tokens = normalizado.split(ESPACIOS).filter { it.isNotBlank() }
        if (tokens.any { it in RUIDO_FUERTE }) return Candidato("", 0)

        val utiles = tokens.filter { token ->
            token.length >= 2 && token.none { it.isDigit() } && token !in RUIDO
        }
        if (utiles.isEmpty()) return Candidato("", 0)

        val nombre = utiles.joinToString(" ")
        if (nombre.length !in 3..80) return Candidato("", 0)

        val base = utiles.sumOf { it.length } + utiles.size * 3
        val bonificacion = utiles.count { token ->
            SUFIJOS_FARMACO.any { token.endsWith(it) }
        } * 10
        val penalizacion =
            (tokens.size - utiles.size) +                       // dosis y conectores
            normalizado.count { it.isDigit() } +                // nÃºmeros sueltos
            CONSONANTES_LARGAS.findAll(nombre).sumOf { it.value.length - 2 } * 3

        return Candidato(nombre, (base + bonificacion - penalizacion).coerceAtLeast(1))
    }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(DIACRITICAS, "")
            .uppercase(Locale.ROOT)
            .replace(NO_ALFANUMERICO, " ")
            .replace(ESPACIOS, " ")
            .trim()
}
