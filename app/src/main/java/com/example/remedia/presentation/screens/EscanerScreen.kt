package com.example.remedia.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.remedia.presentation.components.EstadoContenido
import com.example.remedia.presentation.model.EstadoUi

/**
 * P2 · Escáner.
 * Estados: Cargando = leyendo el texto de la foto, Éxito = texto detectado,
 * Vacío = no se detectó texto, Error = falló la cámara o falta el permiso.
 *
 * Por ahora el "visor" es un recuadro vacío. Cuando se integre CameraX,
 * el PreviewView va dentro de VisorCamara y esta pantalla no cambia su firma.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanerScreen(
    estado: EstadoUi<String>,
    onVolver: () -> Unit,
    onCapturar: () -> Unit,
    onUsarTexto: (String) -> Unit,
    onReintentar: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear caja") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VisorCamara(modifier = Modifier.fillMaxWidth().weight(1f))

            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                EstadoContenido(
                    estado = estado,
                    mensajeVacio = "No se detectó texto. Acerca la cámara al nombre de la caja.",
                    onReintentar = onReintentar
                ) { textoDetectado ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Texto detectado:", style = MaterialTheme.typography.labelLarge)
                        Text(textoDetectado, style = MaterialTheme.typography.titleMedium)
                        Button(onClick = { onUsarTexto(textoDetectado) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Buscar este medicamento")
                        }
                    }
                }
            }

            Button(onClick = onCapturar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Text("  Capturar")
            }
        }
    }
}

/** Recuadro que simula el visor. Aquí irá la vista de CameraX más adelante. */
@Composable
private fun VisorCamara(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.Black, RoundedCornerShape(16.dp))
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("Vista de cámara", color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
private fun EscanerExitoPreview() =
    EscanerScreen(EstadoUi.Exito("Medicamento Ejemplo A 500 mg"), {}, {}, {})

@Preview(showBackground = true)
@Composable
private fun EscanerCargandoPreview() =
    EscanerScreen(EstadoUi.Cargando, {}, {}, {})
