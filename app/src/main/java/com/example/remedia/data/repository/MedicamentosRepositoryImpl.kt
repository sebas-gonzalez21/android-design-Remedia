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
private val CARACTERES_RUIDO = Regex("[^A-Z0-9 ]")

/** Palabras que aparecen en la caja pero que no identifican el producto. */
private val PALABRAS_RUIDO = setOf(
    "ALVIO", "DOLOR", "GENERAL", "TABLETA", "TABLETAS", "CAPSULA", "CAPSULAS",
    "COMPRIMIDO", "GRANULADO", "POLVO", "SOLUCION", "INYECTABLE", "GOTAS",
    "CREMA", "UNGUENTO", "JARABE", "SUSPENSION", "CAJA", "FRASCO", "SOBRE",
    "BLISTER", "MG", "ML", "MCG", "UI", "DEL", "LAS", "LOS", "CON", "PARA", "POR"
)

/** Tope de peticiones de respaldo para no castigar la API con texto ruidoso. */
private const val MAX_CANDIDATOS = 5

/** Normaliza acentos, mayÃºsculas y espacios; se usa para comparar catÃ¡logos. */
internal fun normalizarTexto(texto: String?): String? =
    texto?.trim()
        ?.let { Normalizer.normalize(it, Normalizer.Form.NFD) }
        ?.replace(MARCAS_DIACRITICAS, "")
        ?.replace(ESPACIOS_MULTIPLES, " ")
        ?.uppercase(Locale.ROOT)
        ?.takeIf { it.isNotBlank() }

/**
 * Palabras Ãºtiles de una consulta, ordenadas por tamaÃ±o. El OCR suele entregar
 * frases largas con ruido; las palabras mÃ¡s largas suelen ser el nombre comercial
 * (por ejemplo, "Aspirina" dentro de "... Alvio Del Dolor General Aspirina ...").
 */
internal fun candidatosDeBusqueda(texto: String): List<String> =
    normalizarTexto(texto)
        ?.replace(CARACTERES_RUIDO, " ")
        ?.split(ESPACIOS_MULTIPLES)
        ?.filter { token ->
            val letras = token.count { it.isLetter() }
            val digitos = token.count { it.isDigit() }
            token.length >= 3 && letras >= 3 && digitos <= letras && token !in PALABRAS_RUIDO
        }
        ?.distinct()
        ?.sortedByDescending { it.length }
        ?: emptyList()

/**
 * Puente entre la API (Retrofit) y la Vista. Los ViewModels solo hablan con esto.
 *
 * Dataset i7cb-raxc (CUM Vigentes, INVIMA) para bÃºsqueda y detalle. El precio
 * regulado (nauz-qkjw) y el catÃ¡logo de venta libre (xzwx-qpja) son datos
 * complementarios: si fallan, se muestra "sin dato" pero la pantalla sigue
 * funcionando. Nunca se tragamos una [CancellationException]: la cancelaciÃ³n
 * estructurada debe llegar hasta la corrutina que la originÃ³.
 */
