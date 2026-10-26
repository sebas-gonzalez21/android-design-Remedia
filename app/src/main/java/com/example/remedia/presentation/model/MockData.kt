package com.example.remedia.presentation.model

/**
 * DATOS DE EJEMPLO inventados solo para que las pantallas se vean bien.
 * No son datos reales de la API. Se borra este archivo cuando lleguen los ViewModels.
 */
object MockData {

    val medicamentos = listOf(
        MedicamentoUi(
            expediente = "0001",
            nombre = "Medicamento Ejemplo A 500 mg",
            laboratorio = "Laboratorio Ejemplo S.A.",
            formaFarmaceutica = "Tableta",
            vigente = true,
            ventaLibre = true,
            paraQueSirve = "Grupo ATC de ejemplo (analgésicos y antipiréticos)",
            principiosActivos = listOf("Principio Activo X 500 mg"),
            registroInvima = "INVIMA 0000-EJ-01",
            precioMaximo = "$ 12.500"
        ),
        MedicamentoUi(
            expediente = "0002",
            nombre = "Medicamento Ejemplo B 20 mg",
            laboratorio = "Farmacéutica Muestra Ltda.",
            formaFarmaceutica = "Cápsula",
            vigente = true,
            ventaLibre = false,
            paraQueSirve = "Grupo ATC de ejemplo (sistema digestivo)",
            principiosActivos = listOf("Principio Activo Y 20 mg", "Principio Activo Z 10 mg"),
            registroInvima = "INVIMA 0000-EJ-02",
            precioMaximo = "$ 34.900"
        ),
        MedicamentoUi(
            expediente = "0003",
            nombre = "Medicamento Ejemplo C jarabe",
            laboratorio = "Laboratorio Ejemplo S.A.",
            formaFarmaceutica = "Jarabe",
            vigente = false,
            ventaLibre = true,
            paraQueSirve = "Grupo ATC de ejemplo (sistema respiratorio)",
            principiosActivos = listOf("Principio Activo W 5 mg/mL"),
            registroInvima = "INVIMA 0000-EJ-03",
            precioMaximo = null
        )
    )

    val recientes = listOf("Medicamento Ejemplo A", "Medicamento Ejemplo B")

    fun buscarPorExpediente(expediente: String): EstadoUi<MedicamentoUi> =
        medicamentos.firstOrNull { it.expediente == expediente }
            ?.let { EstadoUi.Exito(it) }
            ?: EstadoUi.Vacio

    /**
     * Para probar los 4 estados desde el buscador:
     * escribe "cargando", "vacio" o "error"; cualquier otro texto devuelve la lista.
     */
    fun resultadosPara(consulta: String): EstadoUi<List<MedicamentoUi>> =
        when (consulta.trim().lowercase()) {
            "cargando" -> EstadoUi.Cargando
            "vacio" -> EstadoUi.Vacio
            "error" -> EstadoUi.Error("No se pudo conectar. Revisa tu internet.")
            else -> EstadoUi.Exito(medicamentos)
        }
}
