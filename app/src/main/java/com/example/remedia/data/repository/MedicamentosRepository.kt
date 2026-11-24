package com.example.remedia.data.repository

import com.example.remedia.data.model.Medicamento

/**
 * Contrato de acceso a los medicamentos: QUÉ se puede pedir, sin decir CÓMO se obtiene.
 *
 * Los ViewModels dependen de esta interfaz, no de Retrofit. Así se puede cambiar la
 * fuente de datos (API, base de datos Room, datos falsos para pruebas) sin tocar
 * ninguna pantalla ni ViewModel.
 */
interface MedicamentosRepository {

    /**
     * Busca medicamentos por nombre o principio activo. Lista vacía = sin resultados.
     * [contexto] es el OCR completo cuando la consulta viene del escáner; permite
     * recuperar el nombre aunque la extracción haya elegido una palabra equivocada.
     */
    suspend fun buscar(texto: String, contexto: String = texto): List<Medicamento>

    /** Detalle completo de un expediente, o null si no existe. */
    suspend fun cargarDetalle(expediente: String): Medicamento?
}