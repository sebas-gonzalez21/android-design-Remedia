package com.example.remedia

import com.example.remedia.data.api.GeminiApi
import com.example.remedia.data.dto.GeminiCandidato
import com.example.remedia.data.dto.GeminiContenido
import com.example.remedia.data.dto.GeminiParte
import com.example.remedia.data.dto.GeminiRequest
import com.example.remedia.data.dto.GeminiRespuesta
import com.example.remedia.data.dto.MedicamentoDto
import com.example.remedia.data.dto.PrecioDto
import com.example.remedia.data.local.RecientesDataSource

import com.example.remedia.data.model.Medicamento
import com.example.remedia.data.repository.MedicamentosRepository
import com.example.remedia.data.api.MedicamentosApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal fun medicamentoDePrueba(
    expediente: String = "123",
    nombre: String = "DOLEX",
    ventaLibre: Boolean = false
) = Medicamento(
    expediente = expediente,
    nombre = nombre,
    laboratorio = "HALEON",
    formaFarmaceutica = "TABLETA",
    vigente = true,
    ventaLibre = ventaLibre,
    paraQueSirve = "PARACETAMOL",
    principiosActivos = listOf("ACETAMINOFEN"),
    registroInvima = "INVIMA 2019M-0014206-R1",
    precioMaximo = null
)

internal fun dtoDePrueba(
    expediente: String = "123",
    producto: String = "DOLEX",
    descripcionatc: String? = "PARACETAMOL",
    principioactivo: String? = "ACETAMINOFEN"
) = MedicamentoDto(
    expediente = expediente,
    producto = producto,
    titular = "HALEON",
    estadoregistro = "Vigente",
    formafarmaceutica = "TABLETA",
    descripcionatc = descripcionatc,
    principioactivo = principioactivo
)

internal class FakeMedicamentosRepository : MedicamentosRepository {
    var buscarHandler: suspend (String) -> List<Medicamento> = { emptyList() }
    var detalleHandler: suspend (String) -> Medicamento? = { null }
    val consultas = mutableListOf<String>()
    val expedientes = mutableListOf<String>()

    override suspend fun buscar(texto: String): List<Medicamento> {
        consultas += texto
        return buscarHandler(texto)
    }

    override suspend fun cargarDetalle(expediente: String): Medicamento? {
        expedientes += expediente
        return detalleHandler(expediente)
    }
}

internal class FakeRecientesDataSource : RecientesDataSource {
    private val estado = MutableStateFlow<List<String>>(emptyList())
    override val recientes: Flow<List<String>> = estado
    var guardados: List<String> = emptyList()
        private set

    fun sembrar(lista: List<String>) {
        estado.value = lista
    }

    override suspend fun guardar(recientes: List<String>) {
        guardados = recientes
        estado.value = recientes
    }
}

internal class FakeGeminiApi : GeminiApi {
    var respuesta: GeminiRespuesta = GeminiRespuesta()
    var ultimoModelo: String? = null
    var ultimaClave: String? = null
    var ultimaPeticion: GeminiRequest? = null
    var llamadas = 0

    override suspend fun generar(
        modelo: String,
        clave: String,
        cuerpo: GeminiRequest
    ): GeminiRespuesta {
        llamadas++
        ultimoModelo = modelo
        ultimaClave = clave
        ultimaPeticion = cuerpo
        return respuesta
    }

    fun responder(texto: String) {
        respuesta = GeminiRespuesta(
            candidates = listOf(
                GeminiCandidato(
                    content = GeminiContenido(parts = listOf(GeminiParte(texto)))
                )
            )
        )
    }
}

internal class FakeMedicamentosApi : MedicamentosApi {
    var buscarHandler: suspend (String) -> List<MedicamentoDto> = { emptyList() }
    var buscarPorNombreHandler: suspend (String) -> List<MedicamentoDto> = { emptyList() }
    var whereBusquedaRecibido: String? = null
    val wheresBusqueda = mutableListOf<String>()
    var detalleHandler: suspend (String) -> List<MedicamentoDto> = { emptyList() }
    var precioHandler: suspend (String) -> List<PrecioDto> = { emptyList() }
    var ventaLibreHandler: suspend () -> List<Map<String, Any>> = { emptyList() }
    var whereRecibido: String? = null
    var llamadasDetalle = 0
    var llamadasCatalogo = 0

    override suspend fun buscar(
        texto: String,
        select: String,
        limit: Int
    ): List<MedicamentoDto> = buscarHandler(texto)

    override suspend fun buscarPorNombre(
        where: String,
        select: String,
        limit: Int
    ): List<MedicamentoDto> {
        whereBusquedaRecibido = where
        wheresBusqueda += where
        return buscarPorNombreHandler(where)
    }

    override suspend fun detalle(expediente: String, limit: Int): List<MedicamentoDto> {
        llamadasDetalle++
        return detalleHandler(expediente)
    }

    override suspend fun precio(where: String, limit: Int): List<PrecioDto> {
        whereRecibido = where
        return precioHandler(where)
    }

    override suspend fun ventaLibrePrincipios(select: String, limit: Int): List<Map<String, Any>> {
        llamadasCatalogo++
        return ventaLibreHandler()
    }
}
