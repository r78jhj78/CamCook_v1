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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    userId: String,
    viewModel: RecetasViewModel,
    onBack: () -> Unit
) {
    val userData by viewModel.getUserData(userId).collectAsState(initial = null)

    val vistasPorReceta by viewModel.vistasPorReceta.collectAsState()
    val titulosRecetasVistas by viewModel.titulosVistas.collectAsState()
    val interacciones by viewModel.interacciones.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

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
                        if (data.vistas.isNotEmpty()) {
                            Text("👀 Recetas vistas:")
                            data.vistas.forEach { receta ->
                                Text("• ${receta.titulo}")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

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
                    Text("Agregar o quitar roles:")


                    RoleToggleButton(
                        role = "chef",
                        hasRole = user.roles.contains("chef"),
                        onAdd = {
                            isLoading = true
                            viewModel.agregarRol(userId, "chef") {
                                isLoading = false
                                message = "Rol 'chef' añadido."
                            }
                        },
                        onRemove = {
                            isLoading = true
                            viewModel.quitarRol(userId, "chef") {
                                isLoading = false
                                message = "Rol 'chef' removido."
                            }
                        }
                    )


                    RoleToggleButton(
                        role = "proveedor",
                        hasRole = user.roles.contains("proveedor"),
                        onAdd = {
                            isLoading = true
                            viewModel.agregarRol(userId, "proveedor") {
                                isLoading = false
                                message = "Rol 'proveedor' añadido."
                            }
                        },
                        onRemove = {
                            isLoading = true
                            viewModel.quitarRol(userId, "proveedor") {
                                isLoading = false
                                message = "Rol 'proveedor' removido."
                            }
                        }
                    )

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
