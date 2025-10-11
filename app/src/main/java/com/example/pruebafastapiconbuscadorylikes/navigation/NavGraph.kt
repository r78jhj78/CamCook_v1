package com.example.pruebafastapiconbuscadorylikes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pruebafastapiconbuscadorylikes.auth.LoginScreen
import com.example.pruebafastapiconbuscadorylikes.auth.RegisterScreen
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.example.pruebafastapiconbuscadorylikes.ui.screens.RecetasScreen
import com.example.pruebafastapiconbuscadorylikes.ui.screens.DetalleRecetaScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.gson.Gson

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RECETAS = "recetas"
    const val DETALLE_RECETA = "detalle_receta"
}
