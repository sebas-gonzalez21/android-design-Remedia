package com.example.remedia.presentation.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remedia.presentation.model.EstadoUi
import com.example.remedia.ui.theme.Gris400
import com.example.remedia.ui.theme.TealPrimary
import com.example.remedia.ui.theme.TealSoft

/**
 * P2 · Escáner (estilo Stitch).
 * Estados: Cargando = leyendo el texto de la foto, Éxito = texto detectado,
 * Vacío = no se detectó texto, Error = falló la cámara o falta el permiso.
 *
 * El fondo oscuro simula la cámara. Cuando se integre CameraX, el PreviewView
 * reemplaza el Box del fondo y esta pantalla no cambia su firma.
 */
@Composable
fun EscanerScreen(
    estado: EstadoUi<String>,
    onVolver: () -> Unit,
    onCapturar: () -> Unit,
    onUsarTexto: (String) -> Unit,
    onReintentar: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B1120), Color(0xFF1E293B), Color(0xFF0B1120))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onVolver,
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Pastilla {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(TealPrimary, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "INVIMA VISIÓN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(44.dp))
            }

            Spacer(Modifier.weight(1f))

            Pastilla {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Enfoca el nombre del medicamento",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))

            Reticula()

            Spacer(Modifier.height(20.dp))

            ResultadoEscaneo(estado, onUsarTexto, onReintentar)

            Spacer(Modifier.weight(1f))

            Surface(
                onClick = onCapturar,
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(4.dp, Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .background(TealPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Capturar",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                onClick = onVolver,
                shape = RoundedCornerShape(50),
                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = Gris400,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Escribir el nombre",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Pastilla(contenido: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            contenido()
        }
    }
}

/** Marco de encuadre con esquinas teal/blancas y una línea láser que sube y baja. */
@Composable
private fun Reticula() {
    val transicion = rememberInfiniteTransition(label = "laser")
    val avance by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avance"
    )

    Box(modifier = Modifier.size(width = 280.dp, height = 220.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val largo = 36.dp.toPx()
            val grosor = 4.dp.toPx()

            fun esquina(x: Float, y: Float, dx: Float, dy: Float, color: Color) {
                drawLine(color, Offset(x, y), Offset(x + dx * largo, y), grosor, StrokeCap.Round)
                drawLine(color, Offset(x, y), Offset(x, y + dy * largo), grosor, StrokeCap.Round)
            }

            val margen = grosor / 2
            esquina(margen, margen, 1f, 1f, TealPrimary)
            esquina(size.width - margen, margen, -1f, 1f, Color.White)
            esquina(margen, size.height - margen, 1f, -1f, Color.White)
            esquina(size.width - margen, size.height - margen, -1f, -1f, TealPrimary)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .height(2.dp)
                .offset(y = (avance * 216f).dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, TealPrimary, Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun ResultadoEscaneo(
    estado: EstadoUi<String>,
    onUsarTexto: (String) -> Unit,
    onReintentar: () -> Unit
) {
    when (estado) {
        is EstadoUi.Cargando -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = TealPrimary,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
            Text("Leyendo el texto…", fontSize = 12.sp, color = Color.White)
        }

        is EstadoUi.Exito -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TEXTO DETECTADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gris400)
            Text(
                text = estado.datos,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Surface(
                onClick = { onUsarTexto(estado.datos) },
                shape = RoundedCornerShape(50),
                color = TealPrimary
            ) {
                Text(
                    text = "Buscar este medicamento",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        is EstadoUi.Vacio -> Text(
            text = "No se detectó texto. Acerca la cámara al nombre de la caja.",
            fontSize = 12.sp,
            color = Gris400,
            textAlign = TextAlign.Center
        )

        is EstadoUi.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = estado.mensaje,
                fontSize = 12.sp,
                color = Color(0xFFFCA5A5),
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onReintentar) {
                Text("Reintentar", color = TealSoft)
            }
        }
    }
}