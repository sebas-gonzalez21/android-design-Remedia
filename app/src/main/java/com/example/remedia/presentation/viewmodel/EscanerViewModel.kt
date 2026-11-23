package com.example.remedia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado del escáner, separado de CameraX y de ML Kit.
 *
 * El analizador de imágenes solo entrega texto; este ViewModel decide cuál línea
 * es el nombre del medicamento y cuándo el usuario lo confirma con el obturador.
 * Así la lógica se prueba sin cámara y la pantalla no acumula estado propio.
 */
class EscanerViewModel : ViewModel() {

    private val _textoDetectado = MutableStateFlow("")
    val textoDetectado: StateFlow<String> = _textoDetectado.asStateFlow()

    private val _estado = MutableStateFlow<EstadoUi<String>>(EstadoUi.Vacio)
    val estado: StateFlow<EstadoUi<String>> = _estado.asStateFlow()

    /** EL OCR va entregando resultados; guardamos el mejor sin confirmarlo todavía. */
    fun onTextoReconocido(textos: List<String>) {
        val candidato = TextoMedicamentoExtractor.extraer(textos) ?: ""
        if (candidato != _textoDetectado.value) _textoDetectado.value = candidato
    }

    /** El obturador confirma el texto que estaba en el visor. */
    fun capturar() {
        val texto = _textoDetectado.value
        _estado.value = if (texto.isBlank()) {
            EstadoUi.Vacio
        } else {
            EstadoUi.Exito(texto)
        }
    }

    fun onPermisoConcedido() {
        if (_estado.value !is EstadoUi.Exito) _estado.value = EstadoUi.Vacio
    }

    fun onPermisoDenegado() {
        if (_estado.value !is EstadoUi.Exito) {
            _estado.value = EstadoUi.Error(
                "Necesitamos permiso de cámara para leer el nombre. Actívalo en Ajustes o toca Reintentar."
            )
        }
    }

    fun onCamaraError(causa: String?) {
        val detalle = causa?.trim()?.takeUnless { it.isBlank() }?.let { " ($it)" }.orEmpty()
        _estado.value = EstadoUi.Error(
            "No se pudo abrir la cámara.$detalle Cierra otra app que la esté usando e inténtalo de nuevo."
        )
    }

    /** "Reintentar" y "Escribir el nombre" vuelven al visor limpio. */
    fun reiniciar() {
        _textoDetectado.value = ""
        _estado.value = EstadoUi.Vacio
    }
}
