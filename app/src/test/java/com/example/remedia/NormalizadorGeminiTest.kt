package com.example.remedia

import com.example.remedia.data.repository.NormalizadorGemini
import com.example.remedia.data.repository.candidatosDeBusqueda
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Gemini se simula: las pruebas no hacen red ni necesitan API key. */
class NormalizadorGeminiTest {

    private val api = FakeGeminiApi()
    private val normalizador = NormalizadorGemini(
        api = api,
        modelo = "gemini-2.0-flash",
        clave = "CLAVE-DE-PRUEBA"
    )

    @Test
    fun `convierte la respuesta json en candidatos limpios`() = runTest {
        api.responder("[\"aspirina 100 mg\", \"dolex avanzado\", \"ab\"]")

        val candidatos = normalizador.candidatos("texto del ocr")

        assertEquals(listOf("ASPIRINA", "DOLEX AVANZADO"), candidatos)
        assertEquals("gemini-2.0-flash", api.ultimoModelo)
        assertEquals("CLAVE-DE-PRUEBA", api.ultimaClave)
        assertEquals(1, api.llamadas)
        assertEquals(true, api.ultimaPeticion?.contents?.first()?.parts?.first()?.text?.contains("OCR"))
    }

    @Test
    fun `si gemini no responde usa el normalizador heuristico`() = runTest {
        val texto = "Ince I Medcanerts I ta re Alvio Del Dolor / Dolor General Aspirina 100mg Tableta"

        val candidatos = normalizador.candidatos(texto)

        assertEquals(candidatosDeBusqueda(texto), candidatos)
    }

    @Test
    fun `sin key no llama a gemini`() = runTest {
        val sinKey = NormalizadorGemini(api = api, clave = "")
        val texto = "Aspirina 100 mg"

        val candidatos = sinKey.candidatos(texto)

        assertEquals(candidatosDeBusqueda(texto), candidatos)
        assertEquals(0, api.llamadas)
    }
}
