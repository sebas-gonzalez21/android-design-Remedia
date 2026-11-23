package com.example.remedia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.remedia.data.local.RecientesDataSource
import com.example.remedia.data.repository.MedicamentosRepository

/**
 * Fábrica manual (el proyecto aún no usa Hilt/Koin): los ViewModel reciben el
 * repositorio por constructor, así se puede sustituir por un doble en las pruebas
 * sin tocar el código de producción.
 */
class MedicamentosViewModelFactory(
    private val repository: MedicamentosRepository,
    private val recientesSource: RecientesDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(BusquedaViewModel::class.java) ->
            BusquedaViewModel(repository, recientesSource) as T
        modelClass.isAssignableFrom(DetalleViewModel::class.java) -> DetalleViewModel(repository) as T
        else -> throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
