package com.example.remedia

import com.example.remedia.data.repository.EnriquecedorFichaGemini
import com.example.remedia.data.repository.EnriquecedorFichaVacio
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Gemini se simula: estas pruebas no hacen red ni necesitan API key. */
class EnriquecedorFichaGeminiTest {

    private val api = FakeGeminiApi()
    private val enriquecedor = EnriquecedorFichaGemini(
        api = api,
        modelo = "gemini-3.1-flash-lite",
        clave = "CLAVE-DE-PRUEBA"
    )

    @Test
    fun `extrae la explicacion y conserva solo urls https distintas`() = runTest {
        api.responder(
            """
            {"paraQueSirve":"  Alivia el dolor leve y la fiebre de forma temporal.  ",
             "imagenes":["https://ejemplo.com/caja.png","http://inseguro.com/caja.png",
                         "https://ejemplo.com/otra.jpg","https://ejemplo.com/caja.png"]}
            """.trimIndent()
        )

        val ficha = enriquecedor.enriquecer(medicamentoDePrueba())

        assertEquals("Alivia el dolor leve y la fiebre de forma temporal.", ficha.paraQueSirve)
        assertEquals(
            listOf("https://ejemplo.com/caja.png", "https://ejemplo.com/otra.jpg"),
            ficha.imagenes
        )
        assertEquals(1, api.llamadas)
        assertEquals("gemini-3.1-flash-lite", api.ultimoModelo)
        assertEquals("CLAVE-DE-PRUEBA", api.ultimaClave)
        assertEquals(300, api.ultimaPeticion?.generationConfig?.maxOutputTokens)
        assertTrue(
            api.ultimaPeticion?.contents?.first()?.parts?.first()?.text?.contains("DOLEX") == true
        )
    }

    @Test
    fun `sin key no llama a gemini`() = runTest {
        val sinKey = EnriquecedorFichaGemini(api = api, clave = "")

        val ficha = sinKey.enriquecer(medicamentoDePrueba())

        assertNull(ficha.paraQueSirve)
        assertTrue(ficha.imagenes.isEmpty())
        assertEquals(0, api.llamadas)
    }

    @Test
    fun `el enriquecedor vacio tampoco hace nada`() = runTest {
        val ficha = EnriquecedorFichaVacio.enriquecer(medicamentoDePrueba())

        assertNull(ficha.paraQueSirve)
        assertTrue(ficha.imagenes.isEmpty())
    }

    @Test
    fun `una respuesta que no es json se descarta sin romper la ficha`() = runTest {
        api.responder("lo siento, no puedo ayudar")

        val ficha = enriquecedor.enriquecer(medicamentoDePrueba())

        assertNull(ficha.paraQueSirve)
        assertTrue(ficha.imagenes.isEmpty())
    }

    @Test
    fun `explicaciones demasiado cortas se descartan pero las imagenes servidas`() = runTest {
        api.responder(
            """{"paraQueSirve":"Sirve.", "imagenes":["https://ejemplo.com/caja.png"]}"""
        )

        val ficha = enriquecedor.enriquecer(medicamentoDePrueba())

        assertNull(ficha.paraQueSirve)
        assertEquals(listOf("https://ejemplo.com/caja.png"), ficha.imagenes)
    }
}
