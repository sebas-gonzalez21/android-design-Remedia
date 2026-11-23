package com.example.remedia.presentation.viewmodel

/**
 * Los 4 estados visuales de cada pantalla: Cargando, Exito, Vacio y Error.
 * Lo usan todos los ViewModels y lo pinta EstadoContenido.
 */
sealed interface EstadoUi<out T> {
    data object Cargando : EstadoUi<Nothing>
    data class Exito<T>(val datos: T) : EstadoUi<T>
    data object Vacio : EstadoUi<Nothing>
    data class Error(val mensaje: String) : EstadoUi<Nothing>
}