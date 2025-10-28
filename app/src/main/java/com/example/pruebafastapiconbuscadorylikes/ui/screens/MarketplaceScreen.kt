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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    userId: String,
    viewModel: com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel,
    onBack: () -> Unit,
    onGoToForm: () -> Unit
) {
    var proveedores by remember { mutableStateOf<List<Pair<Map<String, Any>, List<Map<String, Any>>>>>(emptyList()) }
    var proveedorSeleccionado by remember { mutableStateOf<Pair<Map<String, Any>, List<Map<String, Any>>>?>(null) }
    var cargando by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    /**
     * 🔄 LaunchedEffect: carga inicial y cada vez que el usuario vuelve al marketplace.
     * Esto evita que Compose se quede “pensando” cuando regresas de un formulario.
     */
    LaunchedEffect(userId) {
        cargando = true
        val proveedoresTemp = mutableListOf<Pair<Map<String, Any>, List<Map<String, Any>>>>()

        try {
            val proveedoresSnapshot = db.collection("proveedores")
                .whereEqualTo("estado_validacion", "aprobado")
                .get()
                .await()

            for (provDoc in proveedoresSnapshot.documents) {
                val provData = provDoc.data ?: continue
                val productosSnap = provDoc.reference.collection("productos").get().await()
                val productos = productosSnap.documents.mapNotNull { it.data }
                proveedoresTemp.add(provData to productos)
            }

            proveedores = proveedoresTemp.toList()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cargando = false
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
                    IconButton(onClick = {
                        onGoToForm()
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Vender")
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
        } else {
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
                            Text("🧺 ${prov["tipoProveedor"] ?: ""}")
                            Spacer(Modifier.height(6.dp))

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

                            if (productos.isEmpty()) {
                                Text("Sin productos aún.")
                            } else {
                                productos.take(3).forEach { prod ->
                                    Text("• ${prod["nombre"] ?: ""} - ${prod["precio"] ?: ""}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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

                    Text("🧺 ${prov["tipoProveedor"] ?: ""}")
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
                                    Text("• ${prod["nombre"] ?: ""} - ${prod["precio"] ?: ""}")
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
