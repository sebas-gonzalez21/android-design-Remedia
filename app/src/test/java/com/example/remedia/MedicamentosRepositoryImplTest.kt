package com.example.remedia

import com.example.remedia.data.dto.PrecioDto
import com.example.remedia.data.repository.MedicamentosRepositoryImpl
import com.example.remedia.data.repository.NormalizadorConsulta
import com.example.remedia.data.repository.candidatosDeBusqueda
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class MedicamentosRepositoryImplTest {

    private val api = FakeMedicamentosApi()
    private val repository = MedicamentosRepositoryImpl(api)

    @Test
    fun `buscar completa venta libre y descripcion desde el select ampliado`() = runTest {
        api.buscarHandler = { listOf(dtoDePrueba(principioactivo = " ACETAMINOFEN ")) }
        // El catÃ¡logo real usa tilde; el CUM no. La comparaciÃ³n debe ignorar acentos.
        api.ventaLibreHandler = { listOf(mapOf("principioactivo" to "  AcetaminofÃ©n  ")) }

        val resultado = repository.buscar("dolex")

        assertTrue(resultado.single().ventaLibre)
        assertEquals("PARACETAMOL", resultado.single().paraQueSirve)
        assertEquals(listOf(" ACETAMINOFEN "), resultado.single().principiosActivos)
    }

    @Test
    fun `catalogo de venta libre se descarga una sola vez`() = runTest {
        api.buscarHandler = { listOf(dtoDePrueba()) }
        api.ventaLibreHandler = { listOf(mapOf("principioactivo" to "ACETAMINOFEN")) }

        repository.buscar("dolex")
        repository.buscar("dolex")

        assertEquals(1, api.llamadasCatalogo)
    }

    @Test
    fun `precio y venta libre se consultan en paralelo`() = runTest {
        val enVuelo = AtomicInteger()
        val maximo = AtomicInteger()
        api.detalleHandler = { listOf(dtoDePrueba()) }
        api.ventaLibreHandler = { listOf(mapOf("principioactivo" to "ACETAMINOFEN")) }
        api.precioHandler = {
            val actual = enVuelo.incrementAndGet()
            maximo.updateAndGet { anterior -> maxOf(anterior, actual) }
            delay(50)
            enVuelo.decrementAndGet()
            listOf(PrecioDto(cum = "123-1", precioMaximo = " 1000 "))
        }
        val ventaOriginal = api.ventaLibreHandler
        api.ventaLibreHandler = {
            val actual = enVuelo.incrementAndGet()
            maximo.updateAndGet { anterior -> maxOf(anterior, actual) }
            delay(50)
            enVuelo.decrementAndGet()
            ventaOriginal()
        }

        val resultado = repository.cargarDetalle("123")

        assertEquals(2, maximo.get())
        assertEquals("$ 1000", resultado?.precioMaximo)
        assertTrue(resultado!!.ventaLibre)
        assertEquals("starts_with(cum, '123-')", api.whereRecibido)
    }

    @Test
    fun `cancelacion del precio se propaga en vez de convertirse en dato opcional`() = runTest {
        api.detalleHandler = { listOf(dtoDePrueba()) }
        api.precioHandler = { throw CancellationException("cancelada") }

        val lanzada = capture { repository.cargarDetalle("123") }
        assertTrue(lanzada is CancellationException)
    }

    @Test
    fun `expediente no numerico se rechaza sin llamar a la api`() = runTest {
        val lanzada = capture { repository.cargarDetalle("123' OR '1'='1") }
        assertTrue(lanzada is IllegalArgumentException)
        assertEquals(0, api.llamadasDetalle)
    }

    @Test
    fun `candidatos de busqueda descartan ruido y priorizan la palabra mas larga`() {
        val candidatos = candidatosDeBusqueda(
            "Ince I Medcanerts I ta re Alvio Del Dolor / Dolor General Aspirina 100mg Tableta"
        )

        assertEquals(listOf("MEDCANERTS", "ASPIRINA", "INCE"), candidatos)
    }

    @Test
    fun `texto ruidoso del ocr encuentra el medicamento por prefijo`() = runTest {
        api.buscarHandler = { emptyList() }
        api.buscarPorNombreHandler = { where ->
            if (where.contains("ASPIRINA")) {
                listOf(dtoDePrueba(producto = "ASPIRINA 100 MG"))
            } else {
                emptyList()
            }
        }

        val resultado = repository.buscar(
            "Ince I Medcanerts I ta re Alvio Del Dolor / Dolor General Aspirina 100mg Tableta"
        )

        assertEquals("ASPIRINA 100 MG", resultado.single().nombre)
        assertTrue(api.wheresBusqueda.first().contains("MEDCANERTS"))
        assertEquals("upper(producto) like 'ASPIRINA%'", api.whereBusquedaRecibido)
    }

    @Test
    fun `filas repetidas se agrupan sin el group de la api`() = runTest {
        api.buscarHandler = {
            listOf(
                dtoDePrueba(expediente = "123", producto = "DOLEX", principioactivo = "ACETAMINOFEN"),
                dtoDePrueba(expediente = "123", producto = "DOLEX", principioactivo = "CAFEINA"),
                dtoDePrueba(expediente = "999", producto = "OTRO", principioactivo = "IBUPROFENO")
            )
        }

        val resultado = repository.buscar("dolex")

        assertEquals(2, resultado.size)
        assertEquals("DOLEX", resultado.first().nombre)
        assertEquals(
            listOf("ACETAMINOFEN", "CAFEINA"),
            resultado.first().principiosActivos
        )
    }

    @Test
    fun `el repositorio prueba los candidatos que entrega el normalizador inyectado`() = runTest {
        api.buscarHandler = { emptyList() }
        api.buscarPorNombreHandler = { where ->
            if (where.contains("ASPIRINA")) {
                listOf(dtoDePrueba(producto = "ASPIRINA 100 MG"))
            } else {
                emptyList()
            }
        }
        val repositorioConGemini = MedicamentosRepositoryImpl(
            api = api,
            normalizador = object : NormalizadorConsulta {
                override suspend fun candidatos(texto: String) = listOf("ASPIRINA")
            }
        )

        val resultado = repositorioConGemini.buscar("texto ruidoso cualquiera")

        assertEquals("ASPIRINA 100 MG", resultado.single().nombre)
        assertEquals("upper(producto) like 'ASPIRINA%'", api.whereBusquedaRecibido)
    }

    @Test
    fun `el contexto del ocr se normaliza antes que la consulta elegida`() = runTest {
        api.buscarHandler = { listOf(dtoDePrueba(producto = "RESULTADO INCORRECTO")) }
        api.buscarPorNombreHandler = { where ->
            if (where.contains("ASPIRINA")) {
                listOf(dtoDePrueba(producto = "ASPIRINA 100 MG"))
            } else {
                emptyList()
            }
        }
        val repositorioConGemini = MedicamentosRepositoryImpl(
            api = api,
            normalizador = object : NormalizadorConsulta {
                override suspend fun candidatos(texto: String): List<String> {
                    assertTrue(texto.contains("Medcanerts", ignoreCase = true))
                    return listOf("ASPIRINA")
                }
            }
        )

        val resultado = repositorioConGemini.buscar(
            "ASPIRINA",
            "Ince I Medcanerts I ta re Alvio Del Dolor / Aspirina 100mg"
        )

        assertEquals("ASPIRINA 100 MG", resultado.single().nombre)
        assertEquals(0, api.llamadasBuscar)
        assertEquals("upper(producto) like 'ASPIRINA%'", api.whereBusquedaRecibido)
    }

    private inline fun capture(bloque: () -> Unit): Throwable? = try {
        bloque()
        null
    } catch (e: Throwable) {
        e
    }
}
