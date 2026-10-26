package com.example.remedia.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.remedia.presentation.components.EstadoChip
import com.example.remedia.presentation.components.EstadoContenido
import com.example.remedia.presentation.components.VentaLibreBadge
import com.example.remedia.presentation.model.EstadoUi
import com.example.remedia.presentation.model.MedicamentoUi
import com.example.remedia.presentation.model.MockData

/**
 * P4 · Detalle. Información completa y botón "Ver equivalentes".
 * Se usa LazyColumn para que la pantalla desplace si el contenido es largo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    estado: EstadoUi<MedicamentoUi>,
    onVolver: () -> Unit,
    onVerEquivalentes: (String) -> Unit,
    onReintentar: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        EstadoContenido(
            estado = estado,
            mensajeVacio = "No se encontró este medicamento.",
            onReintentar = onReintentar,
            modifier = Modifier.padding(padding)
        ) { med ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(med.nombre, style = MaterialTheme.typography.headlineSmall)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstadoChip(vigente = med.vigente)
                        VentaLibreBadge(ventaLibre = med.ventaLibre)
                    }
                }
                item { Dato("¿Para qué sirve?", med.paraQueSirve) }
                item { Dato("Principios activos", med.principiosActivos.joinToString("\n")) }
                item { HorizontalDivider() }
                item { Dato("Laboratorio", med.laboratorio) }
                item { Dato("Forma farmacéutica", med.formaFarmaceutica) }
                item { Dato("Registro INVIMA", med.registroInvima) }
                item { Dato("Precio máximo legal", med.precioMaximo ?: "No regulado / sin dato") }
                item {
                    Button(
                        onClick = { onVerEquivalentes(med.principiosActivos.first()) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Ver equivalentes") }
                }
            }
        }
    }
}

@Composable
private fun Dato(titulo: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalleExitoPreview() =
    DetalleScreen(EstadoUi.Exito(MockData.medicamentos.first()), {}, {})

@Preview(showBackground = true)
@Composable
private fun DetalleCargandoPreview() =
    DetalleScreen(EstadoUi.Cargando, {}, {})
