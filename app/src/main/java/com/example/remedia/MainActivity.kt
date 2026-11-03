package com.example.remedia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.remedia.presentation.screens.DetalleScreen
import com.example.remedia.presentation.screens.EscanerScreen
import com.example.remedia.presentation.screens.InicioScreen
import com.example.remedia.presentation.screens.ResultadosScreen
import com.example.remedia.presentation.viewmodel.BusquedaViewModel
import com.example.remedia.presentation.viewmodel.DetalleViewModel
import com.example.remedia.presentation.viewmodel.EscanerViewModel
import com.example.remedia.ui.theme.RemediaTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // El nombre del tema depende del nombre del proyecto (<Proyecto>Theme en ui/theme/Theme.kt)
            RemediaTheme {
                RemediaNavHost()
            }
        }
    }
}

/** Rutas type-safe: cada pantalla es una clase/objeto serializable en vez de un String suelto. */
@Serializable
private object Inicio

@Serializable
private object Escaner

@Serializable
private data class Resultados(val consulta: String)

@Serializable
private data class Detalle(val expediente: String)

/**
 * Flujo (según el prototipo de Stitch):
 *   Inicio ──buscar──▶ Resultados ──tarjeta──▶ Detalle ──"Ver equivalentes"──▶ Resultados
 *   Inicio ──escanear──▶ Escáner ──texto──▶ Resultados
 *
 * Los estados ya salen de los ViewModels reales (BusquedaViewModel, DetalleViewModel,
 * EscanerViewModel), que llaman a MedicamentosRepository -> MedicamentosApi (Retrofit).
 * BusquedaViewModel se crea una sola vez aquí arriba para que Inicio y Resultados
 * compartan el mismo historial de "recientes".
 */
@Composable
private fun RemediaNavHost() {
    val navController = rememberNavController()
    val busquedaViewModel: BusquedaViewModel = viewModel()

    NavHost(navController = navController, startDestination = Inicio) {

        composable<Inicio> {
            val recientes by busquedaViewModel.recientes.collectAsState()
            InicioScreen(
                recientes = recientes,
                onBuscar = { consulta -> navController.navigate(Resultados(consulta)) },
                onEscanear = { navController.navigate(Escaner) }
            )
        }

        composable<Escaner> {
            val escanerViewModel: EscanerViewModel = viewModel()
            val estado by escanerViewModel.estado.collectAsState()
            EscanerScreen(
                estado = estado,
                onVolver = { navController.popBackStack() },
                onCapturar = { escanerViewModel.capturar() },
                onUsarTexto = { texto ->
                    navController.navigate(Resultados(texto)) {
                        popUpTo<Inicio>()
                    }
                }
            )
        }

        composable<Resultados> { entrada ->
            val ruta: Resultados = entrada.toRoute()
            LaunchedEffect(ruta.consulta) { busquedaViewModel.buscar(ruta.consulta) }
            val estado by busquedaViewModel.resultados.collectAsState()
            ResultadosScreen(
                titulo = ruta.consulta,
                estado = estado,
                onVolver = { navController.popBackStack() },
                onMedicamentoClick = { expediente ->
                    navController.navigate(Detalle(expediente))
                },
                onReintentar = { busquedaViewModel.buscar(ruta.consulta) }
            )
        }

        composable<Detalle> { entrada ->
            val ruta: Detalle = entrada.toRoute()
            val detalleViewModel: DetalleViewModel = viewModel()
            LaunchedEffect(ruta.expediente) { detalleViewModel.cargar(ruta.expediente) }
            val estado by detalleViewModel.estado.collectAsState()
            DetalleScreen(
                estado = estado,
                onVolver = { navController.popBackStack() },
                onVerEquivalentes = { principioActivo ->
                    navController.navigate(Resultados(principioActivo))
                },
                onReintentar = { detalleViewModel.cargar(ruta.expediente) }
            )
        }
    }
}
