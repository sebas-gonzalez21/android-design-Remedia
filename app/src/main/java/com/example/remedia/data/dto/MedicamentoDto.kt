package com.example.remedia.data.dto

/**
 * Dataset i7cb-raxc (CUM Vigentes, INVIMA) - ver README, Hito 1 y 4.
 * Nombres de campo verificados contra la API real (datos.gov.co), no inventados.
 * En /buscar y /equivalentes viene narrow (solo los campos de $select); en /detalle
 * viene completo, por eso todos son nulables.
 */
data class MedicamentoDto(
    val expediente: String?,
    val producto: String?,
    val titular: String?,
    val estadoregistro: String?,
    val formafarmaceutica: String?,
    val registrosanitario: String? = null,
    val viaadministracion: String? = null,
    val atc: String? = null,
    val descripcionatc: String? = null,
    val principioactivo: String? = null,
    val cantidad: String? = null,
    val unidadmedida: String? = null
)
