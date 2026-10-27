package com.example.remedia.data.dto

import com.google.gson.annotations.SerializedName

/** Dataset nauz-qkjw (precio máximo regulado). Solo se consulta en el Detalle. */
data class PrecioDto(
    val cum: String?,
    @SerializedName("precio_maximo_de_venta_transaccion_final_comercial")
    val precioMaximo: String?
)
