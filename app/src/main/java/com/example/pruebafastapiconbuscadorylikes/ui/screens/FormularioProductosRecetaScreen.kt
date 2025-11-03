package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafastapiconbuscadorylikes.data.manager.ValidacionManager
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val camcookColor = Color(0xFFFAA935)
val accentColor = Color(0xFF8C7B6B)
val backgroundColor = Color(0xFFF6F6F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProductosRecetaScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: NavController,
    onBack: () -> Unit
) {

    class RecetaSeleccionada(val receta: Receta, precioInicial: String = "") {
        val precio = mutableStateOf(precioInicial)
    }

    val recetas by viewModel.recetas.collectAsState(initial = emptyList())
    val seleccionadas = remember { mutableStateListOf<RecetaSeleccionada>() }
    var contacto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") } // 🔍 Buscador
    val filteredRecetas = recetas.filter {
        it.titulo.contains(searchQuery, ignoreCase = true) ||
                it.descripcion.contains(searchQuery, ignoreCase = true)
    }

    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        try {
            val doc = db.collection("proveedores").document(userId).get().await()
            contacto = doc.getString("telefono") ?: ""
        } catch (_: Exception) {
        } finally {
            cargando = false
        }
        viewModel.cargarRecetas()
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🍽 Publicar recetas", color = camcookColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = camcookColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
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
                CircularProgressIndicator(color = camcookColor)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // 🔍 Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar receta...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = camcookColor,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = camcookColor
                )
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (filteredRecetas.isEmpty()) {
                    item {
                        Text("No se encontraron recetas", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                }

                items(filteredRecetas) { receta ->
                    val seleccionada = seleccionadas.find { it.receta.id == receta.id }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (seleccionada != null) {
                                    seleccionadas.remove(seleccionada)
                                } else {
                                    seleccionadas.add(RecetaSeleccionada(receta))
                                }
                            }
                            .border(
                                width = if (seleccionada != null) 2.dp else 0.dp,
                                color = if (seleccionada != null) Color(0xFFFFEB3B) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = rememberAsyncImagePainter(receta.mainImageUrl.ifEmpty { receta.imagenUrl }),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(75.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        receta.titulo,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        receta.descripcion,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                if (seleccionada != null) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = camcookColor, modifier = Modifier.size(28.dp))
                                }
                            }

                            seleccionada?.let { sel ->
                                Spacer(Modifier.height(8.dp))
                                Column {
                                    OutlinedTextField(
                                        value = sel.precio.value,
                                        onValueChange = { nuevo ->
                                            val limpio = nuevo.trim().replace(",", ".")
                                            if (limpio.isEmpty() || limpio.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                                val valor = limpio.toDoubleOrNull()
                                                if (valor == null || valor <= 10000) {
                                                    sel.precio.value = limpio
                                                }
                                            }
                                        },
                                        label = { Text("💰 Precio (Bs)", color = accentColor) },
                                        placeholder = { Text("Ej: 25.00") },
                                        singleLine = true,
                                        supportingText = { Text("Máx: 10000 Bs", color = Color.Gray) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFFEB3B),
                                            unfocusedBorderColor = Color(0xFFFFEB3B),
                                            cursorColor = camcookColor
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    IconButton(
                                        onClick = { seleccionadas.remove(sel) },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }

                // Botón publicar
                // Botón publicar
                item {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (seleccionadas.isEmpty()) {
                                mensaje = "⚠️ Selecciona al menos una receta"
                                return@Button
                            }
                            for (sel in seleccionadas) {
                                val precio = sel.precio.value.toDoubleOrNull()
                                if (precio == null || precio <= 0) {
                                    mensaje = "⚠️ Precio inválido para '${sel.receta.titulo}'"
                                    return@Button
                                }
                            }

                            scope.launch {
                                try {
                                    val proveedorDoc = db.collection("proveedores").document(userId).get().await()
                                    if (!proveedorDoc.exists()) {
                                        mensaje = "⚠️ Primero debes registrarte como proveedor"
                                        return@launch
                                    }

                                    val estado = proveedorDoc.getString("estado_validacion") ?: "pendiente"
                                    if (estado != "aprobado") {
                                        mensaje = "⏳ Tu cuenta aún no está aprobada"
                                        return@launch
                                    }

                                    val proveedorRef = db.collection("proveedores").document(userId)

                                    // 🔹 CORREGIDO: referencia a sel.receta en todas las líneas
                                    seleccionadas.forEach { sel ->
                                        val data = mapOf(
                                            "tipo" to "receta",
                                            "recetaId" to sel.receta.id,
                                            "nombre" to sel.receta.titulo.take(30),
                                            "precio" to sel.precio.value,
                                            "imagen" to sel.receta.mainImageUrl.ifEmpty { sel.receta.imagenUrl },
                                            "contacto" to contacto
                                        )
                                        proveedorRef.collection("productos").add(data).await()
                                    }

                                    ValidacionManager.asegurarRolProveedor(userId)
                                    mensaje = "✅ Recetas publicadas correctamente"
                                    delay(1500)
                                    navController.popBackStack()
                                    navController.navigate("marketplace")
                                } catch (e: Exception) {
                                    mensaje = "❌ Error al publicar: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = camcookColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Publicar recetas en el Marketplace", color = Color.White)
                    }
                }

                // Mensaje feedback
                mensaje?.let {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    it.startsWith("✅") -> Color(0xFFE8F5E9)
                                    it.startsWith("❌") -> Color(0xFFFFEBEE)
                                    else -> Color(0xFFFFF8E1)
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Text(it, modifier = Modifier.padding(12.dp), color = accentColor)
                        }
                    }
                }
            }
        }
    }
}

