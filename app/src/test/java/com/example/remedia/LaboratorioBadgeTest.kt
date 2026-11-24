package com.example.remedia

import com.example.remedia.presentation.components.indiceColorLaboratorio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** La insignia de farmacéutica debe ser estable y distinguir laboratorios. */
class LaboratorioBadgeTest {

    @Test
    fun `el mismo laboratorio conserva su color`() {
        assertEquals(
            indiceColorLaboratorio("Laboratorios Bayer S.A."),
            indiceColorLaboratorio("  LABORATORIOS BAYER S.A.  ")
        )
    }

    @Test
    fun `distintos laboratorios reciben colores distintos`() {
        val nombres = listOf(
            "BAYER", "PFIZER", "NOVARTIS", "SANOFI", "ASTRAZENECA",
            "HALEON", "ABBOTT", "JOHNSON", "ROCHE", "MERCK"
        )
        val indices = nombres.map(::indiceColorLaboratorio).toSet()

        assertTrue("Se esperaba más de un color, se obtuvo $indices", indices.size > 1)
    }

    @Test
    fun `laboratorio no especificado usa el color neutro`() {
        assertEquals(-1, indiceColorLaboratorio("No especificado"))
        assertEquals(-1, indiceColorLaboratorio("   "))
    }
}
