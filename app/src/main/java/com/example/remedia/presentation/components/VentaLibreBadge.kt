package com.example.remedia.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remedia.ui.theme.FormulaFondo
import com.example.remedia.ui.theme.FormulaTexto
import com.example.remedia.ui.theme.SuperficieGris
import com.example.remedia.ui.theme.TextoSecundario

/** Insignia "VENTA LIBRE" (gris) / "REQUIERE FÓRMULA" (ámbar). */
@Composable
fun VentaLibreBadge(ventaLibre: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = if (ventaLibre) SuperficieGris else FormulaFondo
    ) {
        Text(
            text = if (ventaLibre) "VENTA LIBRE" else "REQUIERE FÓRMULA",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (ventaLibre) TextoSecundario else FormulaTexto
        )
    }
}