package com.example.remedia.presentation.navigation

import kotlinx.serialization.Serializable

/** Rutas type-safe: cada pantalla es una clase/objeto serializable en vez de un String suelto. */
@Serializable
object Inicio

@Serializable
object Escaner

@Serializable
data class Resultados(val consulta: String)

@Serializable
data class Detalle(val expediente: String)
