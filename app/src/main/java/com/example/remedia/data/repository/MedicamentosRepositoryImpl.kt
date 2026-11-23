package com.example.remedia.data.repository

import com.example.remedia.data.api.MedicamentosApi
import com.example.remedia.data.dto.MedicamentoDto
import com.example.remedia.data.model.Medicamento
import com.example.remedia.data.network.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.Normalizer
import java.util.Locale

private val MARCAS_DIACRITICAS = Regex("\\p{M}+")
private val ESPACIOS_MULTIPLES = Regex("\\s+")

/**
 * Puente entre la API (Retrofit) y la Vista. Los ViewModels solo hablan con esto.
 *
 * Dataset i7cb-raxc (CUM Vigentes, INVIMA) para búsqueda y detalle. El precio
 * regulado (nauz-qkjw) y el catálogo de venta libre (xzwx-qpja) son datos
 * complementarios: si fallan, se muestra "sin dato" pero la pantalla sigue
 * funcionando. Nunca se tragamos una [CancellationException]: la cancelación
 * estructurada debe llegar hasta la corrutina que la originó.
 */
class MedicamentosRepositoryImpl(
    private val api: MedicamentosApi = RetrofitClient.api
) : MedicamentosRepository {

    /** Una sola carga del catálogo de venta libre por proceso, sin peticiones N+1. */
    private val catalogoMutex = Mutex()
    private var principiosVentaLibre: Set<String>? = null

    override suspend fun buscar(texto: String): List<Medicamento> {
        val medicamentos = api.buscar(texto = texto).map { it.toMedicamentoBasico() }
        val catalogo = catalogoVentaLibre() ?: return medicamentos

        return medicamentos.map { medicamento ->
            medicamento.copy(
                ventaLibre = medicamento.principiosActivos.firstOrNull().esVentaLibre(catalogo)
            )
        }
    }

    override suspend fun cargarDetalle(expediente: String): Medicamento? {
        val id = expediente.trim()
        require(id.isNotEmpty() && id.all { it.isDigit() }) { "Expediente no válido: $expediente" }

        val base = api.detalle(id).firstOrNull()?.toMedicamentoBasico() ?: return null

        return coroutineScope {
            // El detalle va primero (de él salen el expediente y el principio activo),
            // pero precio y venta libre son independientes entre sí: en paralelo.
            val precioDiferido = async { precioMaximo(id) }
            val ventaLibreDiferido = async { esVentaLibre(base.principiosActivos.firstOrNull()) }

            base.copy(
                precioMaximo = precioDiferido.await(),
                ventaLibre = ventaLibreDiferido.await()
            )
        }
    }

    private suspend fun precioMaximo(expediente: String): String? = try {
        // El expediente ya está validado como numérico; duplicar la comilla es defensa
        // en profundidad por si algún día cambia el formato del dataset.
        val where = "starts_with(cum, '${expediente.replace("'", "''")}-')"
        api.precio(where = where)
            .firstOrNull()?.precioMaximo
            ?.trim()?.takeUnless { it.isBlank() }?.let { "$ $it" }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null // Sin precio es un resultado válido, no un error.
    }

    private suspend fun esVentaLibre(principioActivo: String?): Boolean {
        val principio = normalizar(principioActivo) ?: return false
        val catalogo = catalogoVentaLibre() ?: return false
        return principio in catalogo
    }

    /** Devuelve null solo si el catálogo opcional no se pudo cargar. */
    private suspend fun catalogoVentaLibre(): Set<String>? = try {
        catalogoMutex.withLock {
            principiosVentaLibre ?: api.ventaLibrePrincipios()
                .mapNotNull { fila -> normalizar(fila["principioactivo"] as? String) }
                .toSet()
                .also { principiosVentaLibre = it }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    /**
     * Los dos datasets difieren en tildes y espacios: "ACETAMINOFEN" (CUM) frente
     * a "Acetaminofén" (venta libre). Normalizamos acentos, mayúsculas y espacios.
     */
    private fun normalizar(texto: String?): String? =
        texto?.trim()
            ?.let { Normalizer.normalize(it, Normalizer.Form.NFD) }
            ?.replace(MARCAS_DIACRITICAS, "")
            ?.replace(ESPACIOS_MULTIPLES, " ")
            ?.uppercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() }

    private fun String?.esVentaLibre(catalogo: Set<String>): Boolean =
        normalizar(this)?.let { it in catalogo } ?: false

    private fun MedicamentoDto.toMedicamentoBasico(): Medicamento {
        val nombre = producto?.trim().orEmpty().ifBlank { "Medicamento sin nombre" }
        return Medicamento(
            expediente = expediente.orEmpty(),
            nombre = nombre,
            laboratorio = titular?.trim().orEmpty().ifBlank { "No especificado" },
            formaFarmaceutica = formafarmaceutica?.trim().orEmpty().ifBlank { "No especificado" },
            vigente = estadoregistro?.trim().equals("Vigente", ignoreCase = true),
            // Se completa en buscar()/cargarDetalle() con el catálogo de venta libre.
            ventaLibre = false,
            paraQueSirve = descripcionatc?.trim().orEmpty().ifBlank { "No especificado" },
            principiosActivos = principioactivo?.let { listOf(it) } ?: emptyList(),
            registroInvima = registrosanitario?.trim().orEmpty().ifBlank { "No disponible" },
            precioMaximo = null // Solo se consulta en el Detalle.
        )
    }
}
