package com.example.remedia.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remedia.ui.theme.Vencido
import com.example.remedia.ui.theme.VencidoFondo
import com.example.remedia.ui.theme.Vigente
import com.example.remedia.ui.theme.VigenteFondo

/** Chip "Vigente" (verde) / "Vencido" (rojo) del registro INVIMA. */
@Composable
fun EstadoChip(vigente: Boolean, modifier: Modifier = Modifier) {
    val fondo = if (vigente) VigenteFondo else VencidoFondo
    val texto = if (vigente) Vigente else Vencido

    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = fondo) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (vigente) Icons.Outlined.Check else Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = texto,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(14.dp)
            )
            Text(
                text = if (vigente) "Vigente" else "Vencido",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = texto
            )
        }
    }
}