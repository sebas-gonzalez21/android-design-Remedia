package com.example.remedia

import com.example.remedia.data.dto.PrecioDto
import com.example.remedia.data.repository.MedicamentosRepositoryImpl
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
        // El catálogo real usa tilde; el CUM no. La comparación debe ignorar acentos.
        api.ventaLibreHandler = { listOf(mapOf("principioactivo" to "  Acetaminofén  ")) }

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

    private inline fun capture(bloque: () -> Unit): Throwable? = try {
        bloque()
        null
    } catch (e: Throwable) {
        e
    }
}
