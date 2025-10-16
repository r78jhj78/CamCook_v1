package com.example.pruebafastapiconbuscadorylikes.ui.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pruebafastapiconbuscadorylikes.R
import com.example.pruebafastapiconbuscadorylikes.navigation.Routes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(0f) }

    // 🟠 Animaciones de los círculos
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val circleOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )
    val circleOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    // 🕒 Animación del logo
    LaunchedEffect(true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = { OvershootInterpolator(2f).getInterpolation(it) }
            )
        )
        delay(3000)
        navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E7)) // Fondo crema
    ) {
        // 🎨 Círculos decorativos animados
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            drawCircle(
                color = Color(0xFFF9A825),
                radius = 460f,
                center = Offset(80f, height - 150f + circleOffset1)
            )
            drawCircle(
                color = Color(0xFFF9A825),
                radius = 760f,
                center = Offset(120f, height - 2550f + circleOffset1)
            )
            drawCircle(
                color = Color(0xFFF9A825),
                radius = 220f,
                center = Offset(width - 100f, 120f + circleOffset2)
            )
            drawCircle(
                color = Color(0xFFF9A825),
                radius = 180f,
                center = Offset(width / 2, height - 100f - circleOffset2)
            )
        }

        // 📸 Logo y texto centrado
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.cook_cam_logo),
                contentDescription = "Cook Cam Logo",
                modifier = Modifier
                    .size(220.dp)
                    .scale(scale.value)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cook Cam",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFD84315) // rojo-naranja del logo
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = Color(0xFFD84315))
        }
    }
}

