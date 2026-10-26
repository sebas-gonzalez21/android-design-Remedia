package com.example.remedia.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.remedia.presentation.model.EstadoUi
import com.example.remedia.presentation.model.MockData
import com.example.remedia.presentation.screens.DetalleScreen
import com.example.remedia.presentation.screens.EscanerScreen
import com.example.remedia.presentation.screens.InicioScreen
import com.example.remedia.presentation.screens.ResultadosScreen

/**
 * Flujo (según el prototipo de Stitch):
 *   Inicio ──buscar──▶ Resultados ──tarjeta──▶ Detalle ──"Ver equivalentes"──▶ Resultados
 *   Inicio ──escanear──▶ Escáner ──texto──▶ Resultados
 *
 * Aquí solo se conectan pantallas. Los estados salen de MockData (temporal);
 * cuando lleguen los ViewModels se cambia MockData.xxx por viewModel.estado.collectAsState().
 */
@Composable
fun RemediaNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rutas.INICIO) {

        composable(Rutas.INICIO) {
            InicioScreen(
                recientes = EstadoUi.Exito(MockData.recientes),
                onBuscar = { consulta -> navController.navigate(Rutas.resultados(consulta)) },
                onEscanear = { navController.navigate(Rutas.ESCANER) }
            )
        }

        composable(Rutas.ESCANER) {
            EscanerScreen(
                estado = EstadoUi.Exito("Medicamento Ejemplo A 500 mg"),
                onVolver = { navController.popBackStack() },
                onCapturar = { /* lo conectará el EscanerViewModel */ },
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
            ResultadosScreen(
                titulo = consulta,
                estado = MockData.resultadosPara(consulta),
                onVolver = { navController.popBackStack() },
                onMedicamentoClick = { expediente ->
                    navController.navigate(Rutas.detalle(expediente))
                }
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument(Rutas.ARG_EXPEDIENTE) { type = NavType.StringType })
        ) { entrada ->
            val expediente = entrada.arguments?.getString(Rutas.ARG_EXPEDIENTE).orEmpty()
            DetalleScreen(
                estado = MockData.buscarPorExpediente(expediente),
                onVolver = { navController.popBackStack() },
                onVerEquivalentes = { principioActivo ->
                    navController.navigate(Rutas.resultados(principioActivo))
                }
            )
        }
    }
}
