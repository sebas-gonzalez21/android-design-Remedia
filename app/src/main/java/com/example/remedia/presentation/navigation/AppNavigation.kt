package com.example.remedia.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.remedia.BuildConfig
import com.example.remedia.data.local.RecientesDataStore
import com.example.remedia.data.local.RecientesDataSource
import com.example.remedia.data.network.GeminiClient
import com.example.remedia.data.repository.EnriquecedorFichaGemini
import com.example.remedia.data.repository.EnriquecedorFichaVacio
import com.example.remedia.data.repository.MedicamentosRepository
import com.example.remedia.data.repository.MedicamentosRepositoryImpl
import com.example.remedia.data.repository.NormalizadorGemini
import com.example.remedia.data.repository.NormalizadorHeuristico
import com.example.remedia.presentation.screens.DetalleScreen
import com.example.remedia.presentation.screens.EscanerScreen
import com.example.remedia.presentation.screens.InicioScreen
import com.example.remedia.presentation.screens.ResultadosScreen
import com.example.remedia.presentation.viewmodel.BusquedaViewModel
import com.example.remedia.presentation.viewmodel.DetalleViewModel
import com.example.remedia.presentation.viewmodel.EscanerViewModel
import com.example.remedia.presentation.viewmodel.MedicamentosViewModelFactory

/**
 * Grafo de navegaciÃ³n (segÃºn el prototipo de Stitch):
 *   Inicio â”€â”€buscarâ”€â”€â–¶ Resultados â”€â”€tarjetaâ”€â”€â–¶ Detalle â”€â”€"Ver equivalentes"â”€â”€â–¶ Resultados
 *   Inicio â”€â”€escanearâ”€â”€â–¶ EscÃ¡ner â”€â”€textoâ”€â”€â–¶ Resultados
 *
 * Repositorio y fuente de recientes (DataStore) se crean una sola vez y se inyectan
 * con una fÃ¡brica. BusquedaViewModel se comparte entre Inicio y Resultados
 * (historial persistente y cachÃ© de bÃºsquedas); el DetalleViewModel tambiÃ©n se
 * comparte para no recargar una ficha ya vista.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    repository: MedicamentosRepository = remember {
        MedicamentosRepositoryImpl(
            normalizador = if (GeminiClient.habilitado) {
                NormalizadorGemini(
                    api = GeminiClient.api,
                    modelo = BuildConfig.GEMINI_MODEL,
                    clave = BuildConfig.GEMINI_API_KEY
                )
            } else {
                NormalizadorHeuristico
            },
            enriquecedor = if (GeminiClient.habilitado) {
                EnriquecedorFichaGemini(
                    api = GeminiClient.api,
                    modelo = BuildConfig.GEMINI_MODEL,
                    clave = BuildConfig.GEMINI_API_KEY
                )
            } else {
                EnriquecedorFichaVacio
            }
        )
    }
) {
    val applicationContext = LocalContext.current.applicationContext
    val recientesSource: RecientesDataSource = remember(applicationContext) {
        RecientesDataStore(applicationContext)
    }
    val factory = remember(repository, recientesSource) {
        MedicamentosViewModelFactory(repository, recientesSource)
    }
    val busquedaViewModel: BusquedaViewModel = viewModel(factory = factory)
    val detalleViewModel: DetalleViewModel = viewModel(factory = factory)
    val escanerViewModel: EscanerViewModel = viewModel()

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
                viewModel = escanerViewModel,
                onVolver = { navController.popBackStack() },
                onUsarTexto = { texto ->
                    navController.navigate(
                        Destination.Resultados(texto.consulta, texto.contexto)
                    ) {
                        popUpTo<Destination.Inicio>()
                    }
                }
            )
        }

        composable<Destination.Resultados> { entrada ->
            val ruta: Destination.Resultados = entrada.toRoute()
            ResultadosScreen(
                consulta = ruta.consulta,
                contexto = ruta.contexto,
                viewModel = busquedaViewModel,
                onVolver = { navController.popBackStack() },
                onNuevaBusqueda = { consulta ->
                    // Reemplaza el resultado anterior para que "atrÃ¡s" vuelva al Inicio.
                    navController.navigate(Destination.Resultados(consulta)) {
                        popUpTo<Destination.Inicio>()
                    }
                },
                onMedicamentoClick = { expediente ->
                    navController.navigate(Destination.Detalle(expediente))
                }
            )
        }

        composable<Destination.Detalle> { entrada ->
            val ruta: Destination.Detalle = entrada.toRoute()
            DetalleScreen(
                expediente = ruta.expediente,
                viewModel = detalleViewModel,
                onVolver = { navController.popBackStack() },
                onVerEquivalentes = { principioActivo ->
                    navController.navigate(Destination.Resultados(principioActivo))
                }
            )
        }
    }
}
