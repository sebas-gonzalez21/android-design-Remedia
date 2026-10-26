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

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .addHeader(
                        "Authorization",
                        Credentials.basic(BuildConfig.SOCRATA_KEY_ID, BuildConfig.SOCRATA_KEY_SECRET)
                    )
                    .build()
            )
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
