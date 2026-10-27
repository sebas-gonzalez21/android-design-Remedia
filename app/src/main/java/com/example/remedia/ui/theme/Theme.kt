package com.example.remedia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val RemediaColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Superficie,
    primaryContainer = TealSoft,
    onPrimaryContainer = TealDark,
    secondary = TealDark,
    onSecondary = Superficie,
    background = Fondo,
    onBackground = TextoPrincipal,
    surface = Superficie,
    onSurface = TextoPrincipal,
    surfaceVariant = Fondo,
    onSurfaceVariant = TextoSecundario,
    outline = Borde,
    error = Vencido,
    onError = Superficie,
    errorContainer = VencidoFondo,
    onErrorContainer = Vencido
)

@Composable
fun RemediaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RemediaColorScheme,
        typography = Typography,
        content = content
    )
}