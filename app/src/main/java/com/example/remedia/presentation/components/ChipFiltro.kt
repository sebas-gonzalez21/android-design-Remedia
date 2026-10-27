package com.example.remedia.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remedia.ui.theme.Borde
import com.example.remedia.ui.theme.Superficie
import com.example.remedia.ui.theme.TealPrimary
import com.example.remedia.ui.theme.TextoPrincipal

/** Chip de filtro en píldora: teal relleno si está seleccionado, blanco con borde si no. */
@Composable
fun ChipFiltro(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (seleccionado) TealPrimary else Superficie,
        border = if (seleccionado) null else BorderStroke(1.dp, Borde)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (seleccionado) Color.White else TextoPrincipal
        )
    }
}