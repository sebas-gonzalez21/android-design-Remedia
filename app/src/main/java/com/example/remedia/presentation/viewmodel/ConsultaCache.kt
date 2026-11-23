package com.example.remedia.presentation.viewmodel

/**
 * Caché LRU pequeña y acotada para estados de UI ya calculados.
 *
 * Evita repetir peticiones al volver del Detalle sin dejar crecer la memoria
 * sin límite. Se usa desde el hilo principal (viewModelScope).
 */
internal class ConsultaCache<T>(private val limite: Int = 20) {

    private val datos = LinkedHashMap<String, T>()

    operator fun get(clave: String): T? = datos[clave]

    operator fun set(clave: String, valor: T) {
        datos.remove(clave)
        datos[clave] = valor
        while (datos.size > limite) {
            datos.remove(datos.keys.first())
        }
    }
}
