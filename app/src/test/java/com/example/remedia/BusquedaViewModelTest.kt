package com.example.remedia

import com.example.remedia.presentation.viewmodel.BusquedaViewModel
import com.example.remedia.presentation.viewmodel.EstadoUi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class BusquedaViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeMedicamentosRepository()
    private val recientesSource = FakeRecientesDataSource()
    private lateinit var viewModel: BusquedaViewModel

    @Before
    fun setUp() {
        viewModel = BusquedaViewModel(repository, recientesSource)
    }

    @Test
    fun `buscar guarda resultado y reciente`() = runTest {
        repository.buscarHandler = { _, _ -> listOf(medicamentoDePrueba()) }

        viewModel.buscar("  dolex  ")

        assertEquals(EstadoUi.Exito(listOf(medicamentoDePrueba())), viewModel.resultados.value)
        assertEquals(EstadoUi.Exito(listOf("dolex")), viewModel.recientes.value)
        assertEquals(listOf("dolex"), repository.consultas)
        assertEquals(listOf("dolex"), recientesSource.guardados)
    }

    @Test
    fun `restaura el historial persistido al crear el viewmodel`() = runTest {
        recientesSource.sembrar(listOf("dolex", "advil"))

        val restaurado = BusquedaViewModel(repository, recientesSource)

        assertEquals(EstadoUi.Exito(listOf("dolex", "advil")), restaurado.recientes.value)
    }

    @Test
    fun `revisitar resultados no repite peticion ni registro`() = runTest {
        repository.buscarHandler = { _, _ -> listOf(medicamentoDePrueba()) }
        viewModel.buscar("dolex")
        repository.consultas.clear()

        viewModel.buscar("dolex")

        assertEquals(emptyList<String>(), repository.consultas)
        assertEquals(1, (viewModel.recientes.value as EstadoUi.Exito).datos.size)
        assertEquals(EstadoUi.Exito(listOf(medicamentoDePrueba())), viewModel.resultados.value)
    }

    @Test
    fun `nueva busqueda cancela la anterior sin pisar el resultado`() = runTest {
        val compuerta = CompletableDeferred<Unit>()
        repository.buscarHandler = { texto, _ ->
            if (texto == "lenta") {
                compuerta.await()
                currentCoroutineContext().ensureActive()
            }
            listOf(medicamentoDePrueba(expediente = texto, nombre = texto))
        }

        viewModel.buscar("lenta")
        viewModel.buscar("rapida")
        compuerta.complete(Unit)

        val estado = viewModel.resultados.value
        assertTrue(estado is EstadoUi.Exito)
        assertEquals("rapida", (estado as EstadoUi.Exito).datos.first().expediente)
    }

    @Test
    fun `reintentar con forzar ignora la cache de error`() = runTest {
        var llamadas = 0
        repository.buscarHandler = { _, _ ->
            llamadas++
            if (llamadas == 1) throw IOException("sin red") else listOf(medicamentoDePrueba())
        }

        viewModel.buscar("dolex")
        assertTrue(viewModel.resultados.value is EstadoUi.Error)

        viewModel.buscar("dolex", forzar = true)

        assertTrue(viewModel.resultados.value is EstadoUi.Exito)
        assertEquals(2, llamadas)
    }

    @Test
    fun `el contexto del escanner se pasa al repositorio`() = runTest {
        repository.buscarHandler = { _, _ -> listOf(medicamentoDePrueba()) }

        viewModel.buscar("ASPIRINA", "OCR con ruido | Aspirina 100mg")

        assertEquals("ASPIRINA", repository.consultas.single())
        assertEquals("OCR con ruido | Aspirina 100mg", repository.contextos.single())
    }
}
