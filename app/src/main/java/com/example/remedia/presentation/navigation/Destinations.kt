package com.example.remedia.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Rutas type-safe de la app: cada pantalla es un objeto o clase serializable
 * en vez de un String suelto (nada de "detalle/{expediente}").
 *
 * Al agruparlas en un sealed interface, el compilador conoce todas las rutas posibles.
 */
sealed interface Destination {

    @Serializable
    data object Inicio : Destination

    @Serializable
    data object Escaner : Destination

    /**
     * [consulta] es el texto buscado o el principio activo (en "Ver equivalentes").
     * [contexto] guarda el OCR completo cuando el origen es el escáner.
     */
    @Serializable
    data class Resultados(val consulta: String, val contexto: String = "") : Destination

    @Serializable
    data class Detalle(val expediente: String) : Destination
}