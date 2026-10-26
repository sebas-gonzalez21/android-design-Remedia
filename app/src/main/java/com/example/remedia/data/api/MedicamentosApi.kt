package com.example.remedia.data.api

import com.example.remedia.data.dto.MedicamentoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MedicamentosApi {
    @GET("resource/ec4u-mzse.json")
    suspend fun buscar(
        @Query("\$q") texto: String? = null,
        @Query("\$limit") limit: Int = 20,
        @Query("\$offset") offset: Int = 0
    ): List<MedicamentoDto>

    @GET("resource/ec4u-mzse.json")
    suspend fun porPrincipioActivo(
        @Query("principio_activo") principioActivo: String,
        @Query("\$limit") limit: Int = 50
    ): List<MedicamentoDto>
}
