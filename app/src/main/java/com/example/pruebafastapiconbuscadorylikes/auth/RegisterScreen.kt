package com.example.pruebafastapiconbuscadorylikes.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {

        // 🟠 Círculo superior izquierdo
        Canvas(modifier = Modifier
            .size(200.dp)
            .align(Alignment.TopStart)
        ) {
            drawCircle(
                color = Color(0xFFF4A836),
                center = this.center.copy(x = 0f),
                radius = size.width * 0.75f
            )
        }

        // 🟠 Círculo medio derecho
        Canvas(modifier = Modifier
            .size(160.dp)
            .align(Alignment.CenterEnd)
        ) {
            drawCircle(
                color = Color(0xFFF4A836),
                center = this.center.copy(x = size.width),
                radius = size.width * 0.6f
            )
        }

        // 🟠 Círculo inferior izquierdo
        Canvas(modifier = Modifier
            .size(200.dp)
            .align(Alignment.BottomStart)
        ) {
            drawCircle(
                color = Color(0xFFF4A836),
                center = this.center.copy(x = 0f, y = size.height),
                radius = size.width * 0.75f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 🧾 Título
            Text(
                text = "Crear Cuenta",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🧑 Usuario
            Text(
                text = "Usuario",
                fontStyle = FontStyle.Italic,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 📧 Correo
            Text(
                text = "Correo",
                fontStyle = FontStyle.Italic,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔒 Contraseña
            Text(
                text = "Contraseña",
                fontStyle = FontStyle.Italic,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ✔️ Botón registrarse
            Button(
                onClick = {
                    AuthManager.registerUser(nombre, email, password,
                        onSuccess = { uid -> onRegisterSuccess(uid) },
                        onError = { msg -> error = msg }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF4A836),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Registrarse")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🔗 Enlace a login
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "¿Ya tienes cuenta? Inicia sesión",
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

