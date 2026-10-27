package com.example.remedia.presentation.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remedia.presentation.components.BuscadorBar
import com.example.remedia.presentation.components.ChipFiltro
import com.example.remedia.presentation.components.entradaSuave
import com.example.remedia.presentation.model.EstadoUi
import com.example.remedia.ui.theme.Borde
import com.example.remedia.ui.theme.Fondo
import com.example.remedia.ui.theme.Gris400
import com.example.remedia.ui.theme.Superficie
import com.example.remedia.ui.theme.TealPrimary
import com.example.remedia.ui.theme.TealSoft
import com.example.remedia.ui.theme.TextoPrincipal
import com.example.remedia.ui.theme.TextoSecundario

private val categorias = listOf("Dolor y fiebre", "Hipertensión", "Antibióticos", "Gástricos")

/**
 * P1 · Inicio / Buscar (estilo Stitch).
 * Sin lógica: recibe el estado de "recientes" y reporta clics hacia arriba.
 * Lo único que guarda es el texto a medio escribir (remember).
 */
@Composable
fun InicioScreen(
    recientes: EstadoUi<List<String>>,
    onBuscar: (String) -> Unit,
    onEscanear: () -> Unit,
    onReintentar: () -> Unit = {}
) {
    var texto by remember { mutableStateOf("") }

    Scaffold(containerColor = Fondo) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Encabezado() }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = TealSoft) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .background(TealPrimary, CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "INVIMA COLOMBIA • Catálogo oficial",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                    }
                    Text(
                        text = "¿Qué medicamento buscas?",
                        fontSize = 26.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextoPrincipal
                    )
                    Text(
                        text = "Consulta registro INVIMA, precios regulados y genéricos autorizados.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = TextoSecundario
                    )
                    BuscadorBar(
                        texto = texto,
                        onTextoChange = { texto = it },
                        onBuscar = { if (texto.isNotBlank()) onBuscar(texto) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }

            item { BotonEscanear(onEscanear) }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Categorías frecuentes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextoPrincipal
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorias.forEach { categoria ->
                            ChipFiltro(
                                texto = categoria,
                                seleccionado = false,
                                onClick = { onBuscar(categoria) }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Recientes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextoPrincipal
                )
            }

            when (recientes) {
                is EstadoUi.Cargando -> item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TealPrimary)
                    }
                }

                is EstadoUi.Exito -> itemsIndexed(recientes.datos) { indice, reciente ->
                    TarjetaReciente(
                        nombre = reciente,
                        onClick = { onBuscar(reciente) },
                        modifier = Modifier.entradaSuave(indice)
                    )
                }

                is EstadoUi.Vacio -> item {
                    Text("Aún no has buscado nada.", color = TextoSecundario, fontSize = 13.sp)
                }

                is EstadoUi.Error -> item {
                    Column {
                        Text(recientes.mensaje, color = TextoSecundario, fontSize = 13.sp)
                        TextButton(onClick = onReintentar) {
                            Text("Reintentar", color = TealPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Encabezado() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(TealSoft, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Medication,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Remedia",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextoPrincipal
        )
        Spacer(Modifier.width(8.dp))
        Surface(shape = RoundedCornerShape(6.dp), color = TealSoft) {
            Text(
                text = "COLOMBIA",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
            )
        }
    }
}

/** Acción principal de la pantalla: pulsa suavemente para llamar la atención. */
@Composable
private fun BotonEscanear(onEscanear: () -> Unit) {
    val transicion = rememberInfiniteTransition(label = "pulso")
    val escala by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )

    Surface(
        onClick = onEscanear,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            },
        shape = RoundedCornerShape(20.dp),
        color = TealPrimary,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Escanear caja",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Reconocimiento de texto al instante",
                    fontSize = 12.sp,
                    color = TealSoft
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun TarjetaReciente(nombre: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Superficie,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TealSoft, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Medication,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
                Text(text = "Buscar de nuevo", fontSize = 11.sp, color = TextoSecundario)
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Gris400,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}