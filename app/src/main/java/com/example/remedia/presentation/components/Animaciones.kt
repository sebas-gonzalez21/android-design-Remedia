package com.example.remedia.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Entrada escalonada: fade + slide-up. `indice` retrasa cada elemento un poco (stagger). */
fun Modifier.entradaSuave(indice: Int = 0): Modifier = composed {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(minOf(indice, 6) * 70L)
        progreso.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
    }
    graphicsLayer {
        alpha = progreso.value
        translationY = (1f - progreso.value) * 36.dp.toPx()
    }
}