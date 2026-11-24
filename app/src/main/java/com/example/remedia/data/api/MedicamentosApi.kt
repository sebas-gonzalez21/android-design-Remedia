package com.example.remedia.data.api

import com.example.remedia.data.dto.MedicamentoDto
import com.example.remedia.data.dto.PrecioDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Endpoints reales segun el README (Hito 1 y 4), verificados contra la API
 * (datos.gov.co) antes de implementarlos.
 */
interface MedicamentosApi {

    @GET("resource/i7cb-raxc.json")
    suspend fun buscar(
        @Query("\$q") texto: String,
        @Query("\$select") select: String = SELECT_LISTA,
        @Query("\$group") group: String = SELECT_LISTA,
        @Query("\$limit") limit: Int = 50
    ): List<MedicamentoDto>

    /**
     * BÃºsqueda estricta por el principio del nombre comercial. Se usa como
     * respaldo cuando el texto del OCR tiene ruido: `$q` hace bÃºsqueda de texto
     * completo y puede devolver cosas raras, mientras que este prefijo obliga a
     * que el producto empiece por la palabra candidata.
     */
    @GET("resource/i7cb-raxc.json")
    suspend fun buscarPorNombre(
        @Query("\$where") where: String,
        @Query("\$select") select: String = SELECT_LISTA,
        @Query("\$group") group: String = SELECT_LISTA,
        @Query("\$limit") limit: Int = 50
    ): List<MedicamentoDto>

    @GET("resource/i7cb-raxc.json")
    suspend fun detalle(
        @Query("expediente") expediente: String,
        @Query("\$limit") limit: Int = 1
    ): List<MedicamentoDto>

    @GET("resource/nauz-qkjw.json")
    suspend fun precio(
        @Query("\$where") where: String,
        @Query("\$limit") limit: Int = 1
    ): List<PrecioDto>

    /**
     * Descarga una sola vez el catÃ¡logo de venta libre (~1000 filas) para resolver
     * los filtros de la lista y la insignia del detalle sin una peticiÃ³n por card.
     */
    @GET("resource/xzwx-qpja.json")
    suspend fun ventaLibrePrincipios(
        @Query("\$select") select: String = SELECT_VENTA_LIBRE,
        @Query("\$limit") limit: Int = LIMITE_VENTA_LIBRE
    ): List<Map<String, Any>>

    companion object {
        /**
         * Campos necesarios para pintar la tarjeta de resultados. `descripcionatc` y
         * `principioactivo` se incluyen para que "Â¿Para quÃ© sirve?" y los principios
         * activos no aparezcan como "No especificado" en la lista.
         */
        const val SELECT_LISTA =
            "expediente,producto,titular,estadoregistro,formafarmaceutica,descripcionatc,principioactivo"

        const val SELECT_VENTA_LIBRE = "principioactivo"
        const val LIMITE_VENTA_LIBRE = 5000
    }
}
