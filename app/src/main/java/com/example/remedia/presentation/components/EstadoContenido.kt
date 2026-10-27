package com.example.remedia.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.remedia.presentation.model.EstadoUi

/**
 * Pinta los 4 estados de una pantalla. Cargando, Vacío y Error se dibujan aquí;
 * en Éxito se llama al contenido propio de cada pantalla.
 * Así las 4 screens manejan los estados igual y sin repetir código.
 */
@Composable
fun <T> EstadoContenido(
    estado: EstadoUi<T>,
    mensajeVacio: String,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable (T) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (estado) {
            is EstadoUi.Cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))

            is EstadoUi.Exito -> contenido(estado.datos)

            is EstadoUi.Vacio -> MensajeCentrado(
                icono = { Icon(Icons.Default.SearchOff, contentDescription = null) },
                mensaje = mensajeVacio,
                modifier = Modifier.align(Alignment.Center)
            )

            is EstadoUi.Error -> MensajeCentrado(
                icono = { Icon(Icons.Default.Warning, contentDescription = null) },
                mensaje = estado.mensaje,
                modifier = Modifier.align(Alignment.Center),
                accion = { Button(onClick = onReintentar) { Text("Reintentar") } }
            )
        }
    }
}

@Composable
private fun MensajeCentrado(
    icono: @Composable () -> Unit,
    mensaje: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        icono()
        Text(text = mensaje, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        accion?.invoke()
    }
}
