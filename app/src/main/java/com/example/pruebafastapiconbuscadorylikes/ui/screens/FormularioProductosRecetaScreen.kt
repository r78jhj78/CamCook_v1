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
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProductosRecetaScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: androidx.navigation.NavController,
    onBack: () -> Unit
) {
    data class RecetaSeleccionada(
        val receta: Receta,
        var precio: String = ""
    )

    var recetas by remember { mutableStateOf(listOf<Receta>()) }
    var recetasSeleccionadas by remember { mutableStateOf(listOf<RecetaSeleccionada>()) }
    var contacto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    // 🔄 Cargar recetas desde ViewModel
    LaunchedEffect(Unit) {
        viewModel.recetas.collect { recetas = it }
    }

    // 🔄 Cargar contacto del proveedor
    LaunchedEffect(Unit) {
        db.collection("proveedores").document(userId).get()
            .addOnSuccessListener { doc ->
                contacto = doc.getString("telefono") ?: ""
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

        // 🌍 Scroll general para toda la vista
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text(
                    "Selecciona una o más recetas para vender.\nToca una receta para marcarla o desmarcarla.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 📋 Lista de recetas disponibles
            items(recetas.size) { i ->
                val receta = recetas[i]
                val seleccionada = recetasSeleccionadas.any { it.receta.id == receta.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            recetasSeleccionadas =
                                if (seleccionada) {
                                    recetasSeleccionadas.filterNot { it.receta.id == receta.id }
                                } else {
                                    recetasSeleccionadas + RecetaSeleccionada(receta)
                                }
                        },
                    colors = if (seleccionada)
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
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

            // 🔢 Campos dinámicos para las recetas seleccionadas
            if (recetasSeleccionadas.isNotEmpty()) {
                item {
                    Divider()
                    Text(
                        "Completa los precios para las recetas seleccionadas:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(recetasSeleccionadas.size) { index ->
                    val seleccion = recetasSeleccionadas[index]
                    val receta = seleccion.receta

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = receta.titulo,
                                style = MaterialTheme.typography.titleSmall
                            )

                            OutlinedTextField(
                                value = seleccion.precio,
                                onValueChange = { nuevo ->
                                    recetasSeleccionadas = recetasSeleccionadas.toMutableList().also {
                                        it[index] = it[index].copy(precio = nuevo)
                                    }
                                },
                                label = { Text("💰 Precio para ${receta.titulo}") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = { recetasSeleccionadas = emptyList() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text("🗑 Limpiar selección")
                    }
                }
            }

            // 🚀 Botón final
            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (recetasSeleccionadas.isEmpty()) {
                            mensaje = "⚠️ Selecciona al menos una receta"
                            return@Button
                        }

                        if (recetasSeleccionadas.any { it.precio.isBlank() }) {
                            mensaje = "⚠️ Completa los precios antes de publicar"
                            return@Button
                        }

                        scope.launch(Dispatchers.IO) {
                            recetasSeleccionadas.forEach { sel ->
                                val data = mapOf(
                                    "tipo" to "receta",
                                    "recetaId" to sel.receta.id,
                                    "nombre" to sel.receta.titulo,
                                    "precio" to sel.precio,
                                    "imagen" to sel.receta.imagen_final_url,
                                    "contacto" to contacto
                                )
                                db.collection("proveedores")
                                    .document(userId)
                                    .collection("productos")
                                    .add(data)
                            }

                            withContext(Dispatchers.Main) {
                                mensaje = "✅ Recetas publicadas correctamente"
                            }

                            delay(2000)

                            withContext(Dispatchers.Main) {
                                navController.navigate("marketplace") {
                                    popUpTo("formulario_productos_receta") { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aceptar y volver al Marketplace")
                }
            }

            // 🗨️ Mensaje informativo
            if (mensaje != null) {
                item {
                    Text(
                        mensaje!!,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


