package com.example.pruebafastapiconbuscadorylikes

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.pruebafastapiconbuscadorylikes.ui.theme.PruebaFastApiConBuscadorYLikesTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.example.pruebafastapiconbuscadorylikes.ui.screens.RecetasScreen
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pruebafastapiconbuscadorylikes.auth.LoginScreen
import com.example.pruebafastapiconbuscadorylikes.auth.RegisterScreen
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.navigation.Routes
import com.example.pruebafastapiconbuscadorylikes.ui.screens.DetalleRecetaScreen
import com.google.gson.Gson

/*
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("login") }
            var userId by remember { mutableStateOf("") }
            val viewModel: RecetasViewModel = viewModel()

            var recetaSeleccionada by remember { mutableStateOf<Receta?>(null) }

            when (currentScreen) {
                "login" -> LoginScreen(
                    onLoginSuccess = { uid ->
                        userId = uid
                        currentScreen = "recetas"
                    },
                    onNavigateToRegister = { currentScreen = "register" }
                )

                "register" -> RegisterScreen(
                    onRegisterSuccess = { uid ->
                        userId = uid
                        currentScreen = "recetas"
                    },
                    onNavigateToLogin = { currentScreen = "login" }
                )

                "recetas" -> RecetasScreen(
                    viewModel = viewModel,
                    userId = userId,
                    onRecetaClick = { receta ->
                        recetaSeleccionada = receta
                        currentScreen = "detalleReceta"
                    }
                )

                "detalleReceta" -> {
                    recetaSeleccionada?.let { receta ->
                        DetalleRecetaScreen(
                            receta = receta,
                            onBack = { currentScreen = "recetas" },
                            onLike = { viewModel.darLike(receta.id, userId) }
                        )
                    } ?: run {
                        currentScreen = "recetas"
                    }
                }
            }
        }
    }
}
*/
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: RecetasViewModel = viewModel()
            var userId by remember { mutableStateOf("") }

            NavHost(
                navController = navController,
                startDestination = Routes.LOGIN
            ) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLoginSuccess = { uid ->
                            userId = uid
                            navController.navigate(Routes.RECETAS)
                        },
                        onNavigateToRegister = {
                            navController.navigate(Routes.REGISTER)
                        }
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreen(
                        onRegisterSuccess = { uid ->
                            userId = uid
                            navController.navigate(Routes.RECETAS)
                        },
                        onNavigateToLogin = {
                            navController.popBackStack() // Vuelve al login
                        }
                    )
                }

                composable(Routes.RECETAS) {
                    RecetasScreen(
                        viewModel = viewModel,
                        userId = userId,
                        onRecetaClick = { receta ->
                            val recetaJson = Uri.encode(Gson().toJson(receta))
                            navController.navigate("${Routes.DETALLE_RECETA}/$recetaJson")
                        }
                    )
                }

                composable(
                    route = "${Routes.DETALLE_RECETA}/{recetaJson}",
                    arguments = listOf(navArgument("recetaJson") {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val recetaJson = backStackEntry.arguments?.getString("recetaJson")
                    val receta = Gson().fromJson(recetaJson, Receta::class.java)

                    DetalleRecetaScreen(
                        receta = receta,
                        onBack = { navController.popBackStack() },
                        onLike = { viewModel.darLike(receta.id, userId) }
                    )
                }
            }
        }
    }
}
