package com.example.remedia.data.repository

import com.example.remedia.data.dto.MedicamentoDto
import com.example.remedia.data.network.RetrofitClient
import com.example.remedia.presentation.model.MedicamentoUi

/**
 * Puente entre la API (Retrofit) y la Vista. Los ViewModels solo hablan con esto.
 *
 * Dataset i7cb-raxc (CUM Vigentes, INVIMA) para búsqueda y detalle: trae expediente,
 * vigencia y registro INVIMA reales. Precio regulado (nauz-qkjw) y venta libre
 * (xzwx-qpja) solo se consultan en el Detalle, tal como lo define el README (Hito 1/4):
 * no todo medicamento tiene precio regulado (solo los que están en control de precios),
 * así que "sin precio" es un resultado válido, no un error.
 */
object MedicamentosRepository {

    suspend fun buscar(texto: String): List<MedicamentoUi> =
        RetrofitClient.api.buscar(texto = texto).map { it.toUiBasico() }

    suspend fun cargarDetalle(expediente: String): MedicamentoUi? {
        val base = RetrofitClient.api.detalle(expediente).firstOrNull()?.toUiBasico() ?: return null

        val precio = runCatching {
            RetrofitClient.api.precio(where = "starts_with(cum, '$expediente-')").firstOrNull()?.precioMaximo
        }.getOrNull()

        val ventaLibre = base.principiosActivos.firstOrNull()?.let { principio ->
            runCatching { RetrofitClient.api.ventaLibre(principio).isNotEmpty() }.getOrDefault(false)
        } ?: false

        return base.copy(
            precioMaximo = precio?.trim()?.takeUnless { it.isBlank() }?.let { "$ $it" },
            ventaLibre = ventaLibre
        )
    }

    private fun MedicamentoDto.toUiBasico(): MedicamentoUi {
        val nombre = producto?.trim().orEmpty().ifBlank { "Medicamento sin nombre" }
        return MedicamentoUi(
            expediente = expediente.orEmpty(),
            nombre = nombre,
            laboratorio = titular?.trim().orEmpty().ifBlank { "No especificado" },
            formaFarmaceutica = formafarmaceutica?.trim().orEmpty().ifBlank { "No especificado" },
            vigente = estadoregistro?.trim().equals("Vigente", ignoreCase = true),
            ventaLibre = false, // se confirma solo al entrar al Detalle (dataset aparte)
            paraQueSirve = descripcionatc?.trim().orEmpty().ifBlank { "No especificado" },
            principiosActivos = principioactivo?.let { listOf(it) } ?: emptyList(),
            registroInvima = registrosanitario?.trim().orEmpty().ifBlank { "No disponible" },
            precioMaximo = null // se confirma solo al entrar al Detalle
        )
    }
}
