package com.example.remedia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remedia.data.model.Medicamento
import com.example.remedia.data.repository.MedicamentosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Trae el detalle por expediente (dataset i7cb-raxc) y, dentro del mismo repositorio,
 * el precio regulado (nauz-qkjw) y la venta libre (xzwx-qpja) — ver README Hito 1/4.
 */
class DetalleViewModel(
    private val repository: MedicamentosRepository
) : ViewModel() {

    private val cache = ConsultaCache<EstadoUi<Medicamento>>()
    private var cargarJob: Job? = null

    private val _estado = MutableStateFlow<EstadoUi<Medicamento>>(EstadoUi.Vacio)
    val estado: StateFlow<EstadoUi<Medicamento>> = _estado.asStateFlow()

    fun cargar(expediente: String, forzar: Boolean = false) {
        val id = expediente.trim()
        if (id.isBlank()) return

        cargarJob?.cancel()
        if (!forzar) {
            val guardado = cache[id]
            if (guardado != null) {
                _estado.value = guardado
                return
            }
        }

        _estado.value = EstadoUi.Cargando
        cargarJob = viewModelScope.launch {
            val estado = try {
                repository.cargarDetalle(id)
                    ?.let { EstadoUi.Exito(it) }
                    ?: EstadoUi.Error("No encontramos este medicamento. Vuelve a buscarlo.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                EstadoUi.Error(e.message ?: "No se pudo conectar con el servidor.")
            }
            cache[id] = estado
            _estado.value = estado
        }
    }

    override fun onCleared() {
        cargarJob?.cancel()
        super.onCleared()
    }
}
