package com.example.walletpet.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.walletpet.core.ui.components.BarraInferior
import com.example.walletpet.core.ui.theme.Crema
import com.example.walletpet.core.ui.theme.FuenteTexto
import com.example.walletpet.presentation.auth.AuthGate
import com.example.walletpet.presentation.gastos.GastosScreen
import com.example.walletpet.presentation.gastos.ReporteDetalleScreen
import com.example.walletpet.presentation.gastos.ReportesScreen
import com.example.walletpet.presentation.inicio.HomeScreen
import com.example.walletpet.presentation.metas.MetasScreen
import com.example.walletpet.presentation.splash.SplashScreen
import com.example.walletpet.presentation.tienda.TiendaScreen

/** Punto de entrada de la interfaz: splash → huella → pantallas con barra inferior. */
@Composable
fun WalletPetApp() {
    var mostrandoSplash by rememberSaveable { mutableStateOf(true) }

    // La letra de todo el texto de la app (los títulos usan otra, ver Fuentes.kt)
    ProvideTextStyle(TextStyle(fontFamily = FuenteTexto)) {
        if (mostrandoSplash) {
            SplashScreen { mostrandoSplash = false }
        } else {
            AuthGate { AppNavGraph() }
        }
    }
}

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val pestana = Routes.pestanaDe(entrada?.destination?.route)

    Box(Modifier.fillMaxSize().background(Crema)) {
        NavHost(
            navController = nav,
            startDestination = Routes.INICIO,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.INICIO) {
                HomeScreen(
                    onIrATienda = { nav.irAPestana(Routes.TIENDA) },
                    onIrAMetas = { nav.irAPestana(Routes.METAS) }
                )
            }
            composable(Routes.TIENDA) { TiendaScreen() }
            composable(Routes.METAS) { MetasScreen() }
            composable(Routes.GASTOS) {
                GastosScreen(onVerReportes = { nav.navigate(Routes.REPORTES) })
            }
            composable(Routes.REPORTES) {
                ReportesScreen(
                    onAbrir = { clave -> nav.navigate(Routes.reporte(clave)) },
                    onVolver = { nav.popBackStack() }
                )
            }
            composable(
                Routes.REPORTE_DETALLE,
                arguments = listOf(navArgument("clave") { type = NavType.StringType })
            ) { entry ->
                ReporteDetalleScreen(
                    clave = entry.arguments?.getString("clave").orEmpty(),
                    onVolver = { nav.popBackStack() }
                )
            }
        }

        BarraInferior(
            seleccion = pestana,
            onSelect = { nav.irAPestana(Routes.pestanas[it]) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/** Cambia de pestaña sin apilar pantallas repetidas (el botón atrás regresa a Inicio). */
private fun NavController.irAPestana(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
