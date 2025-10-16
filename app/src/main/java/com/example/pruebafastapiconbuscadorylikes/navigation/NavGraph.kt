package com.example.pruebafastapiconbuscadorylikes.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pruebafastapiconbuscadorylikes.auth.LoginScreen
import com.example.pruebafastapiconbuscadorylikes.auth.RegisterScreen
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.example.pruebafastapiconbuscadorylikes.ui.screens.RecetasScreen
import com.example.pruebafastapiconbuscadorylikes.ui.screens.DetalleRecetaScreen
import com.google.gson.Gson

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RECETAS = "recetas"
    const val DETALLE_RECETA = "detalle_receta"
}

sealed class Screen(val route: String) {
    object Login : Screen(Routes.LOGIN)
    object Register : Screen(Routes.REGISTER)
    object Recetas : Screen(Routes.RECETAS)
    object DetalleReceta : Screen("${Routes.DETALLE_RECETA}/{recetaId}") {
        fun createRoute(recetaId: String) = "${Routes.DETALLE_RECETA}/$recetaId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // Login
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { uid ->
                    navController.navigate(Routes.RECETAS) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // Evita volver al login
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        // Registro
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { uid ->
                    navController.navigate(Routes.RECETAS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )


        }

        // Lista de recetas
        composable(Routes.RECETAS) {
            val viewModel: RecetasViewModel = viewModel()
            val userId = "usuario_demo" // O recupéralo desde un ViewModel/Storage/Args

            RecetasScreen(
                navController = navController,
                viewModel = viewModel,
                userId = userId,
                onRecetaClick = { receta ->
                    navController.navigate(Screen.DetalleReceta.createRoute(receta.id))
                },
                onGoToProfile = {
                    navController.navigate("perfil")
                },
                onGoToFavorites = {
                    navController.navigate("favoritos")
                },
                onGoToSettings = {
                    navController.navigate("ajustes")
                },
                onGoBackToInicio = {
                    navController.popBackStack()
                }
            )
        }


        // Detalle de receta
        composable(
            route = "detalle_receta/{recetaJson}",
            arguments = listOf(navArgument("recetaJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val recetaJson = backStackEntry.arguments?.getString("recetaJson")
            val receta = Gson().fromJson(recetaJson, Receta::class.java)

            DetalleRecetaScreen(
                receta = receta,
                onBack = { navController.popBackStack() },
                onLike = { /* lógica para dar like */ }
            )
        }

    }
}
