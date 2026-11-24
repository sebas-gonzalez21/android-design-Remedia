package com.example.remedia.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.remedia.presentation.viewmodel.EscanerViewModel
import com.example.remedia.presentation.viewmodel.EstadoUi
import com.example.remedia.presentation.viewmodel.TextoEscaneado
import com.example.remedia.ui.theme.Gris400
import com.example.remedia.ui.theme.TealPrimary
import com.example.remedia.ui.theme.TealSoft
import com.example.remedia.ui.theme.TextoSecundario
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * P2 · Escáner (estilo Stitch) con cámara real.
 *
 * CameraX entrega el preview y frames al analizador; ML Kit lee el texto y el
 * ViewModel elige la mejor línea. El obturador confirma el texto leído y
 * "Buscar este medicamento" navega a Resultados con esa consulta.
 *
 * La cámara se enlaza al ciclo de vida de la pantalla y se libera al salir:
 * analizador, recognizer, executor y use cases. El permiso se pide al entrar
 * y se puede volver a pedir desde "Reintentar".
 */
@Composable
fun EscanerScreen(
    viewModel: EscanerViewModel,
    onVolver: () -> Unit,
    onUsarTexto: (TextoEscaneado) -> Unit
) {
    val contexto = LocalContext.current
    val cicloVida = LocalLifecycleOwner.current
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val textoDetectado by viewModel.textoDetectado.collectAsStateWithLifecycle()

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var intento by remember { mutableStateOf(0) }
    var editando by remember { mutableStateOf(false) }
    var borrador by remember { mutableStateOf("") }

    val pedalPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
        if (concedido) viewModel.onPermisoConcedido() else viewModel.onPermisoDenegado()
    }

    LaunchedEffect(Unit) {
        if (!permisoConcedido) pedalPermiso.launch(Manifest.permission.CAMERA)
    }

    val vistaCamara = remember(contexto) {
        PreviewView(contexto).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(cicloVida, vistaCamara, permisoConcedido, intento, viewModel) {
        if (!permisoConcedido) return@DisposableEffect onDispose {}

        val executor = Executors.newSingleThreadExecutor()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val analisis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        var proveedor: ProcessCameraProvider? = null
        var vistaPrevia: Preview? = null
        var controlCamara: CameraControl? = null
        var desmontado = false

        analisis.setAnalyzer(executor) { imagen ->
            val mediaImage = imagen.image
            if (mediaImage == null) {
                imagen.close()
                return@setAnalyzer
            }
            val entrada = InputImage.fromMediaImage(mediaImage, imagen.imageInfo.rotationDegrees)
            recognizer.process(entrada)
                .addOnSuccessListener { resultado ->
                    viewModel.onTextoReconocido(resultado.textBlocks.map { bloque -> bloque.text })
                }
                .addOnFailureListener { error -> viewModel.onCamaraError(error.message) }
                // Sin cerrar el ImageProxy, CameraX deja de entregar frames.
                .addOnCompleteListener { imagen.close() }
        }

        val futuro = ProcessCameraProvider.getInstance(contexto)
        futuro.addListener({
            // La pantalla pudo salir mientras se resolvía el provider: no enlazar nada.
            if (!desmontado) {
                try {
                    proveedor = futuro.get()
                    val preview = Preview.Builder().build().also { vista ->
                        vista.setSurfaceProvider(vistaCamara.surfaceProvider)
                    }
                    vistaPrevia = preview
                    val camara = proveedor?.bindToLifecycle(
                        cicloVida,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analisis
                    )
                    controlCamara = camara?.cameraControl

                    // Toque para enfocar: el nombre suele estar en una zona pequeña
                    // de la caja y el autoenfoque de escena no siempre la elige.
                    vistaCamara.setOnTouchListener { _, evento ->
                        if (evento.action != MotionEvent.ACTION_DOWN) {
                            return@setOnTouchListener false
                        }
                        val punto = vistaCamara.meteringPointFactory
                            .createPoint(evento.x, evento.y)
                        val enfoque = FocusMeteringAction.Builder(
                            punto,
                            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                        )
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                        controlCamara?.startFocusAndMetering(enfoque)
                        true
                    }
                    viewModel.onPermisoConcedido()
                } catch (e: Exception) {
                    viewModel.onCamaraError(e.message)
                }
            }
        }, ContextCompat.getMainExecutor(contexto))

        onDispose {
            desmontado = true
            vistaCamara.setOnTouchListener(null)
            controlCamara = null
            analisis.clearAnalyzer()
            recognizer.close()
            executor.shutdownNow()
            vistaPrevia?.let { proveedor?.unbind(it, analisis) }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0B1120))) {
        AndroidView(factory = { vistaCamara }, modifier = Modifier.fillMaxSize())
        // Velo oscuro para que el texto y la retícula se lean sobre cualquier caja.
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.38f)))

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
                    text = "Toca el nombre del medicamento para enfocar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))

            Reticula()

            Spacer(Modifier.height(20.dp))

            ResultadoEscaneo(
                estado = estado,
                textoVista = textoDetectado,
                onUsarTexto = onUsarTexto,
                onReintentar = {
                    viewModel.reiniciar()
                    if (permisoConcedido) {
                        intento++ // vuelve a enlazar la cámara tras un error
                    } else {
                        pedalPermiso.launch(Manifest.permission.CAMERA)
                    }
                }
            )

            Spacer(Modifier.weight(1f))

            Surface(
                onClick = {
                    if (permisoConcedido) viewModel.capturar() else {
                        pedalPermiso.launch(Manifest.permission.CAMERA)
                    }
                },
                enabled = permisoConcedido && textoDetectado.isNotBlank(),
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(4.dp, Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .background(
                            if (textoDetectado.isNotBlank()) TealPrimary else Color(0xFF334155),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Confirmar texto detectado",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                onClick = {
                    borrador = textoDetectado
                    editando = true
                },
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

        if (editando) {
            AlertDialog(
                onDismissRequest = { editando = false },
                title = { Text("Corregir el nombre") },
                text = {
                    Column {
                        Text(
                            text = "Si el OCR leyó mal la caja, escribe aquí el nombre correcto.",
                            fontSize = 12.sp,
                            color = TextoSecundario
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = borrador,
                            onValueChange = { borrador = it },
                            label = { Text("Nombre del medicamento") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (borrador.isNotBlank()) {
                                viewModel.actualizarTexto(borrador)
                                viewModel.capturar()
                            }
                            editando = false
                        }
                    ) { Text("Buscar") }
                },
                dismissButton = {
                    TextButton(onClick = { editando = false }) { Text("Cancelar") }
                }
            )
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
    estado: EstadoUi<TextoEscaneado>,
    textoVista: String,
    onUsarTexto: (TextoEscaneado) -> Unit,
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
                text = estado.datos.consulta,
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

        is EstadoUi.Vacio -> if (textoVista.isBlank()) {
            Text(
                text = "No se detectó texto. Acerca la cámara al nombre de la caja.",
                fontSize = 12.sp,
                color = Gris400,
                textAlign = TextAlign.Center
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LISTO PARA CONFIRMAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gris400)
                Text(
                    text = textoVista,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Toca el obturador para buscar este nombre.",
                    fontSize = 12.sp,
                    color = Gris400,
                    textAlign = TextAlign.Center
                )
            }
        }

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
