package com.example.remedia.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remedia.presentation.model.MedicamentoUi
import com.example.remedia.ui.theme.Borde
import com.example.remedia.ui.theme.Fondo
import com.example.remedia.ui.theme.Gris400
import com.example.remedia.ui.theme.RojoBanner
import com.example.remedia.ui.theme.RojoBordeBanner
import com.example.remedia.ui.theme.RojoTexto
import com.example.remedia.ui.theme.Superficie
import com.example.remedia.ui.theme.SuperficieGris
import com.example.remedia.ui.theme.TealPrimary
import com.example.remedia.ui.theme.TextoPrincipal
import com.example.remedia.ui.theme.TextoSecundario
import com.example.remedia.ui.theme.Vencido

/** Tarjeta de un medicamento en la lista de resultados (estilo Stitch). */
@Composable
fun MedicamentoCard(
    medicamento: MedicamentoUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    indice: Int = 0
) {
    val borde = if (medicamento.vigente) Borde else Vencido.copy(alpha = 0.45f)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .entradaSuave(indice),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Superficie),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, borde)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = medicamento.nombre,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextoPrincipal
                )
                EstadoChip(vigente = medicamento.vigente)
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medicamento.laboratorio,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    color = TextoSecundario
                )
                Text(
                    text = medicamento.registroInvima,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Gris400
                )
            }

            Spacer(Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Fondo,
                border = BorderStroke(1.dp, SuperficieGris)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = medicamento.formaFarmaceutica,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextoSecundario
                    )
                }
            }

            if (!medicamento.vigente) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RojoBanner,
                    border = BorderStroke(1.dp, RojoBordeBanner)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = Vencido,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Registro sanitario ${medicamento.registroInvima} vencido. " +
                                    "Consulte con su médico alternativas vigentes.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = RojoTexto
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = SuperficieGris)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VentaLibreBadge(ventaLibre = medicamento.ventaLibre)

                if (medicamento.precioMaximo != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PRECIO REGULADO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gris400
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = medicamento.precioMaximo,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextoPrincipal
                            )
                            Text(
                                text = " COP",
                                fontSize = 10.sp,
                                color = TextoSecundario
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Sin precio oficial",
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = Gris400
                    )
                }
            }
        }
    }
}