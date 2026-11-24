package com.example.remedia

import com.example.remedia.data.model.Medicamento
import com.example.remedia.data.repository.SimilitudMedicamento
import org.junit.Assert.assertEquals
import org.junit.Test

class SimilitudMedicamentoTest {

    @Test
    fun `el resultado mas parecido al texto escaneado va primero`() {
        val cardio = medicamento(
            nombre = "CARDIOASPIRINA® 81MG TABLETAS CON CUBIERTA ENTERICA",
            forma = "TABLETAS CON CUBIERTA ENTERICA",
            activo = "ASPIRINA USP"
        )
        val efervescente = medicamento(
            nombre = "ASPIRINA® 500MG TABLETAS EFERVESCENTES",
            forma = "TABLETAS EFERVESCENTES",
            activo = "ACIDO ACETIL SALICILICO"
        )
        val ultra = medicamento(
            nombre = "ASPIRINA® ULTRA",
            forma = "TABLETA",
            activo = "ACIDO ACETILSALICILICO"
        )
        val buscado = medicamento(
            nombre = "ASPIRINA® 100 MG TABLETAS",
            forma = "TABLETAS",
            activo = "ACIDO ACETILSALICILICO"
        )

        val resultado = SimilitudMedicamento.ordenar(
            medicamentos = listOf(cardio, efervescente, ultra, buscado),
            pista = "Ince I Medcanerts I ta re Alvio Del Dolor / Dolor General Aspirina 100mg Tableta",
            nombreBuscado = "ASPIRINA"
        )

        assertEquals(
            listOf(
                "ASPIRINA® 100 MG TABLETAS",
                "ASPIRINA® 500MG TABLETAS EFERVESCENTES",
                "ASPIRINA® ULTRA",
                "CARDIOASPIRINA® 81MG TABLETAS CON CUBIERTA ENTERICA"
            ),
            resultado.map { it.nombre }
        )
    }

    @Test
    fun `la dosis del ocr pesa aunque no haya un nombre elegido`() {
        val medicamentos = listOf(
            medicamento(
                nombre = "CARDIOASPIRINA® 81MG TABLETAS",
                forma = "TABLETAS",
                activo = "ASPIRINA USP"
            ),
            medicamento(
                nombre = "ASPIRINA® 500MG TABLETAS",
                forma = "TABLETAS",
                activo = "ACIDO ACETIL SALICILICO"
            ),
            medicamento(
                nombre = "ASPIRINA® 100 MG TABLETAS",
                forma = "TABLETAS",
                activo = "ACIDO ACETILSALICILICO"
            )
        )

        val resultado = SimilitudMedicamento.ordenar(
            medicamentos = medicamentos,
            pista = "Aspirina 100mg Tableta"
        )

        assertEquals("ASPIRINA® 100 MG TABLETAS", resultado.first().nombre)
    }

    private fun medicamento(
        nombre: String,
        forma: String,
        activo: String
    ): Medicamento = Medicamento(
        expediente = nombre,
        nombre = nombre,
        laboratorio = "LABORATORIO",
        formaFarmaceutica = forma,
        vigente = true,
        ventaLibre = false,
        paraQueSirve = "PARACETAMOL",
        principiosActivos = listOf(activo),
        registroInvima = "INVIMA",
        precioMaximo = null
    )
}
