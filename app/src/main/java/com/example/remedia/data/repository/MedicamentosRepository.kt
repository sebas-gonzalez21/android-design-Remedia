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

    /** Busca medicamentos por nombre o principio activo. Lista vacía = sin resultados. */
    suspend fun buscar(texto: String): List<Medicamento>

    /** Detalle completo de un expediente, o null si no existe. */
    suspend fun cargarDetalle(expediente: String): Medicamento?
}