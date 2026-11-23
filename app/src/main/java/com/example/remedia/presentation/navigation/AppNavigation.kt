package com.example.remedia.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.remedia.presentation.screens.DetalleScreen
import com.example.remedia.presentation.screens.EscanerScreen
import com.example.remedia.presentation.screens.InicioScreen
import com.example.remedia.presentation.screens.ResultadosScreen
import com.example.remedia.presentation.viewmodel.BusquedaViewModel

/**
 * Grafo de navegación (según el prototipo de Stitch):
 *   Inicio ──buscar──▶ Resultados ──tarjeta──▶ Detalle ──"Ver equivalentes"──▶ Resultados
 *   Inicio ──escanear──▶ Escáner ──texto──▶ Resultados
 *
 * Cada pantalla observa su propio estado con collectAsState() (ver presentation/screens).
 * Aquí se crea una sola vez BusquedaViewModel para que Inicio y Resultados
 * compartan el mismo historial de "recientes".
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    busquedaViewModel: BusquedaViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Inicio
    ) {

        composable<Destination.Inicio> {
            InicioScreen(
                viewModel = busquedaViewModel,
                onBuscar = { consulta -> navController.navigate(Destination.Resultados(consulta)) },
                onEscanear = { navController.navigate(Destination.Escaner) }
            )
        }

        composable<Destination.Escaner> {
            EscanerScreen(
                onVolver = { navController.popBackStack() },
                onUsarTexto = { texto ->
                    navController.navigate(Destination.Resultados(texto)) {
                        popUpTo<Destination.Inicio>()
                    }
                }
            )
        }

        composable<Destination.Resultados> { entrada ->
            val ruta: Destination.Resultados = entrada.toRoute()
            ResultadosScreen(
                consulta = ruta.consulta,
                viewModel = busquedaViewModel,
                onVolver = { navController.popBackStack() },
                onMedicamentoClick = { expediente ->
                    navController.navigate(Destination.Detalle(expediente))
                }
            )
        }

        composable<Destination.Detalle> { entrada ->
            val ruta: Destination.Detalle = entrada.toRoute()
            DetalleScreen(
                expediente = ruta.expediente,
                onVolver = { navController.popBackStack() },
                onVerEquivalentes = { principioActivo ->
                    navController.navigate(Destination.Resultados(principioActivo))
                }
            )
        }
    }
}