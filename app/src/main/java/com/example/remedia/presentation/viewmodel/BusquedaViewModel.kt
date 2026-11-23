package com.example.remedia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remedia.data.local.RecientesDataSource
import com.example.remedia.data.model.Medicamento
import com.example.remedia.data.repository.MedicamentosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de Inicio (recientes) y Resultados (búsqueda). Una sola instancia se
 * comparte entre esas dos pantallas (ver AppNavigation) para que el historial
 * de búsquedas recientes persista al navegar y entre ejecuciones de la app.
 */
class BusquedaViewModel(
    private val repository: MedicamentosRepository,
    private val recientesSource: RecientesDataSource
) : ViewModel() {

    private val historial = mutableListOf<String>()
    private val cache = ConsultaCache<EstadoUi<List<Medicamento>>>()
    private var buscarJob: Job? = null
    private var huboBusqueda = false

    private val _recientes = MutableStateFlow<EstadoUi<List<String>>>(EstadoUi.Vacio)
    val recientes: StateFlow<EstadoUi<List<String>>> = _recientes.asStateFlow()

    private val _resultados = MutableStateFlow<EstadoUi<List<Medicamento>>>(EstadoUi.Vacio)
    val resultados: StateFlow<EstadoUi<List<Medicamento>>> = _resultados.asStateFlow()

    init {
        viewModelScope.launch {
            val guardados = try {
                recientesSource.recientes.first()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList() // El historial es un extra: nunca rompe la app.
            }
            // Si el usuario ya buscó mientras se leía el DataStore, no pisamos su lista.
            if (!huboBusqueda) {
                historial.addAll(guardados.take(MAX_RECIENTES))
                _recientes.value = EstadoUi.Exito(historial.toList())
            }
        }
    }

    /**
     * @param forzar true desde "Reintentar": ignora la caché aunque el último
     * intento haya terminado en error.
     */
    fun buscar(texto: String, forzar: Boolean = false) {
        val consulta = texto.trim()
        if (consulta.isBlank()) return

        // Cancelar siempre, también en aciertos de caché: una búsqueda anterior
        // todavía en vuelo no debe pisar el estado que acabamos de restaurar.
        buscarJob?.cancel()

        if (!forzar) {
            val guardado = cache[consulta]
            if (guardado != null) {
                _resultados.value = guardado
                return
            }
        }

        _resultados.value = EstadoUi.Cargando
        buscarJob = viewModelScope.launch {
            val estado = try {
                val lista = repository.buscar(consulta)
                registrarReciente(consulta)
                if (lista.isEmpty()) EstadoUi.Vacio else EstadoUi.Exito(lista)
            } catch (e: CancellationException) {
                throw e // Nunca convertir una cancelación en Error.
            } catch (e: Exception) {
                EstadoUi.Error(e.message ?: "No se pudo conectar con el servidor.")
            }
            cache[consulta] = estado
            _resultados.value = estado
        }
    }

    private fun registrarReciente(texto: String) {
        huboBusqueda = true
        historial.remove(texto)
        historial.add(0, texto)
        while (historial.size > MAX_RECIENTES) historial.removeAt(historial.lastIndex)

        val lista = historial.toList()
        _recientes.value = EstadoUi.Exito(lista)
        viewModelScope.launch {
            try {
                recientesSource.guardar(lista)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Persistir es best-effort: la sesión sigue funcionando en memoria.
            }
        }
    }

    override fun onCleared() {
        buscarJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val MAX_RECIENTES = 10
    }
}
