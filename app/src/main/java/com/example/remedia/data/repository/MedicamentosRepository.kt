package com.example.remedia.data.repository

import com.example.remedia.data.dto.MedicamentoDto
import com.example.remedia.data.network.RetrofitClient
import com.example.remedia.presentation.model.MedicamentoUi

/**
 * Puente entre la API (Retrofit) y la Vista. Los ViewModels solo hablan con esto.
 *
 * El dataset de datos.gov.co ("ec4u-mzse") es de CONTROL DE PRECIOS, no el registro
 * INVIMA: no trae expediente, vigencia ni venta libre reales. Esos campos se rellenan
 * con un valor por defecto honesto hasta que se conecte una fuente que sí los tenga.
 */
object MedicamentosRepository {

    private val cachePorExpediente = mutableMapOf<String, MedicamentoUi>()

    suspend fun buscar(texto: String): List<MedicamentoUi> =
        RetrofitClient.api.buscar(texto = texto).map { it.toUi() }.also(::actualizarCache)

    suspend fun porPrincipioActivo(principioActivo: String): List<MedicamentoUi> =
        RetrofitClient.api.porPrincipioActivo(principioActivo = principioActivo)
            .map { it.toUi() }
            .also(::actualizarCache)

    /** No existe endpoint por id único en la API; se resuelve contra lo último consultado. */
    fun obtenerDeCache(expediente: String): MedicamentoUi? = cachePorExpediente[expediente]

    private fun actualizarCache(lista: List<MedicamentoUi>) {
        lista.forEach { cachePorExpediente[it.expediente] = it }
    }

    private fun MedicamentoDto.toUi(): MedicamentoUi {
        val nombreBase = nombreComercial?.trim().orEmpty().ifBlank { "Medicamento sin nombre" }
        val nombreCompleto = concentracion?.trim()
            .takeUnless { it.isNullOrBlank() }
            ?.let { "$nombreBase $it" }
            ?: nombreBase
        val claveUnica = listOf(nombreBase, concentracion.orEmpty(), fabricante.orEmpty())
            .joinToString("|")

        return MedicamentoUi(
            expediente = claveUnica.hashCode().toString(),
            nombre = nombreCompleto,
            laboratorio = fabricante?.trim().orEmpty().ifBlank { "No especificado" },
            formaFarmaceutica = unidadDispensacion?.trim().orEmpty().ifBlank { "No especificado" },
            vigente = true, // el dataset de precios no marca vigencia; se asume vigente si está listado
            ventaLibre = false, // el dataset no distingue venta libre; no hay forma de inferirlo aquí
            paraQueSirve = principioActivo?.trim().orEmpty().ifBlank { "No especificado" },
            principiosActivos = principioActivo?.let { listOf(it) } ?: emptyList(),
            registroInvima = "No disponible en este dataset",
            precioMaximo = precioPorTableta?.trim()?.takeUnless { it.isBlank() }?.let { "$ $it" }
        )
    }
}
