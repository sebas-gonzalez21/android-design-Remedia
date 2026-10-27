package com.example.remedia.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remedia.presentation.components.BotonCircular
import com.example.remedia.presentation.components.ChipFiltro
import com.example.remedia.presentation.components.EstadoContenido
import com.example.remedia.presentation.components.MedicamentoCard
import com.example.remedia.presentation.model.EstadoUi
import com.example.remedia.presentation.model.MedicamentoUi
import com.example.remedia.ui.theme.Borde
import com.example.remedia.ui.theme.Fondo
import com.example.remedia.ui.theme.Gris400
import com.example.remedia.ui.theme.Superficie
import com.example.remedia.ui.theme.TealPrimary
import com.example.remedia.ui.theme.TextoPrincipal
import com.example.remedia.ui.theme.TextoSecundario

private enum class Filtro(val etiqueta: String) {
    TODOS("Todos"), VENTA_LIBRE("Venta libre"), CON_FORMULA("Con fórmula")
}

/**
 * P3 · Resultados (estilo Stitch). Lista con LazyColumn (nunca Column).
 * El filtro es estado temporal de la UI, por eso vive en remember.
 */
@Composable
fun ResultadosScreen(
    titulo: String,
    estado: EstadoUi<List<MedicamentoUi>>,
    onVolver: () -> Unit,
    onMedicamentoClick: (String) -> Unit,
    onReintentar: () -> Unit = {}
) {
    var filtro by remember { mutableStateOf(Filtro.TODOS) }

    Scaffold(containerColor = Fondo) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Fondo)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
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
                        color = TextoPrincipal
                    )
                    Text(text = "  •  Resultados", fontSize = 12.sp, color = TextoSecundario)
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    color = Superficie,
                    border = BorderStroke(1.dp, Borde)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = Gris400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = titulo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextoPrincipal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Filtro.entries.forEach { opcion ->
                        ChipFiltro(
                            texto = opcion.etiqueta,
                            seleccionado = filtro == opcion,
                            onClick = { filtro = opcion }
                        )
                    }
                }
            }

            EstadoContenido(
                estado = estado,
                mensajeVacio = "No encontramos medicamentos con esa búsqueda.",
                onReintentar = onReintentar
            ) { lista ->
                val visibles = when (filtro) {
                    Filtro.TODOS -> lista
                    Filtro.VENTA_LIBRE -> lista.filter { it.ventaLibre }
                    Filtro.CON_FORMULA -> lista.filter { !it.ventaLibre }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${visibles.size} resultados para “$titulo”",
                            fontSize = 12.sp,
                            color = TextoSecundario
                        )
                    }
                    itemsIndexed(visibles, key = { _, med -> med.expediente }) { indice, medicamento ->
                        MedicamentoCard(
                            medicamento = medicamento,
                            onClick = { onMedicamentoClick(medicamento.expediente) },
                            indice = indice
                        )
                    }
                }
            }
        }
    }
}