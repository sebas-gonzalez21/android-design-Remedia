package com.example.remedia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.remedia.presentation.model.EstadoUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * El visor de cámara todavía no está integrado (falta CameraX, ver EscanerScreen).
 * Por ahora "capturar" informa honestamente que esa función no está lista,
 * en vez de simular un resultado falso.
 */
class EscanerViewModel : ViewModel() {

    private val _estado = MutableStateFlow<EstadoUi<String>>(EstadoUi.Vacio)
    val estado: StateFlow<EstadoUi<String>> = _estado.asStateFlow()

    fun capturar() {
        _estado.value = EstadoUi.Error("El escáner con cámara aún no está implementado. Usa el buscador de texto.")
    }
}
