package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pruebafastapiconbuscadorylikes.model.Ingrediente
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProductosIngredientesScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: androidx.navigation.NavController,
    onBack: () -> Unit
) {

    // ✅ Aquí está la data class completa
    data class IngredienteSeleccionado(
        val ingrediente: Ingrediente,
        var precio: String = "",
        var cantidad: String = "",
        var unidad: String = ""
    )

    var ingredientes by remember { mutableStateOf(listOf<Ingrediente>()) }
    var seleccionados by remember { mutableStateOf(listOf<IngredienteSeleccionado>()) }
    var contacto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.recetas.collect { recetas ->
            val allIngredientes = recetas.flatMap { it.ingredientes ?: emptyList() }
            ingredientes = allIngredientes.distinctBy { it.nombre }
        }

        db.collection("proveedores").document(userId).get()
            .addOnSuccessListener { doc ->
                contacto = doc.getString("telefono") ?: ""
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🥕 Publicar ingredientes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Info inicial
            item {
                Text(
                    "Selecciona uno o más ingredientes de tus recetas para vender.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Lista de ingredientes para seleccionar
            items(ingredientes.size) { i ->
                val ing = ingredientes[i]
                val seleccionado = seleccionados.any { it.ingrediente.nombre == ing.nombre }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            seleccionados =
                                if (seleccionado) {
                                    seleccionados.filterNot { it.ingrediente.nombre == ing.nombre }
                                } else {
                                    seleccionados + IngredienteSeleccionado(ing)
                                }
                        },
                    colors = if (seleccionado)
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    else
                        CardDefaults.cardColors()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                ing.nombre ?: "Ingrediente sin nombre",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        if (seleccionado) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Seleccionado",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Asignar precio, cantidad y unidad a los seleccionados
            if (seleccionados.isNotEmpty()) {
                item {
                    Divider()
                    Text(
                        "Asigna precios y cantidades a los ingredientes seleccionados:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(seleccionados.size) { index ->
                    val sel = seleccionados[index]
                    val ing = sel.ingrediente

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = ing.nombre ?: "Ingrediente sin nombre",
                                style = MaterialTheme.typography.titleSmall
                            )

                            OutlinedTextField(
                                value = sel.cantidad,
                                onValueChange = { nuevo ->
                                    seleccionados = seleccionados.toMutableList().also {
                                        it[index] = it[index].copy(cantidad = nuevo)
                                    }
                                },
                                label = { Text("Cantidad") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = sel.unidad,
                                onValueChange = { nuevo ->
                                    seleccionados = seleccionados.toMutableList().also {
                                        it[index] = it[index].copy(unidad = nuevo)
                                    }
                                },
                                label = { Text("Unidad (gramo, kilo, libra...)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = sel.precio,
                                onValueChange = { nuevo ->
                                    seleccionados = seleccionados.toMutableList().also {
                                        it[index] = it[index].copy(precio = nuevo)
                                    }
                                },
                                label = { Text("💰 Precio") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Botón para limpiar selección
                item {
                    Button(
                        onClick = { seleccionados = emptyList() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("🗑 Limpiar selección")
                    }
                }
            }

            // Botón de publicar
            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (seleccionados.isEmpty()) {
                            mensaje = "⚠️ Selecciona al menos un ingrediente"
                            return@Button
                        }

                        if (seleccionados.any { it.precio.isBlank() || it.cantidad.isBlank() || it.unidad.isBlank() }) {
                            mensaje = "⚠️ Completa todos los campos antes de publicar"
                            return@Button
                        }

                        scope.launch(Dispatchers.IO) {
                            seleccionados.forEach { sel ->
                                val data = mapOf(
                                    "tipo" to "ingrediente",
                                    "nombre" to (sel.ingrediente.nombre ?: ""),
                                    "cantidad" to sel.cantidad,
                                    "unidad" to sel.unidad,
                                    "precio" to sel.precio,
                                    "contacto" to contacto
                                )

                                db.collection("proveedores")
                                    .document(userId)
                                    .collection("productos")
                                    .add(data)
                            }

                            withContext(Dispatchers.Main) {
                                mensaje = "✅ Ingredientes publicados correctamente"
                            }

                            delay(2000)

                            withContext(Dispatchers.Main) {
                                navController.navigate("marketplace") {
                                    popUpTo("formulario_productos_ingredientes") { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aceptar y volver al Marketplace")
                }
            }

            // Mensaje de estado
            mensaje?.let {
                item {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
