package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val camcookColor = Color(0xFFFAA935)
    val accentColor = Color(0xFF8C7B6B)
    val backgroundColor = Color(0xFFF6F6F6)
    val cardColor = Color(0xFFFFF3E0)

    var query by remember { mutableStateOf("") }
    val ingredientes by viewModel.ingredientes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.escucharTodosIngredientes()
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🧂 Buscar Ingrediente",
                        color = accentColor,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onGoBackToInicio) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = camcookColor
                )
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
                label = { Text("Buscar ingrediente...", color = accentColor) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = camcookColor,
                    unfocusedBorderColor = accentColor,
                    cursorColor = accentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.buscarIngrediente(query) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = camcookColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🔍 Buscar", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(ingredientes) { ing ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text(
                                ing.nombre ?: "Ingrediente",
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${ing.cantidad ?: "-"} ${ing.unidad ?: ""}",
                                color = accentColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
