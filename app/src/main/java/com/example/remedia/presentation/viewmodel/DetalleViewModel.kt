package com.example.remedia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.remedia.data.repository.MedicamentosRepository
import com.example.remedia.presentation.model.EstadoUi
import com.example.remedia.presentation.model.MedicamentoUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * La API no tiene endpoint por id único, así que el detalle se resuelve contra
 * la última búsqueda cacheada en el repositorio (ver MedicamentosRepository).
 * Si se llega aquí sin haber buscado antes (ej. deep link), se pide reintentar.
 */
class DetalleViewModel(
    private val repository: MedicamentosRepository = MedicamentosRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoUi<MedicamentoUi>>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi<MedicamentoUi>> = _estado.asStateFlow()

    fun cargar(expediente: String) {
        _estado.value = repository.obtenerDeCache(expediente)
            ?.let { EstadoUi.Exito(it) }
            ?: EstadoUi.Error("No encontramos este medicamento. Vuelve a buscarlo.")
    }
}
