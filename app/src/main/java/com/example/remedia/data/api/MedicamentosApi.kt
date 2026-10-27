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

    @GET("resource/xzwx-qpja.json")
    suspend fun ventaLibre(
        @Query("\$q") principioActivo: String,
        @Query("\$limit") limit: Int = 1
    ): List<Map<String, Any>>

    companion object {
        const val SELECT_LISTA = "expediente,producto,titular,estadoregistro,formafarmaceutica"
    }
}
