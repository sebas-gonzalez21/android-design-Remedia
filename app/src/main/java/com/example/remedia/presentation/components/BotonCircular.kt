package com.example.remedia.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.remedia.ui.theme.Borde
import com.example.remedia.ui.theme.Superficie

/** Botón redondo blanco con borde (atrás, compartir). */
@Composable
fun BotonCircular(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = Superficie,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) { contenido() }
    }
}