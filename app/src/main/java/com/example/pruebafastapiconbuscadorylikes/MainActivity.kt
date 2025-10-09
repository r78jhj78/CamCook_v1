package com.example.pruebafastapiconbuscadorylikes

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
import com.example.pruebafastapiconbuscadorylikes.auth.LoginScreen
import com.example.pruebafastapiconbuscadorylikes.auth.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("login") }
            var userId by remember { mutableStateOf("") }
            val viewModel: RecetasViewModel = viewModel()

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

                "recetas" -> RecetasScreen(viewModel, userId)
            }
        }
    }
}
