package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pruebafastapiconbuscadorylikes.model.Ingrediente
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientesScreen(
    navController: NavController,
    viewModel: RecetasViewModel,
    onGoBackToInicio: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val ingredientes by viewModel.ingredientes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.escucharTodosIngredientes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧂 Buscar Ingrediente") },
                navigationIcon = {
                    IconButton(onClick = onGoBackToInicio) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar ingrediente...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.buscarIngrediente(query) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔍 Buscar")
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(ingredientes) { ing ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(ing.nombre ?: "Ingrediente", fontWeight = FontWeight.Bold)
                            Text("${ing.cantidad ?: "-"} ${ing.unidad ?: ""}")
                        }
                    }
                }
            }
        }
    }
}
