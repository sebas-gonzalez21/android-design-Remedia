package com.example.remedia.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Insignia "Venta libre" / "Requiere fórmula". */
@Composable
fun VentaLibreBadge(ventaLibre: Boolean, modifier: Modifier = Modifier) {
    val colorFondo = if (ventaLibre) MaterialTheme.colorScheme.secondaryContainer
    else MaterialTheme.colorScheme.tertiaryContainer
    val colorTexto = if (ventaLibre) MaterialTheme.colorScheme.onSecondaryContainer
    else MaterialTheme.colorScheme.onTertiaryContainer

    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = colorFondo) {
        Text(
            text = if (ventaLibre) "Venta libre" else "Requiere fórmula",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colorTexto
        )
    }
}
