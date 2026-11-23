package com.example.remedia

import com.example.remedia.presentation.viewmodel.EscanerViewModel
import com.example.remedia.presentation.viewmodel.EstadoUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** El OCR se simula con listas de texto: no hace falta cámara ni ML Kit. */
class EscanerViewModelTest {

    private val viewModel = EscanerViewModel()

    @Test
    fun `descarta ruidos y confirma el nombre del medicamento`() {
        viewModel.onTextoReconocido(
            listOf("INVIMA 2019M-001", "  DOLEX   AVANZADO ", "LOTE A123", "500 mg")
        )

        assertEquals("DOLEX AVANZADO", viewModel.textoDetectado.value)

        viewModel.capturar()

        assertEquals(EstadoUi.Exito("DOLEX AVANZADO"), viewModel.estado.value)
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
}
