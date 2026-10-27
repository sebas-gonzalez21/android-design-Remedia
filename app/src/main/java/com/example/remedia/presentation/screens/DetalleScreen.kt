package com.example.remedia.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remedia.presentation.components.BotonCircular
import com.example.remedia.presentation.components.EstadoChip
import com.example.remedia.presentation.components.EstadoContenido
import com.example.remedia.presentation.components.entradaSuave
import com.example.remedia.presentation.model.EstadoUi
import com.example.remedia.presentation.model.MedicamentoUi
import com.example.remedia.ui.theme.Borde
import com.example.remedia.ui.theme.Fondo
import com.example.remedia.ui.theme.FormulaFondo
import com.example.remedia.ui.theme.FormulaTexto
import com.example.remedia.ui.theme.Gris400
import com.example.remedia.ui.theme.MentaHero
import com.example.remedia.ui.theme.Superficie
import com.example.remedia.ui.theme.SuperficieGris
import com.example.remedia.ui.theme.TealPrimary
import com.example.remedia.ui.theme.TealSoft
import com.example.remedia.ui.theme.TextoCuerpo
import com.example.remedia.ui.theme.TextoPrincipal
import com.example.remedia.ui.theme.TextoSecundario
import com.example.remedia.ui.theme.Vigente
import com.example.remedia.ui.theme.VigenteFondo

/**
 * P4 · Detalle / Ficha técnica (estilo Stitch).
 * Solo muestra datos que sí entrega la API: nada de dosis ni excipientes.
 */
@Composable
fun DetalleScreen(
    estado: EstadoUi<MedicamentoUi>,
    onVolver: () -> Unit,
    onVerEquivalentes: (String) -> Unit,
    onReintentar: () -> Unit = {}
) {
    Scaffold(
        containerColor = Fondo,
        bottomBar = {
            if (estado is EstadoUi.Exito) {
                val med = estado.datos
                BarraEquivalentes(
                    onClick = { onVerEquivalentes(med.principiosActivos.firstOrNull() ?: med.nombre) }
                )
            }
        }
    ) { padding ->
        EstadoContenido(
            estado = estado,
            mensajeVacio = "No se encontró este medicamento.",
            onReintentar = onReintentar,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) { med ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Cabecera(med = med, onVolver = onVolver) }

                item {
                    TarjetaSeccion(
                        icono = Icons.Outlined.Medication,
                        titulo = "¿Para qué sirve?",
                        indice = 0,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = med.paraQueSirve,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = TextoCuerpo
                        )
                    }
                }

                item {
                    TarjetaSeccion(
                        icono = Icons.Outlined.Science,
                        titulo = "Principios activos",
                        indice = 1,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        med.principiosActivos.forEach { principio ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Fondo,
                                border = BorderStroke(1.dp, SuperficieGris)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "PRINCIPIO ACTIVO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gris400
                                    )
                                    Text(
                                        text = principio,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextoPrincipal
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        HorizontalDivider(color = SuperficieGris)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Forma farmacéutica", fontSize = 12.sp, color = TextoSecundario)
                            Text(
                                text = med.formaFarmaceutica,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoPrincipal
                            )
                        }
                    }
                }

                item {
                    TarjetaSeccion(
                        icono = Icons.Outlined.VerifiedUser,
                        titulo = "Registro INVIMA",
                        indice = 2,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        extremo = { EstadoChip(vigente = med.vigente) }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Fondo,
                            border = BorderStroke(1.dp, SuperficieGris)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "CÓDIGO REGISTRO SANITARIO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gris400
                                )
                                Text(
                                    text = med.registroInvima,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextoPrincipal
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = Vigente,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Fuente: datos abiertos de INVIMA (datos.gov.co)",
                                fontSize = 11.sp,
                                color = TextoSecundario
                            )
                        }
                    }
                }

                item {
                    TarjetaSeccion(
                        icono = Icons.Outlined.Payments,
                        titulo = "Precio máximo legal",
                        indice = 3,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        if (med.precioMaximo != null) {
                            Text(
                                text = "TOPE REGULADO NACIONAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gris400
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = med.precioMaximo,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TealPrimary
                                )
                                Text(
                                    text = " COP",
                                    modifier = Modifier.padding(bottom = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoSecundario
                                )
                            }
                            Text(
                                text = "Es el tope al que puede venderse este medicamento.",
                                modifier = Modifier.padding(top = 4.dp),
                                fontSize = 11.sp,
                                color = TextoSecundario
                            )
                        } else {
                            Text(
                                text = "No regulado / sin dato",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoSecundario
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Cabecera(med: MedicamentoUi, onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(MentaHero, Fondo)))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonCircular(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextoPrincipal,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Remedia",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TealPrimary
            )
            Text(text = "  •  Ficha técnica", fontSize = 12.sp, color = TextoSecundario)
            Spacer(Modifier.weight(1f))
            BotonCircular(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Compartir",
                    tint = TextoSecundario,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Surface(shape = RoundedCornerShape(50), color = TealPrimary.copy(alpha = 0.1f)) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "FICHA TÉCNICA OFICIAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TealPrimary
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = med.nombre,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextoPrincipal
        )
        Text(
            text = med.laboratorio,
            modifier = Modifier.padding(top = 4.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextoSecundario
        )

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InsigniaVentaLibre(ventaLibre = med.ventaLibre)
            Surface(
                shape = RoundedCornerShape(50),
                color = Superficie,
                border = BorderStroke(1.dp, Borde)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = Gris400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = med.formaFarmaceutica,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextoCuerpo
                    )
                }
            }
        }
    }
}

/** Insignia destacada del encabezado: verde con escudo (venta libre) o ámbar (fórmula). */
@Composable
private fun InsigniaVentaLibre(ventaLibre: Boolean) {
    val fondo = if (ventaLibre) VigenteFondo else FormulaFondo
    val color = if (ventaLibre) Vigente else FormulaTexto

    Surface(shape = RoundedCornerShape(50), color = fondo) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (ventaLibre) Icons.Outlined.Shield else Icons.Outlined.Description,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (ventaLibre) "Venta libre" else "Requiere fórmula",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun TarjetaSeccion(
    icono: ImageVector,
    titulo: String,
    indice: Int,
    modifier: Modifier = Modifier,
    extremo: (@Composable () -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .entradaSuave(indice),
        shape = RoundedCornerShape(16.dp),
        color = Superficie,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(TealSoft, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = titulo,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextoPrincipal
                    )
                }
                extremo?.invoke()
            }
            Spacer(Modifier.height(12.dp))
            contenido()
        }
    }
}

@Composable
private fun BarraEquivalentes(onClick: () -> Unit) {
    Surface(color = Superficie, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Surface(
                onClick = onClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                color = TealPrimary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SwapHoriz,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Ver equivalentes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}