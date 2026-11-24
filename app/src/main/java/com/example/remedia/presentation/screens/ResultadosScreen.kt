package com.example.remedia.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remedia.presentation.components.BotonCircular
import com.example.remedia.presentation.components.BuscadorBar
import com.example.remedia.presentation.components.ChipFiltro
import com.example.remedia.presentation.components.EstadoContenido
import com.example.remedia.presentation.components.MedicamentoCard
import com.example.remedia.presentation.viewmodel.BusquedaViewModel
import com.example.remedia.ui.theme.Fondo
import com.example.remedia.ui.theme.TextoPrincipal
import com.example.remedia.ui.theme.TextoSecundario

private enum class Filtro(val etiqueta: String) {
    TODOS("Todos"), VENTA_LIBRE("Venta libre"), CON_FORMULA("Con fÃ³rmula")
}

/** Guarda Ã­ndice y desplazamiento para que volver del Detalle no reinicie el scroll. */
private val ListaStateSaver = Saver<LazyListState, ArrayList<Int>>(
    save = { arrayListOf(it.firstVisibleItemIndex, it.firstVisibleItemScrollOffset) },
    restore = { LazyListState(it[0], it[1]) }
)

/**
 * P3 Â· Resultados (estilo Stitch). Lista con LazyColumn (nunca Column).
 * La bÃºsqueda se lanza con LaunchedEffect(consulta), pero el ViewModel cachea el
 * resultado: al volver del Detalle se restaura el estado sin otra peticiÃ³n.
 * Filtro y scroll sobreviven a la rotaciÃ³n y al back stack (rememberSaveable).
 */
@Composable
fun ResultadosScreen(
    consulta: String,
    contexto: String = "",
    viewModel: BusquedaViewModel,
    onVolver: () -> Unit,
    onNuevaBusqueda: (String) -> Unit,
    onMedicamentoClick: (String) -> Unit
) {
    LaunchedEffect(consulta, contexto) { viewModel.buscar(consulta, contexto) }
    val estado by viewModel.resultados.collectAsStateWithLifecycle()
    var textoBusqueda by rememberSaveable { mutableStateOf(consulta) }
    var filtro by rememberSaveable { mutableStateOf(Filtro.TODOS) }
    val listaState = rememberSaveable(saver = ListaStateSaver) { LazyListState(0, 0) }

    Scaffold(containerColor = Fondo) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Fondo)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BotonCircular(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextoPrincipal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Remedia",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextoPrincipal
                    )
                    Text(text = "  â€¢  Resultados", fontSize = 12.sp, color = TextoSecundario)
                }

                Spacer(Modifier.height(12.dp))

                BuscadorBar(
                    texto = textoBusqueda,
                    onTextoChange = { textoBusqueda = it },
                    onBuscar = {
                        if (textoBusqueda.isNotBlank()) onNuevaBusqueda(textoBusqueda.trim())
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Filtro.entries.forEach { opcion ->
                        ChipFiltro(
                            texto = opcion.etiqueta,
                            seleccionado = filtro == opcion,
                            onClick = { filtro = opcion }
                        )
                    }
                }
            }

            EstadoContenido(
                estado = estado,
                mensajeVacio = "No encontramos medicamentos con esa bÃºsqueda.",
                onReintentar = { viewModel.buscar(consulta, contexto, forzar = true) }
            ) { lista ->
                val visibles = when (filtro) {
                    Filtro.TODOS -> lista
                    Filtro.VENTA_LIBRE -> lista.filter { it.ventaLibre }
                    Filtro.CON_FORMULA -> lista.filter { !it.ventaLibre }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listaState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${visibles.size} resultados para â€œ$consultaâ€",
                            fontSize = 12.sp,
                            color = TextoSecundario
                        )
                    }
                    itemsIndexed(visibles, key = { _, med -> med.expediente }) { indice, medicamento ->
                        MedicamentoCard(
                            medicamento = medicamento,
                            onClick = { onMedicamentoClick(medicamento.expediente) },
                            indice = indice
                        )
                    }
                }
            }
        }
    }
}