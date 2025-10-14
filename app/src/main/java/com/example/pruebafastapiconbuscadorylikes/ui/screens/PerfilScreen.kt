package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

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

                    Text("Likes: ${user.likes.size}")
                    Text("Vistas: ${user.vistas.size}")

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Agregar o quitar roles:")

                    // Botón para Chef
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

                    // Botón para Proveedor
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
