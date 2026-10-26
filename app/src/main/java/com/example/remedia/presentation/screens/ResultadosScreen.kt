package com.example.remedia.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.remedia.presentation.components.EstadoContenido
import com.example.remedia.presentation.components.MedicamentoCard
import com.example.remedia.presentation.model.EstadoUi
import com.example.remedia.presentation.model.MockData
import com.example.remedia.presentation.model.MedicamentoUi

private enum class Filtro(val etiqueta: String) {
    TODOS("Todos"), VIGENTES("Vigentes"), VENCIDOS("Vencidos")
}

/**
 * P3 · Resultados. Lista con LazyColumn (nunca Column).
 * El filtro es estado temporal de la UI, por eso vive en remember.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadosScreen(
    titulo: String,
    estado: EstadoUi<List<MedicamentoUi>>,
    onVolver: () -> Unit,
    onMedicamentoClick: (String) -> Unit,
    onReintentar: () -> Unit = {}
) {
    var filtro by remember { mutableStateOf(Filtro.TODOS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Filtro.entries.forEach { opcion ->
                    FilterChip(
                        selected = filtro == opcion,
                        onClick = { filtro = opcion },
                        label = { Text(opcion.etiqueta) }
                    )
                }
            }

            EstadoContenido(
                estado = estado,
                mensajeVacio = "No encontramos medicamentos con esa búsqueda.",
                onReintentar = onReintentar
            ) { lista ->
                val visibles = when (filtro) {
                    Filtro.TODOS -> lista
                    Filtro.VIGENTES -> lista.filter { it.vigente }
                    Filtro.VENCIDOS -> lista.filter { !it.vigente }
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visibles, key = { it.expediente }) { medicamento ->
                        MedicamentoCard(
                            medicamento = medicamento,
                            onClick = { onMedicamentoClick(medicamento.expediente) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultadosExitoPreview() =
    ResultadosScreen("Resultados", EstadoUi.Exito(MockData.medicamentos), {}, {})

@Preview(showBackground = true)
@Composable
private fun ResultadosErrorPreview() =
    ResultadosScreen("Resultados", EstadoUi.Error("No se pudo conectar."), {}, {})
