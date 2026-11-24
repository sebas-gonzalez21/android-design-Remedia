package com.example.remedia.data.api

import com.example.remedia.data.dto.GeminiRequest
import com.example.remedia.data.dto.GeminiRespuesta
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoint REST de Gemini usado solo para refinar consultas del OCR. */
interface GeminiApi {

    @POST("v1beta/models/{modelo}:generateContent")
    suspend fun generar(
        @Path("modelo") modelo: String,
        @Query("key") clave: String,
        @Body cuerpo: GeminiRequest
    ): GeminiRespuesta
}
