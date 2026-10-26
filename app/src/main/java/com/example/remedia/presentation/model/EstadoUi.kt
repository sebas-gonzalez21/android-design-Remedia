package com.example.remedia.presentation.model

/**
 * Los 4 estados visuales de cada pantalla.
 * TEMPORAL: cuando el compañero del ViewModel entregue su sealed class de estado,
 * se reemplaza este archivo (o se alinea el nombre) y las pantallas no cambian.
 */
sealed interface EstadoUi<out T> {
    data object Cargando : EstadoUi<Nothing>
    data class Exito<T>(val datos: T) : EstadoUi<T>
    data object Vacio : EstadoUi<Nothing>
    data class Error(val mensaje: String) : EstadoUi<Nothing>
}
