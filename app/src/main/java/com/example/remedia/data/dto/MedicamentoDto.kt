package com.example.remedia.data.dto

import com.google.gson.annotations.SerializedName

data class MedicamentoDto(
    @SerializedName("principio_activo") val principioActivo: String?,
    @SerializedName("nombre_comercial") val nombreComercial: String?,
    @SerializedName("concentracion") val concentracion: String?,
    @SerializedName("unidad_de_dispensacion") val unidadDispensacion: String?,
    @SerializedName("unidad_base") val unidadBase: String?,
    @SerializedName("fabricante") val fabricante: String?,
    @SerializedName("precio_por_tableta") val precioPorTableta: String?,
    @SerializedName("factoresprecio") val factorPrecio: String?,
    @SerializedName("numerofactor") val numeroFactor: String?
)
