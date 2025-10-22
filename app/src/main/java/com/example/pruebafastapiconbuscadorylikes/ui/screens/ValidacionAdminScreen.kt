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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidacionAdminScreen(
    onBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val adminEmail = "equipodecamcook@gmail.com"

    var pendientes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }

    // 🔒 Restringir acceso solo al correo del admin
    if (currentUser?.email != adminEmail) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("🚫 No tienes permisos para acceder a esta pantalla.")
        }
        return
    }

    // 🔁 Escucha en tiempo real solo los proveedores pendientes
    LaunchedEffect(Unit) {
        db.collection("proveedores")
            .whereEqualTo("estado_validacion", "pendiente")
            .addSnapshotListener { snapshot, _ ->
                snapshot ?: return@addSnapshotListener
                pendientes = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    data + mapOf("uid" to doc.id)
                }
                cargando = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👑 Panel de Validación de Proveedores") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        ) {
            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (pendientes.isEmpty()) {
                Text("✅ No hay solicitudes pendientes.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pendientes) { prov ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🏪 ${prov["nombre"] ?: "Sin nombre"}", style = MaterialTheme.typography.titleMedium)
                                Text("🧺 Tipo: ${prov["tipoProducto"] ?: "No especificado"}")
                                Text("📧 Usuario: ${prov["userId"] ?: "Desconocido"}")

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            aprobarProveedor(db, prov["uid"].toString()) {
                                                mensaje = if (it) "✅ Proveedor aprobado" else "❌ Error al aprobar"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Aprobar")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            rechazarProveedor(db, prov["uid"].toString()) {
                                                mensaje = if (it) "🚫 Rechazado" else "❌ Error al rechazar"
                                            }
                                        }
                                    ) {
                                        Text("Rechazar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (mensaje.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(mensaje, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ✅ Aprobar proveedor: actualiza `estado_validacion`
private fun aprobarProveedor(db: FirebaseFirestore, uid: String, onResult: (Boolean) -> Unit) {
    db.collection("proveedores").document(uid)
        .update("estado_validacion", "aprobado")
        .addOnSuccessListener {
            // Opcional: también marca en el documento del usuario si lo tienes
            db.collection("usuarios").document(uid)
                .update("estado_validacion", "aprobado")
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        }
        .addOnFailureListener { onResult(false) }
}

private fun rechazarProveedor(db: FirebaseFirestore, uid: String, onResult: (Boolean) -> Unit) {
    db.collection("proveedores").document(uid)
        .update("estado_validacion", "rechazado")
        .addOnSuccessListener {
            db.collection("usuarios").document(uid)
                .update("estado_validacion", "rechazado")
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        }
        .addOnFailureListener { onResult(false) }
}
