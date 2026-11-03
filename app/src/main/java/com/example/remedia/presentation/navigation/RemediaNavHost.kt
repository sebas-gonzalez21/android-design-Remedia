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
 * Flujo (según el prototipo de Stitch):
 *   Inicio ──buscar──▶ Resultados ──tarjeta──▶ Detalle ──"Ver equivalentes"──▶ Resultados
 *   Inicio ──escanear──▶ Escáner ──texto──▶ Resultados
 *
 * Cada pantalla recibe su ViewModel como parámetro (con viewModel() por defecto) y
 * observa su propio estado con collectAsState() — ver presentation/screens.
 * busquedaViewModel se crea una sola vez aquí arriba (como parámetro con default)
 * para que Inicio y Resultados compartan el mismo historial de "recientes".
 */
@Composable
fun RemediaNavHost(
    navController: NavHostController = rememberNavController(),
    busquedaViewModel: BusquedaViewModel = viewModel()
) {
    NavHost(navController = navController, startDestination = Inicio) {

        composable<Inicio> {
            InicioScreen(
                viewModel = busquedaViewModel,
                onBuscar = { consulta -> navController.navigate(Resultados(consulta)) },
                onEscanear = { navController.navigate(Escaner) }
            )
        }

        composable<Escaner> {
            EscanerScreen(
                onVolver = { navController.popBackStack() },
                onUsarTexto = { texto ->
                    navController.navigate(Resultados(texto)) {
                        popUpTo<Inicio>()
                    }
                }
            )
        }

        composable<Resultados> { entrada ->
            val ruta: Resultados = entrada.toRoute()
            ResultadosScreen(
                consulta = ruta.consulta,
                viewModel = busquedaViewModel,
                onVolver = { navController.popBackStack() },
                onMedicamentoClick = { expediente ->
                    navController.navigate(Detalle(expediente))
                }
            )
        }

        composable<Detalle> { entrada ->
            val ruta: Detalle = entrada.toRoute()
            DetalleScreen(
                expediente = ruta.expediente,
                onVolver = { navController.popBackStack() },
                onVerEquivalentes = { principioActivo ->
                    navController.navigate(Resultados(principioActivo))
                }
            )
        }
    }
}
