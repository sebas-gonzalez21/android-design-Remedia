package com.example.remedia.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Chip "Vigente" / "Vencido" del registro INVIMA. Solo pinta lo que recibe. */
@Composable
fun EstadoChip(vigente: Boolean, modifier: Modifier = Modifier) {
    val colorFondo = if (vigente) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.errorContainer
    val colorTexto = if (vigente) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onErrorContainer

    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = colorFondo) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (vigente) Icons.Default.Verified else Icons.Default.Cancel,
                contentDescription = null,
                tint = colorTexto,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = if (vigente) "Vigente" else "Vencido",
                style = MaterialTheme.typography.labelMedium,
                color = colorTexto
            )
        }
    }
}
