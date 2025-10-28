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
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProveedorScreen(
    uid: String,
    onBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var proveedor by remember { mutableStateOf<Map<String, Any>?>(null) }
    var productos by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        db.collection("proveedores").document(uid).get().addOnSuccessListener { doc ->
            proveedor = doc.data
            db.collection("proveedores").document(uid)
                .collection("productos")
                .get()
                .addOnSuccessListener { snap ->
                    productos = snap.documents.mapNotNull { it.data }
                    cargando = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📄 Detalle del Proveedor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (cargando) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            proveedor?.let { prov ->
                Column(
                    Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🏪 Nombre: ${prov["nombre"]}")
                    Text("📞 Teléfono: ${prov["telefono"]}")
                    Text("🧺 Tipo de proveedor: ${prov["tipoProveedor"]}")
                    Text("📜 Descripción: ${prov["descripcion"]}")
                    Text("🌐 Imagen: ${prov["imagen"] ?: "No disponible"}")
                    Text("🕓 Estado actual: ${prov["estado_validacion"]}")

                    Divider()

                    Text("📦 Productos publicados:", style = MaterialTheme.typography.titleMedium)

                    if (productos.isEmpty()) {
                        Text("No tiene productos registrados.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(productos) { prod ->
                                Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                                    Column(Modifier.padding(8.dp)) {
                                        Text("🍽️ Nombre: ${prod["nombre"]}")
                                        Text("💰 Precio: ${prod["precio"]}")
                                        Text("📂 Tipo: ${prod["tipo"]}")
                                        if (prod["cantidad"] != null) Text("📦 Cantidad: ${prod["cantidad"]}")
                                        if (prod["unidad"] != null) Text("⚖️ Unidad: ${prod["unidad"]}")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                actualizarEstado(db, uid, "aprobado") {
                                    mensaje = if (it) "✅ Aprobado correctamente" else "❌ Error al aprobar"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Aprobar")
                        }

                        OutlinedButton(
                            onClick = {
                                actualizarEstado(db, uid, "rechazado") {
                                    mensaje = if (it) "🚫 Rechazado correctamente" else "❌ Error al rechazar"
                                }
                            }
                        ) {
                            Text("Rechazar")
                        }
                    }

                    if (mensaje.isNotEmpty()) {
                        Text(mensaje, color = MaterialTheme.colorScheme.primary)
                    }
                }
            } ?: Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("❌ No se encontró el proveedor.")
            }
        }
    }
}

private fun actualizarEstado(db: FirebaseFirestore, uid: String, estado: String, onResult: (Boolean) -> Unit) {
    db.collection("proveedores").document(uid)
        .update("estado_validacion", estado)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}
