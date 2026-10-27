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
 * Trae el detalle por expediente (dataset i7cb-raxc) y, dentro del mismo repositorio,
 * el precio regulado (nauz-qkjw) y la venta libre (xzwx-qpja) — ver README Hito 1/4.
 */
class DetalleViewModel(
    private val repository: MedicamentosRepository = MedicamentosRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoUi<MedicamentoUi>>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi<MedicamentoUi>> = _estado.asStateFlow()

    fun cargar(expediente: String) {
        _estado.value = EstadoUi.Cargando
        viewModelScope.launch {
            _estado.value = try {
                repository.cargarDetalle(expediente)
                    ?.let { EstadoUi.Exito(it) }
                    ?: EstadoUi.Error("No encontramos este medicamento. Vuelve a buscarlo.")
            } catch (e: Exception) {
                EstadoUi.Error(e.message ?: "No se pudo conectar con el servidor.")
            }
        }
    }
}
