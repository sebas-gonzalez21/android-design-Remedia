package com.example.remedia.data.repository

import com.example.remedia.data.model.Medicamento

/**
 * Ordena los resultados por parecido con lo que se escaneó.
 *
 * La API devuelve las filas en su propio orden: para "ASPIRINA" muestra primero
 * CARDIOASPIRINA 81MG y después ASPIRINA 500MG, aunque la caja dijera
 * "Aspirina 100mg Tableta". Aquí se premian la coincidencia del nombre, la dosis
 * y la forma farmacéutica para que el resultado más parecido quede arriba.
 */
internal object SimilitudMedicamento {

    private val SIN_RUIDO = Regex("[^A-Z0-9 ]")
    private val ESPACIOS = Regex("\\s+")
    private val NUMEROS = Regex("\\d+")

    /** La caja dice "Tableta" y el CUM dice "TABLETAS": son la misma familia. */
    private val FAMILIAS_FORMA = mapOf(
        "TABLETA" to "TABLETA",
        "TABLETAS" to "TABLETA",
        "COMPRIMIDO" to "TABLETA",
        "COMPRIMIDOS" to "TABLETA",
        "CAPSULA" to "CAPSULA",
        "CAPSULAS" to "CAPSULA",
        "GRANULADO" to "GRANULADO",
        "POLVO" to "POLVO",
        "SOLUCION" to "SOLUCION",
        "INYECTABLE" to "INYECTABLE",
        "GOTAS" to "GOTAS",
        "CREMA" to "CREMA",
        "UNGUENTO" to "UNGUENTO",
        "JARABE" to "JARABE",
        "SUSPENSION" to "SUSPENSION",
        "EFERVESCENTE" to "EFERVESCENTE"
    )

    fun ordenar(
        medicamentos: List<Medicamento>,
        pista: String,
        nombreBuscado: String? = null
    ): List<Medicamento> {
        val tokensPista = tokens(pista)
        val tokensPistaSet = tokensPista.toSet()
        val dosis = numeros(pista)
        val formas = tokensPista.mapNotNull { FAMILIAS_FORMA[it] }.toSet()
        val nombre = tokens(nombreBuscado).joinToString(" ")

        return medicamentos.sortedByDescending { medicamento ->
            val nombreMed = tokens(medicamento.nombre).joinToString(" ")
            val dosisMed = numeros(medicamento.nombre)
            val formaMed = (tokens(medicamento.nombre) + tokens(medicamento.formaFarmaceutica))
                .mapNotNull { FAMILIAS_FORMA[it] }
                .toSet()

            var puntos = 0

            if (nombre.isNotBlank()) {
                when {
                    nombreMed.startsWith(nombre) -> puntos += 10
                    // CARDIOASPIRINA contiene ASPIRINA, pero no es la caja escaneada.
                    nombreMed.contains(nombre) -> puntos += 1
                }
            } else {
                puntos += tokens(medicamento.nombre)
                    .count { it.length >= 4 && it in tokensPistaSet } * 2
            }

            puntos += dosis.count { it in dosisMed } * 6
            if (formas.any { it in formaMed }) puntos += 3

            medicamento.principiosActivos.forEach { activo ->
                if (tokens(activo).any { it.length >= 4 && it in tokensPistaSet }) puntos += 2
            }

            puntos
        }
    }

    private fun tokens(texto: String?): List<String> =
        normalizarTexto(texto)
            ?.replace(SIN_RUIDO, " ")
            ?.split(ESPACIOS)
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    /** "100mg" y "100 MG" valen lo mismo; evita comparar "100MG" con "100". */
    private fun numeros(texto: String?): Set<String> =
        normalizarTexto(texto)
            ?.let { NUMEROS.findAll(it) }
            ?.map { it.value }
            ?.filter { it.length in 1..4 }
            ?.toSet()
            ?: emptySet()
}
