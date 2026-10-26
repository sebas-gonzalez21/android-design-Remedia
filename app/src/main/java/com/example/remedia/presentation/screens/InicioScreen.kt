package com.example.remedia.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.remedia.presentation.components.BuscadorBar
import com.example.remedia.presentation.components.EstadoContenido
import com.example.remedia.presentation.model.EstadoUi
import com.example.remedia.presentation.model.MockData

/**
 * P1 · Inicio / Buscar.
 * Sin lógica: recibe el estado de "recientes" y reporta clics hacia arriba.
 * Lo único que guarda es el texto a medio escribir (remember).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    recientes: EstadoUi<List<String>>,
    onBuscar: (String) -> Unit,
    onEscanear: () -> Unit,
    onReintentar: () -> Unit = {}
) {
    var texto by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Remedia") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BuscadorBar(
                texto = texto,
                onTextoChange = { texto = it },
                onBuscar = { if (texto.isNotBlank()) onBuscar(texto) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = onEscanear, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  Escanear caja")
            }

            Text("Búsquedas recientes", style = MaterialTheme.typography.titleMedium)

            EstadoContenido(
                estado = recientes,
                mensajeVacio = "Aún no has buscado nada.",
                onReintentar = onReintentar
            ) { lista ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(lista) { reciente ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBuscar(reciente) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, contentDescription = null)
                            Text(text = "  $reciente", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InicioExitoPreview() =
    InicioScreen(EstadoUi.Exito(MockData.recientes), onBuscar = {}, onEscanear = {})

@Preview(showBackground = true)
@Composable
private fun InicioVacioPreview() =
    InicioScreen(EstadoUi.Vacio, onBuscar = {}, onEscanear = {})
