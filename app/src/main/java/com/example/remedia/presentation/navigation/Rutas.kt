package com.example.remedia.presentation.navigation

import android.net.Uri

/** Nombres de las rutas. Una sola fuente de verdad para no escribir strings sueltos. */
object Rutas {
    const val INICIO = "inicio"
    const val ESCANER = "escaner"

    const val ARG_CONSULTA = "consulta"
    const val RESULTADOS = "resultados/{$ARG_CONSULTA}"
    fun resultados(consulta: String) = "resultados/${Uri.encode(consulta)}"

    const val ARG_EXPEDIENTE = "expediente"
    const val DETALLE = "detalle/{$ARG_EXPEDIENTE}"
    fun detalle(expediente: String) = "detalle/${Uri.encode(expediente)}"
}
