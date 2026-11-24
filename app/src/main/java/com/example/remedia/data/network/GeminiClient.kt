package com.example.remedia.data.network

import com.example.remedia.BuildConfig
import com.example.remedia.data.api.GeminiApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GeminiClient {

    /** True solo cuando el usuario puso una key en local.properties. */
    val habilitado: Boolean get() = BuildConfig.GEMINI_API_KEY.isNotBlank()

    // Sin logging: el interceptor de Retrofit imprimirÃ­a la URL con ?key=...
    // Timeouts cortos a propÃ³sito: Gemini se usa solo como respaldo y no debe
    // convertir una bÃºsqueda fallida en una espera larga.
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()

    private val urlBase = BuildConfig.GEMINI_BASE_URL
        .takeIf { it.startsWith("http://") || it.startsWith("https://") }
        ?.let { url -> if (url.endsWith("/")) url else "$url/" }
        ?: "https://generativelanguage.googleapis.com/"

    val api: GeminiApi = Retrofit.Builder()
        .baseUrl(urlBase)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeminiApi::class.java)
}
