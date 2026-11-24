package com.example.remedia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado del escÃ¡ner, separado de CameraX y de ML Kit.
 *
 * El analizador de imÃ¡genes solo entrega texto; este ViewModel decide cuÃ¡l lÃ­nea
 * es el nombre del medicamento, guarda el OCR completo como contexto y permite
 * al usuario corregir el texto antes de buscar. AsÃ­ la lÃ³gica se prueba sin
 * cÃ¡mara y la pantalla no acumula estado propio.
 */
class EscanerViewModel : ViewModel() {

    private val _textoDetectado = MutableStateFlow("")
    val textoDetectado: StateFlow<String> = _textoDetectado.asStateFlow()

    private val _estado = MutableStateFlow<EstadoUi<TextoEscaneado>>(EstadoUi.Vacio)
    val estado: StateFlow<EstadoUi<TextoEscaneado>> = _estado.asStateFlow()

    /** Ãšltimas lÃ­neas Ãºtiles del OCR, sin recortar: sirven como contexto de respaldo. */
    private val lineasCrudas = ArrayDeque<String>()

    /**
     * El OCR va entregando resultados; guardamos el mejor sin confirmarlo todavÃ­a.
     * Se conserva el texto anterior cuando el nuevo candidato es peor: evita que
     * el nombre mostrado parpadee mientras la cÃ¡mara se enfoca.
     */
    fun onTextoReconocido(textos: List<String>) {
        val utiles = textos.map { it.trim() }.filter { it.isNotBlank() }
        if (utiles.isNotEmpty()) {
            lineasCrudas.addAll(utiles)
            while (lineasCrudas.size > MAX_LINEAS) lineasCrudas.removeFirst()
        }

        val candidato = TextoMedicamentoExtractor.extraer(textos, _textoDetectado.value)
        if (candidato != null && candidato != _textoDetectado.value) _textoDetectado.value = candidato
    }

    /** El usuario corrige el nombre leÃ­do antes de confirmar. */
    fun actualizarTexto(texto: String) {
        val limpio = texto.trim()
        if (limpio.isNotBlank()) _textoDetectado.value = limpio
    }

    /** El obturador confirma el texto que estaba en el visor, junto con su contexto. */
    fun capturar() {
        val texto = _textoDetectado.value
        _estado.value = if (texto.isBlank()) {
            EstadoUi.Vacio
        } else {
            val crudo = lineasCrudas.joinToString(" | ")
            EstadoUi.Exito(
                TextoEscaneado(
                    consulta = texto,
                    // Siempre viaja el OCR completo: con key configurada, el
                    // repositorio usa Gemini sobre este texto antes de buscar.
                    contexto = crudo.ifBlank { texto }
                )
            )
        }
    }

    fun onPermisoConcedido() {
        if (_estado.value !is EstadoUi.Exito) _estado.value = EstadoUi.Vacio
    }

    fun onPermisoDenegado() {
        if (_estado.value !is EstadoUi.Exito) {
            _estado.value = EstadoUi.Error(
                "Necesitamos permiso de cÃ¡mara para leer el nombre. ActÃ­valo en Ajustes o toca Reintentar."
            )
        }
    }

    fun onCamaraError(causa: String?) {
        val detalle = causa?.trim()?.takeUnless { it.isBlank() }?.let { " ($it)" }.orEmpty()
        _estado.value = EstadoUi.Error(
            "No se pudo abrir la cÃ¡mara.$detalle Cierra otra app que la estÃ© usando e intÃ©ntalo de nuevo."
        )
    }

    /** "Reintentar" y "Escribir el nombre" vuelven al visor limpio. */
    fun reiniciar() {
        _textoDetectado.value = ""
        lineasCrudas.clear()
        _estado.value = EstadoUi.Vacio
    }

    private companion object {
        const val MAX_LINEAS = 12
    }
}
