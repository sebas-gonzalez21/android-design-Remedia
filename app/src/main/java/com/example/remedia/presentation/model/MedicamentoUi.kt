package com.example.remedia.presentation.model

/**
 * Lo que la Vista necesita pintar de un medicamento.
 * TEMPORAL: cuando exista data/model/Medicamento (entidad del ER), el ViewModel
 * entregará los datos y esta clase se reemplaza o se mapea. Solo `val`, con id único.
 */
data class MedicamentoUi(
    val expediente: String,
    val nombre: String,
    val laboratorio: String,
    val formaFarmaceutica: String,
    val vigente: Boolean,
    val ventaLibre: Boolean,
    val paraQueSirve: String,
    val principiosActivos: List<String>,
    val registroInvima: String,
    val precioMaximo: String?
)
