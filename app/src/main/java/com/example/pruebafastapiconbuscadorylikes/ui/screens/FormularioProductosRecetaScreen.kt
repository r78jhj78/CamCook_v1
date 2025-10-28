package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProductosRecetaScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: androidx.navigation.NavController,
    onBack: () -> Unit
) {
    data class RecetaSeleccionada(val receta: Receta, var precio: String = "")

    val recetas by viewModel.recetas.collectAsState(initial = emptyList())
    var seleccionadas by remember { mutableStateOf(listOf<RecetaSeleccionada>()) }
    var contacto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        try {
            val doc = db.collection("proveedores").document(userId).get().await()
            contacto = doc.getString("telefono") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🍽 Publicar recetas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (cargando) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Selecciona una o más recetas para vender.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(recetas.size) { i ->
                val receta = recetas[i]
                val seleccionada = seleccionadas.any { it.receta.id == receta.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            seleccionadas =
                                if (seleccionada)
                                    seleccionadas.filterNot { it.receta.id == receta.id }
                                else
                                    seleccionadas + RecetaSeleccionada(receta)
                        },
                    colors = if (seleccionada)
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    else
                        CardDefaults.cardColors()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(receta.imagen_final_url),
                            contentDescription = null,
                            modifier = Modifier
                                .size(70.dp)
                                .padding(end = 8.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(receta.titulo, style = MaterialTheme.typography.titleSmall)
                            Text(receta.descripcion, maxLines = 1)
                        }
                        if (seleccionada) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Seleccionada",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (seleccionadas.isNotEmpty()) {
                item {
                    Divider()
                    Text(
                        "Completa los precios para las recetas seleccionadas:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(seleccionadas.size) { index ->
                    val sel = seleccionadas[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(sel.receta.titulo)
                            OutlinedTextField(
                                value = sel.precio,
                                onValueChange = { nuevo ->
                                    val limpio = nuevo.trim().replace(",", ".")
                                    if (limpio.isEmpty() || limpio.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                        seleccionadas = seleccionadas.toMutableList().also {
                                            it[index] = it[index].copy(precio = limpio)
                                        }
                                    }
                                },
                                label = { Text("💰 Precio (Bs)") },
                                placeholder = { Text("Ej: 25.00") },
                                singleLine = true
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = { seleccionadas = emptyList() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("🗑 Limpiar selección")
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (seleccionadas.isEmpty()) {
                            mensaje = "⚠️ Selecciona al menos una receta"
                            return@Button
                        }

                        if (seleccionadas.any { it.precio.isBlank() }) {
                            mensaje = "⚠️ Completa los precios antes de publicar"
                            return@Button
                        }

                        scope.launch(Dispatchers.IO) {
                            try {
                                val proveedorRef = db.collection("proveedores").document(userId)
                                seleccionadas.forEach { sel ->
                                    val data = mapOf(
                                        "tipo" to "receta",
                                        "recetaId" to sel.receta.id,
                                        "nombre" to sel.receta.titulo,
                                        "precio" to sel.precio,
                                        "imagen" to sel.receta.imagen_final_url,
                                        "contacto" to contacto
                                    )
                                    proveedorRef.collection("productos").add(data).await()
                                }

                                withContext(Dispatchers.Main) {
                                    mensaje = "✅ Recetas publicadas correctamente"
                                }

                                delay(1500)
                                withContext(Dispatchers.Main) {
                                    navController.popBackStack()
                                    navController.navigate("marketplace")
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    mensaje = "❌ Error al publicar: ${e.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aceptar y volver al Marketplace")
                }
            }

            mensaje?.let {
                item {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
