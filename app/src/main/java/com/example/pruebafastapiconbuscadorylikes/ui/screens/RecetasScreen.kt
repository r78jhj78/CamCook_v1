package com.example.pruebafastapiconbuscadorylikes.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel

@Composable
fun RecetasScreen(viewModel: RecetasViewModel, userId: String) {
    val recetas by viewModel.recetas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var query by remember { mutableStateOf("") }

    // 🔹 Escuchar recetas en tiempo real al montar la pantalla
    LaunchedEffect(Unit) {
        viewModel.escucharTodasRecetas()
    }
    Column(Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar receta...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(onClick = { viewModel.buscarRecetas(query) }, modifier = Modifier.fillMaxWidth()) {
            Text("Buscar")
        }

        if (isLoading) {
            CircularProgressIndicator(Modifier.padding(16.dp))
        } else {
            LazyColumn {
                items(recetas) { receta ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text(text = receta.titulo, style = MaterialTheme.typography.titleMedium)
                            Text(text = receta.descripcion, style = MaterialTheme.typography.bodyMedium)
                            Text("👍 ${receta.likes}   👀 ${receta.popup_clicks}")

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    viewModel.darLike(receta.id, userId)
                                }) {
                                    val dioLike = receta.liked_by.containsKey(userId)
                                    Text(if (dioLike) "💔 Quitar Like" else "❤️ Like")
                                }
                                TextButton(onClick = {
                                    viewModel.agregarVista(receta.id, userId) // 🔹 aquí
                                }) {
                                    Text("👁️ Ver")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}