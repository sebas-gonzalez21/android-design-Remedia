package com.example.remedia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remedia.data.repository.MedicamentosRepository
import com.example.remedia.presentation.model.EstadoUi
import com.example.remedia.presentation.model.MedicamentoUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de Inicio (recientes) y Resultados (búsqueda). Una sola instancia se
 * comparte entre esas dos pantallas (ver RemediaNavHost) para que el historial
 * de búsquedas recientes persista al navegar.
 */
class BusquedaViewModel(
    private val repository: MedicamentosRepository = MedicamentosRepository
) : ViewModel() {

    private val historial = mutableListOf<String>()

    private val _recientes = MutableStateFlow<EstadoUi<List<String>>>(EstadoUi.Vacio)
    val recientes: StateFlow<EstadoUi<List<String>>> = _recientes.asStateFlow()

    private val _resultados = MutableStateFlow<EstadoUi<List<MedicamentoUi>>>(EstadoUi.Cargando)
    val resultados: StateFlow<EstadoUi<List<MedicamentoUi>>> = _resultados.asStateFlow()

    fun buscar(texto: String) {
        if (texto.isBlank()) return
        registrarReciente(texto)
        _resultados.value = EstadoUi.Cargando
        viewModelScope.launch {
            _resultados.value = try {
                val lista = repository.buscar(texto)
                if (lista.isEmpty()) EstadoUi.Vacio else EstadoUi.Exito(lista)
            } catch (e: Exception) {
                EstadoUi.Error(e.message ?: "No se pudo conectar con el servidor.")
            }
        }
    }

    private fun registrarReciente(texto: String) {
        historial.remove(texto)
        historial.add(0, texto)
        while (historial.size > 10) historial.removeAt(historial.lastIndex)
        _recientes.value = EstadoUi.Exito(historial.toList())
    }
}
