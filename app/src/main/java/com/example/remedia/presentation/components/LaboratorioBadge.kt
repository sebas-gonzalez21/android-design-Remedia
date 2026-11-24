package com.example.remedia.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remedia.ui.theme.Gris400
import com.example.remedia.ui.theme.TealDark
import com.example.remedia.ui.theme.TealSoft
import com.example.remedia.ui.theme.TextoPrincipal
import java.util.Locale

/** Colores estables para distinguir laboratorios de un vistazo en la lista. */
private val PaletaLaboratorio = listOf(
    Color(0xFF0F9D8A), // teal
    Color(0xFF2563EB), // azul
    Color(0xFF7C3AED), // violeta
    Color(0xFFDB2777), // fucsia
    Color(0xFFEA580C), // naranja
    Color(0xFF0E7490)  // cian
)

/**
 * Devuelve -1 cuando no hay laboratorio (color neutro). En caso contrario, un
 * índice estable de la paleta: el mismo laboratorio siempre tiene el mismo color
 * aunque aparezca en varias tarjetas o en búsquedas distintas.
 */
internal fun indiceColorLaboratorio(nombre: String): Int {
    val clave = nombre.trim().uppercase(Locale.ROOT)
    if (clave.isBlank() || clave == "NO ESPECIFICADO") return -1
    val hash = clave.hashCode().toLong() and 0x7FFFFFFF
    return (hash % PaletaLaboratorio.size).toInt()
}

/** Insignia "FARMACÉUTICA" con el titular del registro INVIMA. */
@Composable
fun LaboratorioBadge(nombre: String, modifier: Modifier = Modifier) {
    val indice = indiceColorLaboratorio(nombre)
    val color = if (indice < 0) Gris400 else PaletaLaboratorio[indice]

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = TealSoft
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Business,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "FARMACÉUTICA",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TealDark,
                    maxLines = 1
                )
                Text(
                    text = nombre,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
        }
    }
}
