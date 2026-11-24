package com.example.remedia

import com.example.remedia.presentation.viewmodel.EscanerViewModel
import com.example.remedia.presentation.viewmodel.EstadoUi
import com.example.remedia.presentation.viewmodel.TextoEscaneado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** El OCR se simula con listas de texto: no hace falta cÃ¡mara ni ML Kit. */
class EscanerViewModelTest {

    private val viewModel = EscanerViewModel()

    @Test
    fun `descarta ruidos y confirma el nombre del medicamento`() {
        viewModel.onTextoReconocido(
            listOf("INVIMA 2019M-001", "  DOLEX   AVANZADO ", "LOTE A123", "500 mg")
        )

        assertEquals("DOLEX AVANZADO", viewModel.textoDetectado.value)

        viewModel.capturar()

        val exito = viewModel.estado.value as EstadoUi.Exito<TextoEscaneado>
        assertEquals("DOLEX AVANZADO", exito.datos.consulta)
        // Confianza alta: no hace falta mandar el OCR completo a la bÃºsqueda.
        assertEquals("DOLEX AVANZADO", exito.datos.contexto)
    }

    @Test
    fun `un bloque con varias lineas separa el nombre del registro`() {
        viewModel.onTextoReconocido(
            listOf("INVIMA 2024\nDOLEX AVANZADO\nParacetamol 500 mg\nVENCE 2027")
        )

        assertEquals("DOLEX AVANZADO", viewModel.textoDetectado.value)
    }

    @Test
    fun `sin texto util el obturador no inventa un resultado`() {
        viewModel.onTextoReconocido(listOf("INVIMA", "LOTE 123", "  ", "123 456"))

        assertEquals("", viewModel.textoDetectado.value)

        viewModel.capturar()

        assertEquals(EstadoUi.Vacio, viewModel.estado.value)
    }

    @Test
    fun `texto ruidoso con separadores elige el principio activo`() {
        viewModel.onTextoReconocido(
            listOf("Ince I Medcanerts I ta re Alvio Del Dolor / Dolor General Aspirina 100mg Tableta")
        )

        assertEquals("ASPIRINA", viewModel.textoDetectado.value)
    }

    @Test
    fun `la dosis y la forma farmaceutica no se incluyen en el nombre`() {
        viewModel.onTextoReconocido(listOf("ASPIRINA 100 MG TABLETAS"))

        assertEquals("ASPIRINA", viewModel.textoDetectado.value)
    }

    @Test
    fun `un frame peor no reemplaza al nombre ya detectado`() {
        viewModel.onTextoReconocido(listOf("DOLEX AVANZADO"))
        viewModel.onTextoReconocido(listOf("INVIMA 2024", "VENCE 2027", "LOTE A123"))

        assertEquals("DOLEX AVANZADO", viewModel.textoDetectado.value)
    }

    @Test
    fun `permiso denegado muestra error y al concederse vuelve al visor`() {
        viewModel.onPermisoDenegado()

        val error = viewModel.estado.value as EstadoUi.Error
        assertTrue(error.mensaje.contains("permiso", ignoreCase = true))

        viewModel.onPermisoConcedido()

        assertEquals(EstadoUi.Vacio, viewModel.estado.value)
    }

    @Test
    fun `reiniciar limpia el texto detectado y el estado confirmado`() {
        viewModel.onTextoReconocido(listOf("DOLEX"))
        viewModel.capturar()

        viewModel.reiniciar()

        assertEquals("", viewModel.textoDetectado.value)
        assertEquals(EstadoUi.Vacio, viewModel.estado.value)
    }

    @Test
    fun `un nombre detectado con confianza no arrastra el ruido del ocr`() {
        viewModel.onTextoReconocido(
            listOf("Ince I Medcanerts I ta re Alvio Del Dolor / Dolor General Aspirina 100mg Tableta")
        )
        viewModel.capturar()

        val exito = viewModel.estado.value as EstadoUi.Exito<TextoEscaneado>

        assertEquals("ASPIRINA", exito.datos.consulta)
        assertEquals("ASPIRINA", exito.datos.contexto)
    }

    @Test
    fun `el nombre corregido se usa pero conserva el ocr original`() {
        viewModel.onTextoReconocido(listOf("INCE", "MEDCANERTS", "ALVIO DEL DOLOR"))
        viewModel.actualizarTexto("Aspirina 100 mg")
        viewModel.capturar()

        val exito = viewModel.estado.value as EstadoUi.Exito<TextoEscaneado>

        assertEquals("Aspirina 100 mg", exito.datos.consulta)
        assertTrue(exito.datos.contexto.contains("MEDCANERTS"))
    }
}
