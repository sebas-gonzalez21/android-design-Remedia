package com.example.remedia

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.remedia.presentation.screens.DetalleScreen
import com.example.remedia.presentation.screens.EscanerScreen
import com.example.remedia.presentation.screens.InicioScreen
import com.example.remedia.presentation.screens.ResultadosScreen
import com.example.remedia.presentation.viewmodel.BusquedaViewModel
import com.example.remedia.presentation.viewmodel.DetalleViewModel
import com.example.remedia.presentation.viewmodel.EscanerViewModel
import com.example.remedia.ui.theme.RemediaTheme

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

    NavHost(navController = navController, startDestination = Rutas.INICIO) {

        composable(Rutas.INICIO) {
            val recientes by busquedaViewModel.recientes.collectAsState()
            InicioScreen(
                recientes = recientes,
                onBuscar = { consulta -> navController.navigate(Rutas.resultados(consulta)) },
                onEscanear = { navController.navigate(Rutas.ESCANER) }
            )
        }

        composable(Rutas.ESCANER) {
            val escanerViewModel: EscanerViewModel = viewModel()
            val estado by escanerViewModel.estado.collectAsState()
            EscanerScreen(
                estado = estado,
                onVolver = { navController.popBackStack() },
                onCapturar = { escanerViewModel.capturar() },
                onUsarTexto = { texto ->
                    navController.navigate(Rutas.resultados(texto)) {
                        popUpTo(Rutas.INICIO)
                    }
                }
            )
        }

        composable(
            route = Rutas.RESULTADOS,
            arguments = listOf(navArgument(Rutas.ARG_CONSULTA) { type = NavType.StringType })
        ) { entrada ->
            val consulta = entrada.arguments?.getString(Rutas.ARG_CONSULTA).orEmpty()
            LaunchedEffect(consulta) { busquedaViewModel.buscar(consulta) }
            val estado by busquedaViewModel.resultados.collectAsState()
            ResultadosScreen(
                titulo = consulta,
                estado = estado,
                onVolver = { navController.popBackStack() },
                onMedicamentoClick = { expediente ->
                    navController.navigate(Rutas.detalle(expediente))
                },
                onReintentar = { busquedaViewModel.buscar(consulta) }
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument(Rutas.ARG_EXPEDIENTE) { type = NavType.StringType })
        ) { entrada ->
            val expediente = entrada.arguments?.getString(Rutas.ARG_EXPEDIENTE).orEmpty()
            val detalleViewModel: DetalleViewModel = viewModel()
            LaunchedEffect(expediente) { detalleViewModel.cargar(expediente) }
            val estado by detalleViewModel.estado.collectAsState()
            DetalleScreen(
                estado = estado,
                onVolver = { navController.popBackStack() },
                onVerEquivalentes = { principioActivo ->
                    navController.navigate(Rutas.resultados(principioActivo))
                },
                onReintentar = { detalleViewModel.cargar(expediente) }
            )
        }
    }
}

/** Nombres de las rutas. Una sola fuente de verdad para no escribir strings sueltos. */
private object Rutas {
    const val INICIO = "inicio"
    const val ESCANER = "escaner"

    const val ARG_CONSULTA = "consulta"
    const val RESULTADOS = "resultados/{$ARG_CONSULTA}"
    fun resultados(consulta: String) = "resultados/${Uri.encode(consulta)}"

    const val ARG_EXPEDIENTE = "expediente"
    const val DETALLE = "detalle/{$ARG_EXPEDIENTE}"
    fun detalle(expediente: String) = "detalle/${Uri.encode(expediente)}"
}
