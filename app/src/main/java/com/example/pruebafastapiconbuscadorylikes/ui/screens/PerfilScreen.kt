package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.google.firebase.auth.FirebaseAuth
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: NavController,
    onBack: () -> Unit
) {
    val camcookColor = Color(0xFFFAA935)
    val accentColor = Color(0xFF8C7B6B)
    val backgroundColor = Color(0xFFF6F6F6)
    val beigeColor = Color(0xFFE5D9C5)

    if (userId == "viewer") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text("Debes iniciar sesión para ver tu perfil.", color = accentColor)
        }
        return
    }

    val userData by viewModel.getUserData(userId).collectAsState(initial = null)
    val interacciones by viewModel.interacciones.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: ""
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.escucharVistasConTitulos(userId)
        viewModel.cargarInteracciones(userId)
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text("👤 Perfil", color = Color.White, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = camcookColor)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            userData?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Imagen de perfil
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(camcookColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Imagen de perfil",
                            tint = accentColor,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    // Información del usuario
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(user.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accentColor)
                            Text(user.email, fontSize = 14.sp, color = accentColor)
                            Text("Roles: ${user.roles.joinToString(", ")}", fontSize = 14.sp, color = accentColor)

                            user.vistas["estado_validacion"]?.let {
                                Text("Estado de validación: $it", fontSize = 14.sp, color = accentColor)
                            }
                        }
                    }

                    // Recetas con like
                    interacciones?.likes?.takeIf { it.isNotEmpty() }?.let { likes ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("❤️ Recetas con like:", fontWeight = FontWeight.Bold, color = camcookColor)
                                likes.forEach { receta ->
                                    Text("• ${receta.titulo}", color = accentColor)
                                }
                            }
                        }
                    }

                    // Panel Admin
                    if (userEmail == "equipodecamcook@gmail.com") {
                        Button(
                            onClick = { navController.navigate("admin_validacion") },
                            colors = ButtonDefaults.buttonColors(containerColor = camcookColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Panel Admin", color = Color.White)
                        }
                    }

                    // Cerrar sesión
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar sesión", color = Color.White)
                    }

                    // Diálogo de confirmación
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    FirebaseAuth.getInstance().signOut()
                                    showDialog = false
                                    navController.navigate("login") {
                                        popUpTo("marketplace") { inclusive = true }
                                    }
                                }) {
                                    Text("Sí, cerrar sesión", color = camcookColor)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDialog = false }) {
                                    Text("Cancelar", color = accentColor)
                                }
                            },
                            title = { Text("¿Cerrar sesión?", color = accentColor) },
                            text = { Text("Tu sesión se cerrará y deberás iniciar nuevamente.", color = accentColor) },
                            containerColor = beigeColor
                        )
                    }
                }
            } ?: CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = camcookColor)
        }
    }
}


