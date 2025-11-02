package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Colores corporativos CamCook
val camcookColor = Color(0xFFFAA935)
val accentColor = Color(0xFF8C7B6B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProductosRecetaScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: NavController,
    onBack: () -> Unit
) {
    // Estructura auxiliar para manejar selección + precio
    data class RecetaSeleccionada(val receta: Receta, var precio: String = "")

    val recetas by viewModel.recetas.collectAsState(initial = emptyList())
    var seleccionadas by remember { mutableStateOf(listOf<RecetaSeleccionada>()) }
    var contacto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    // 🔹 Carga inicial de datos de proveedor y recetas
    LaunchedEffect(userId) {
        try {
            val doc = db.collection("proveedores").document(userId).get().await()
            contacto = doc.getString("telefono") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cargando = false
        }

        viewModel.cargarRecetas()
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

            // 🔹 Listado de recetas
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
                    colors = CardDefaults.cardColors(
                        containerColor = if (seleccionada) camcookColor.copy(alpha = 0.2f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(receta.imagen_final_url),
                            contentDescription = null,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(receta.titulo, style = MaterialTheme.typography.titleMedium, color = accentColor)
                            Text(receta.descripcion, maxLines = 1, color = Color.Gray)
                        }
                        if (seleccionada) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Seleccionada",
                                tint = camcookColor
                            )
                        }
                    }
                }
            }

            // 🔹 Si hay recetas seleccionadas, mostrar precios
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
                                        val valor = limpio.toDoubleOrNull()
                                        if (valor == null || valor <= 10000) {
                                            seleccionadas = seleccionadas.toMutableList().also {
                                                it[index] = it[index].copy(precio = limpio)
                                            }
                                        }
                                    }
                                },
                                label = { Text("💰 Precio (Bs)", color = accentColor) },
                                placeholder = { Text("Ej: 25.00") },
                                singleLine = true,
                                supportingText = { Text("Máx: 10000 Bs", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = camcookColor,
                                    unfocusedBorderColor = accentColor,
                                    cursorColor = camcookColor
                                )
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = { seleccionadas = emptyList() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Limpiar selección", color = Color.White)
                    }
                }
            }

            // 🔹 Botón principal para publicar
            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (seleccionadas.isEmpty()) {
                            mensaje = "⚠️ Selecciona al menos una receta"
                            return@Button
                        }

                        for (sel in seleccionadas) {
                            val precio = sel.precio.toDoubleOrNull()
                            if (precio == null || precio <= 0) {
                                mensaje = "⚠️ Precio inválido para '${sel.receta.titulo}'"
                                return@Button
                            }
                        }

                        scope.launch(Dispatchers.IO) {
                            try {
                                // 🔹 Verificar que el proveedor existe y está aprobado
                                val proveedorDoc = db.collection("proveedores").document(userId).get().await()
                                if (!proveedorDoc.exists()) {
                                    withContext(Dispatchers.Main) {
                                        mensaje = "⚠️ Primero debes registrarte como proveedor antes de publicar recetas"
                                    }
                                    return@launch
                                }

                                val estado = proveedorDoc.getString("estado_validacion") ?: "pendiente"
                                if (estado != "aprobado") {
                                    withContext(Dispatchers.Main) {
                                        mensaje = "⏳ Tu cuenta de proveedor aún no está aprobada"
                                    }
                                    return@launch
                                }

                                // 🔹 Subir productos (recetas con precios)
                                val proveedorRef = db.collection("proveedores").document(userId)
                                seleccionadas.forEach { sel ->
                                    val data = mapOf(
                                        "tipo" to "receta",
                                        "recetaId" to sel.receta.id,
                                        "nombre" to sel.receta.titulo.take(30),
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
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    mensaje = "❌ Error al publicar: ${e.message ?: "verifica conexión o permisos"}"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = camcookColor)
                ) {
                    Text("Aceptar y volver al Marketplace", color = Color.White)
                }
            }

            // 🔹 Mensaje de estado
            mensaje?.let {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                it.startsWith("✅") -> Color(0xFFE8F5E9)
                                it.startsWith("❌") -> Color(0xFFFFEBEE)
                                else -> Color(0xFFFFF8E1)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(12.dp),
                            color = accentColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
