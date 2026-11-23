package com.example.remedia

import com.example.remedia.presentation.viewmodel.DetalleViewModel
import com.example.remedia.presentation.viewmodel.EstadoUi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DetalleViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeMedicamentosRepository()
    private lateinit var viewModel: DetalleViewModel

    @Before
    fun setUp() {
        viewModel = DetalleViewModel(repository)
    }

    @Test
    fun `cargar dos veces el mismo expediente reutiliza la cache`() = runTest {
        repository.detalleHandler = { medicamentoDePrueba() }

        viewModel.cargar("123")
        viewModel.cargar("123")

        assertEquals(1, repository.expedientes.size)
        assertEquals(
            EstadoUi.Exito(medicamentoDePrueba()),
            viewModel.estado.value
        )
    }

    @Test
    fun `detalle inexistente termina en error`() = runTest {
        repository.detalleHandler = { null }

        viewModel.cargar("999")

        assertEquals(
            "No encontramos este medicamento. Vuelve a buscarlo.",
            (viewModel.estado.value as EstadoUi.Error).mensaje
        )
    }
}