class MedicamentosRepositoryImpl(
    private val api: MedicamentosApi = RetrofitClient.api,
    private val normalizador: NormalizadorConsulta = NormalizadorHeuristico
) : MedicamentosRepository {

    /** Una sola carga del catÃ¡logo de venta libre por proceso, sin peticiones N+1. */
    private val catalogoMutex = Mutex()
    private var principiosVentaLibre: Set<String>? = null

    override suspend fun buscar(texto: String, contexto: String): List<Medicamento> {
        val consulta = texto.trim()
        val contextoOCR = contexto.trim()
            .takeIf { it.isNotBlank() && it != consulta }

        // Cuando el escÃ¡ner entrega el OCR completo, probamos primero los tÃ©rminos
        // limpios. Si el visor eligiÃ³ "INCE" pero la caja decÃ­a "ASPIRINA", Gemini
        // (o el normalizador heurÃ­stico) puede recuperar el nombre real. En una
        // bÃºsqueda escrita a mano no hay contexto extra y no se hace este paso.
        if (contextoOCR != null) {
            val porContexto = buscarPorCandidatos(normalizador.candidatos(contextoOCR))
            if (porContexto.isNotEmpty()) return conVentaLibre(porContexto)
        }

        val encontrados = api.buscar(texto = consulta)
        // El texto venÃ­a del OCR y puede tener ruido. Antes de rendirnos, probamos
        // las palabras candidatas como prefijo del nombre comercial.
        val medicamentos = if (encontrados.isNotEmpty()) {
            aMedicamentos(encontrados)
        } else {
            // Solo aquÃ­ (cuando $q fallÃ³) se permite el costo de Gemini.
            buscarPorCandidatos(normalizador.candidatos(contextoOCR ?: consulta))
        }
        return conVentaLibre(medicamentos)
    }

    /** Completa la insignia de venta libre sin lanzar peticiones por tarjeta. */
    private suspend fun conVentaLibre(medicamentos: List<Medicamento>): List<Medicamento> {
        val catalogo = catalogoVentaLibre() ?: return medicamentos
        return medicamentos.map { medicamento ->
            medicamento.copy(
                ventaLibre = medicamento.principiosActivos.firstOrNull().esVentaLibre(catalogo)
            )
        }
    }

    /** Prueba `upper(producto) like 'PALABRA%'` hasta encontrar resultados. */
    private suspend fun buscarPorCandidatos(candidatos: List<String>): List<Medicamento> {
        for (candidato in candidatos.take(MAX_CANDIDATOS)) {
            val where = "upper(producto) like '${candidato.replace("'", "''")}%'"
            val encontrados = aMedicamentos(api.buscarPorNombre(where = where))
            if (encontrados.isNotEmpty()) return encontrados
        }
        return emptyList()
    }

    override suspend fun cargarDetalle(expediente: String): Medicamento? {
        val id = expediente.trim()
        require(id.isNotEmpty() && id.all { it.isDigit() }) { "Expediente no vÃ¡lido: $expediente" }

        val base = api.detalle(id).firstOrNull()?.toMedicamentoBasico() ?: return null

        return coroutineScope {
            // El detalle va primero (de Ã©l salen el expediente y el principio activo),
            // pero precio y venta libre son independientes entre sÃ­: en paralelo.
            val precioDiferido = async { precioMaximo(id) }
            val ventaLibreDiferido = async { esVentaLibre(base.principiosActivos.firstOrNull()) }

            base.copy(
                precioMaximo = precioDiferido.await(),
                ventaLibre = ventaLibreDiferido.await()
            )
        }
    }

    private suspend fun precioMaximo(expediente: String): String? = try {
        // El expediente ya estÃ¡ validado como numÃ©rico; duplicar la comilla es defensa
        // en profundidad por si algÃºn dÃ­a cambia el formato del dataset.
        val where = "starts_with(cum, '${expediente.replace("'", "''")}-')"
        api.precio(where = where)
            .firstOrNull()?.precioMaximo
            ?.trim()?.takeUnless { it.isBlank() }?.let { "$ $it" }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null // Sin precio es un resultado vÃ¡lido, no un error.
    }

    private suspend fun esVentaLibre(principioActivo: String?): Boolean {
        val principio = normalizar(principioActivo) ?: return false
        val catalogo = catalogoVentaLibre() ?: return false
        return principio in catalogo
    }

    /** Devuelve null solo si el catÃ¡logo opcional no se pudo cargar. */
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
     * a "AcetaminofÃ©n" (venta libre). Normalizamos acentos, mayÃºsculas y espacios.
     */
    private fun normalizar(texto: String?): String? = normalizarTexto(texto)

    private fun String?.esVentaLibre(catalogo: Set<String>): Boolean =
        normalizar(this)?.let { it in catalogo } ?: false

    /**
     * Sustituye al `$group` de la API (que tarda ~4 s). Dos filas del mismo
     * expediente/products con distinto principio activo se agrupan en un solo
     * medicamento, conservando todos sus principios activos.
     */
    private fun aMedicamentos(dtos: List<MedicamentoDto>): List<Medicamento> {
        val agrupados = LinkedHashMap<String, Medicamento>()
        for (dto in dtos) {
            val basico = dto.toMedicamentoBasico()
            val clave = listOf(
                basico.expediente,
                normalizar(basico.nombre).orEmpty(),
                normalizar(basico.formaFarmaceutica).orEmpty()
            ).joinToString("|")
            val existente = agrupados[clave]
            agrupados[clave] = if (existente == null) {
                basico
            } else {
                val activos = (existente.principiosActivos + basico.principiosActivos)
                    .distinctBy { normalizar(it).orEmpty() }
                existente.copy(principiosActivos = activos)
            }
        }
        return agrupados.values.toList()
    }

    private fun MedicamentoDto.toMedicamentoBasico(): Medicamento {
        val nombre = producto?.trim().orEmpty().ifBlank { "Medicamento sin nombre" }
        return Medicamento(
            expediente = expediente.orEmpty(),
            nombre = nombre,
            laboratorio = titular?.trim().orEmpty().ifBlank { "No especificado" },
            formaFarmaceutica = formafarmaceutica?.trim().orEmpty().ifBlank { "No especificado" },
            vigente = estadoregistro?.trim().equals("Vigente", ignoreCase = true),
            // Se completa en buscar()/cargarDetalle() con el catÃ¡logo de venta libre.
            ventaLibre = false,
            paraQueSirve = descripcionatc?.trim().orEmpty().ifBlank { "No especificado" },
            principiosActivos = principioactivo?.let { listOf(it) } ?: emptyList(),
            registroInvima = registrosanitario?.trim().orEmpty().ifBlank { "No disponible" },
            precioMaximo = null // Solo se consulta en el Detalle.
        )
    }
}
