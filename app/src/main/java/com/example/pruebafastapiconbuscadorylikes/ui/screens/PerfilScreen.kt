package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: androidx.navigation.NavController,
    onBack: () -> Unit
) {
    if (userId == "viewer") {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Debes iniciar sesión para ver tu perfil.")
        }
        return
    }
    val userData by viewModel.getUserData(userId).collectAsState(initial = null)

    val vistasPorReceta by viewModel.vistasPorReceta.collectAsState()
    val titulosRecetasVistas by viewModel.titulosVistas.collectAsState()
    val interacciones by viewModel.interacciones.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: ""
    LaunchedEffect(Unit) {
        viewModel.escucharVistasConTitulos(userId)
        viewModel.cargarInteracciones(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👤 Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
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
                Column {
                    Text("Nombre: ${user.nombre}", fontWeight = FontWeight.Bold)
                    Text("Email: ${user.email}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Roles actuales: ${user.roles.joinToString(", ")}")
                    if (user.vistas.containsKey("estado_validacion")) {
                        val estado = user.vistas["estado_validacion"]
                        Text("Estado de validación: $estado")
                    }
                    Spacer(modifier = Modifier.height(16.dp))


                    Spacer(modifier = Modifier.height(24.dp))
                    Divider()
/*
                    if (user.vistas.isNotEmpty()) {
                        Text("🍽 Recetas vistas (Firestore):")
                        Spacer(modifier = Modifier.height(8.dp))

                        user.vistas.forEach { (recetaId, cantidad) ->
                            val titulo = titulosRecetasVistas[recetaId] ?: "Receta $recetaId"
                            Text("• $titulo ($cantidad vista${if (cantidad != 1) "s" else ""})")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }*/


                    interacciones?.let { data ->
                        /*if (data.vistas.isNotEmpty()) {
                            Text("👀 Recetas vistas:")
                            data.vistas.forEach { receta ->
                                Text("• ${receta.titulo}")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }*/

                        if (data.likes.isNotEmpty()) {
                            Text("❤️ Recetas con like:")
                            data.likes.forEach { receta ->
                                Text("• ${receta.titulo} ")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Divider()
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    if (userEmail == "equipodecamcook@gmail.com") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("admin_validacion") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("👑 Panel Admin (Validar Solicitudes)")
                        }
                    }

                    var showDialog by remember { mutableStateOf(false) }

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
                                    Text("Sí, cerrar sesión")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDialog = false }) {
                                    Text("Cancelar")
                                }
                            },
                            title = { Text("¿Cerrar sesión?") },
                            text = { Text("Tu sesión se cerrará y deberás iniciar nuevamente.") }
                        )
                    }

                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🚪 Cerrar sesión", color = MaterialTheme.colorScheme.onErrorContainer)
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    if (isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }

                    message?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = it, color = Color.Green)
                    }
                }
            } ?: run {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun RoleToggleButton(
    role: String,
    hasRole: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text("Rol: $role", modifier = Modifier.weight(1f))

        if (hasRole) {
            Button(onClick = onRemove) {
                Text("Quitar")
            }
        } else {
            Button(onClick = onAdd) {
                Text("Agregar")
            }
        }
    }
}
