package com.example.remedia.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remedia.ui.theme.Borde
import com.example.remedia.ui.theme.Fondo
import com.example.remedia.ui.theme.Gris400
import com.example.remedia.ui.theme.TealPrimary
import com.example.remedia.ui.theme.TextoSecundario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Muestra la primera URL candidata que responda realmente una imagen HTTPS.
 *
 * No confiamos en la URL que sugiere Gemini: puede estar inventada o haber
 * cambiado. Por eso se descarta cualquier respuesta que no sea 2xx, cuyo
 * `Content-Type` no empiece por `image/`, o que no se pueda decodificar.
 * Si ninguna sirve, se muestra un placeholder en vez de una imagen rota.
 */
@Composable
fun ImagenProducto(imagenes: List<String>, nombre: String) {
    var bitmap by remember(imagenes) { mutableStateOf<Bitmap?>(null) }
    var cargando by remember(imagenes) { mutableStateOf(imagenes.isNotEmpty()) }

    LaunchedEffect(imagenes) {
        bitmap = null
        cargando = imagenes.isNotEmpty()
        val resultado = withContext(Dispatchers.IO) {
            imagenes.take(MAX_URLS).firstNotNullOfOrNull(::descargarImagen)
        }
        bitmap = resultado
        cargando = false
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Fondo)
            .border(1.dp, Borde, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Imagen del envase de $nombre",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            cargando -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = TealPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.height(8.dp))
                Text("Buscando la imagenâ€¦", fontSize = 12.sp, color = TextoSecundario)
            }

            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Medication,
                    contentDescription = null,
                    tint = Gris400,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Imagen no disponible",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoSecundario
                )
                Text(
                    text = "No encontramos una imagen verificada de este envase.",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    fontSize = 11.sp,
                    color = Gris400
                )
            }
        }
    }
}

private fun descargarImagen(url: String): Bitmap? {
    if (!url.startsWith("https://", ignoreCase = true)) return null

    val conexion = runCatching { URL(url).openConnection() as? HttpURLConnection }
        .getOrNull() ?: return null
    conexion.connectTimeout = 4_000
    conexion.readTimeout = 8_000
    conexion.instanceFollowRedirects = true

    try {
        conexion.connect()
        if (conexion.responseCode !in 200..299) return null

        val tipo = conexion.contentType?.substringBefore(';')?.trim()?.lowercase()
            ?: return null
        if (!tipo.startsWith("image/")) return null

        val bytes = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        conexion.inputStream.use { entrada ->
            var total = 0
            while (total < MAX_BYTES) {
                val leidos = entrada.read(buffer)
                if (leidos <= 0) break
                bytes.write(buffer, 0, leidos)
                total += leidos
            }
        }
        val datos = bytes.toByteArray()

        val medida = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(datos, 0, datos.size, medida)
        if (medida.outWidth <= 0 || medida.outHeight <= 0) return null

        val muestra = maxOf(
            1,
            (medida.outWidth + LADO_MAX - 1) / LADO_MAX,
            (medida.outHeight + LADO_MAX - 1) / LADO_MAX
        )
        return BitmapFactory.decodeByteArray(
            datos,
            0,
            datos.size,
            BitmapFactory.Options().apply { inSampleSize = muestra }
        )
    } catch (e: Exception) {
        return null
    } finally {
        conexion.disconnect()
    }
}

private const val MAX_URLS = 3
private const val MAX_BYTES = 3 * 1024 * 1024
private const val LADO_MAX = 720
