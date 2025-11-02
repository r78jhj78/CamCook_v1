package com.example.pruebafastapiconbuscadorylikes.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pruebafastapiconbuscadorylikes.R
import com.example.pruebafastapiconbuscadorylikes.data.model.EmailSender
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val buttonHeight by animateDpAsState(targetValue = if (loading) 45.dp else 50.dp, label = "buttonHeight")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 🎨 Decoración visual
        Canvas(modifier = Modifier
            .size(180.dp)
            .align(Alignment.TopEnd)
        ) {
            drawCircle(
                color = Color(0xFFFAA935),
                center = Offset(size.width, 0f),
                radius = size.width / 1.5f
            )
        }

        Canvas(modifier = Modifier
            .size(180.dp)
            .align(Alignment.BottomStart)
        ) {
            drawCircle(
                color = Color(0xFFFAA935),
                center = Offset(0f, size.height),
                radius = size.width / 1.5f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.cook_cam_logo),
                contentDescription = "Logo CamCook",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )

            Crossfade(targetState = loading, label = "titleFade") { isLoading ->
                Text(
                    text = if (isLoading) "Procesando..." else "¡Crea tu cuenta 🎉!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            AnimatedVisibility(visible = !loading) {
                Text(
                    text = "Únete y empieza a explorar",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(visible = !loading) {
                Column {
                    // Nombre
                    Text(
                        "Nombre de usuario",
                        fontStyle = FontStyle.Italic,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it.trimStart().trimEnd() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFAA935),
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    Text(
                        "Correo electrónico",
                        fontStyle = FontStyle.Italic,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trimStart().trimEnd() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFAA935),
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contraseña
                    Text(
                        "Contraseña segura",
                        fontStyle = FontStyle.Italic,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFAA935),
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de registro
            Button(
                onClick = {
                    val trimmedNombre = nombre.trim()
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()

                    if (trimmedNombre.isEmpty() || trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
                        error = "⚠️ Por favor completa todos los campos"
                        return@Button
                    }

                    if (!isPasswordStrong(trimmedPassword)) {
                        error = "⚠️ La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un símbolo"
                        return@Button
                    }

                    loading = true
                    AuthManager.registerUser(trimmedNombre, trimmedEmail, trimmedPassword,
                        onSuccess = { uid ->
                            scope.launch {
                                EmailSender.sendWelcomeEmail(context, trimmedEmail, trimmedNombre)
                            }
                            loading = false
                            onRegisterSuccess(uid)
                        },
                        onError = { msg ->
                            error = msg
                            loading = false
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFAA935),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (loading) "Creando..." else "Registrarse")
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = !loading) {
                TextButton(onClick = onNavigateToLogin) {
                    Text("¿Ya tienes cuenta? Inicia sesión", color = Color.Black)
                }
            }

            AnimatedVisibility(visible = error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

fun isPasswordStrong(password: String): Boolean {
    val regex = Regex("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}\$")
    return regex.matches(password)
}
