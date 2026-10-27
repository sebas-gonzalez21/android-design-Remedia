package com.example.remedia.presentation.navigation

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
fun RemediaNavHost() {
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
