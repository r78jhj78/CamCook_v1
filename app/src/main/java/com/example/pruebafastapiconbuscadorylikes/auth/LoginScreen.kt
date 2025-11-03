package com.example.pruebafastapiconbuscadorylikes.auth

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pruebafastapiconbuscadorylikes.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var passwordVisible by remember { mutableStateOf(false) }
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    var email by remember { mutableStateOf(sharedPref.getString("saved_email", "") ?: "") }

    val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("163492793985-qanbr305ue8jsuj3f9t6hiogqehs3dqi.apps.googleusercontent.com")
            .requestEmail()
            .build()
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            AuthManager.loginWithGoogle(
                credential,
                onSuccess = { uid ->
                    onLoginSuccess(uid)
                },
                onError = { msg ->
                    error = msg
                }
            )
        } catch (e: Exception) {
            error = "⚠️ Error: ${e.localizedMessage}"
        }
    }

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
            Text(
                text = "Bienvenido de nuevo 👋",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Accede a tu cuenta para continuar",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 📧 Email
            Text(
                "Correo electrónico",
                fontStyle = FontStyle.Italic,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() }, // elimina espacios al inicio y final
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

            // 🔒 Contraseña
            Text(
                "Contraseña",
                fontStyle = FontStyle.Italic,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFAA935),
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color.Black
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) {
                        painterResource(id = R.drawable.ic_visibility_off) // 👁️ cerrado
                    } else {
                        painterResource(id = R.drawable.ic_visibility) // 👁️ abierto
                    }
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = icon,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = Color.Gray
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ Botón de login
            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    if (trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
                        error = "⚠️ Por favor completa todos los campos"
                        return@Button
                    }
                    AuthManager.loginUser(
                        trimmedEmail,
                        trimmedPassword,
                        onSuccess = { uid ->
                            val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("saved_email", trimmedEmail)
                                apply()
                            }

                            onLoginSuccess(uid)
                        },
                        onError = { msg -> error = msg }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFAA935),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Iniciar sesión")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🔗 Google login
            OutlinedButton(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Text("Continuar con Google")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🔁 Recuperar contraseña
            TextButton(onClick = {
                val trimmedEmail = email.trim()
                if (trimmedEmail.isEmpty()) {
                    error = "⚠️ Por favor, ingresa tu correo"
                } else {
                    AuthManager.resetPassword(trimmedEmail) {
                        error = "📩 Revisa tu correo para restablecer la contraseña"
                    }
                }
            }) {
                Text("¿Olvidaste tu contraseña?", color = Color(0xFFFAA935))
            }

            // 🆕 Registro
            TextButton(onClick = onNavigateToRegister) {
                Text("¿No tienes cuenta? Regístrate", color = Color.Black)
            }

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
