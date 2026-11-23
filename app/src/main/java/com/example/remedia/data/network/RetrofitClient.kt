package com.example.remedia.data.network

import com.example.remedia.BuildConfig
import com.example.remedia.data.api.MedicamentosApi
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://www.datos.gov.co/"

    // La key de Socrata es OPCIONAL: sin ella la API funciona, solo con menos
    // cupo de peticiones. Si las credenciales están vacías NO se envía el header,
    // porque un "Authorization: Basic" vacío puede hacer que el servidor rechace la petición.
    private val tieneCredenciales: Boolean =
        BuildConfig.SOCRATA_KEY_ID.isNotBlank() && BuildConfig.SOCRATA_KEY_SECRET.isNotBlank()

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val peticion = chain.request()
            val peticionFinal = if (tieneCredenciales) {
                peticion.newBuilder()
                    .header(
                        "Authorization",
                        Credentials.basic(BuildConfig.SOCRATA_KEY_ID, BuildConfig.SOCRATA_KEY_SECRET)
                    )
                    .build()
            } else {
                peticion
            }
            chain.proceed(peticionFinal)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val api: MedicamentosApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MedicamentosApi::class.java)
}