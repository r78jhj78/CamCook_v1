package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    userId: String,
    viewModel: com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel,
    onBack: () -> Unit,
    onGoToForm: () -> Unit
) {
    // Lista de proveedores y sus productos
    var proveedores by remember { mutableStateOf<List<Pair<Map<String, Any>, List<Map<String, Any>>>>>(emptyList()) }
    var proveedorSeleccionado by remember { mutableStateOf<Pair<Map<String, Any>, List<Map<String, Any>>>?>(null) }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("proveedores").whereEqualTo("estado_validacion", "aprobado").addSnapshotListener { snapshot, _ ->
            snapshot ?: return@addSnapshotListener
            val temp = mutableListOf<Pair<Map<String, Any>, List<Map<String, Any>>>>()

            snapshot.documents.forEach { doc ->
                val provData = doc.data ?: return@forEach

                doc.reference.collection("productos").get()
                    .addOnSuccessListener { productosSnap ->
                        val productos = productosSnap.documents.mapNotNull { it.data }
                        temp.add(provData to productos)
                        proveedores = temp.toList()
                    }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🛍 Marketplace") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onGoToForm) {
                        Icon(Icons.Filled.Add, contentDescription = "Vender")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(proveedores) { (prov, productos) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { proveedorSeleccionado = prov to productos },
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            prov["nombre"]?.toString() ?: "Proveedor sin nombre",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("🧺 ${prov["tipoProducto"] ?: ""}")
                        Spacer(Modifier.height(6.dp))

                        // Imagen del proveedor (si existe)
                        val img = prov["imagen"]?.toString()
                        if (!img.isNullOrEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(img),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                        }

                        // Mostrar algunos productos
                        if (productos.isEmpty()) {
                            Text("Sin productos aún.")
                        } else {
                            productos.take(3).forEach { prod ->
                                Text("• ${prod["nombre"]} - ${prod["precio"]}")
                            }
                        }
                    }
                }
            }
        }
    }

    // 🪟 Popup con info completa del proveedor
    proveedorSeleccionado?.let { (prov, productos) ->
        AlertDialog(
            onDismissRequest = { proveedorSeleccionado = null },
            confirmButton = {
                TextButton(onClick = { proveedorSeleccionado = null }) {
                    Text("Cerrar")
                }
            },
            title = {
                Text(prov["nombre"]?.toString() ?: "Proveedor")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val img = prov["imagen"]?.toString()
                    if (!img.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(img),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }

                    Text("🧺 ${prov["tipoProducto"] ?: ""}")
                    Text(prov["descripcion"]?.toString() ?: "")

                    Divider(Modifier.padding(vertical = 8.dp))
                    Text("📦 Productos disponibles:", style = MaterialTheme.typography.titleMedium)

                    if (productos.isEmpty()) {
                        Text("Este proveedor aún no tiene productos registrados.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            items(productos) { prod ->
                                Column {
                                    Text("• ${prod["nombre"]} - ${prod["precio"]}")
                                    val prodImg = prod["imagen"]?.toString()
                                    if (!prodImg.isNullOrEmpty()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(prodImg),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
