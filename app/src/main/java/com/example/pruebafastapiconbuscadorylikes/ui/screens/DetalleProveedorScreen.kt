package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProveedorScreen(
    uid: String,
    onBack: () -> Unit
) {
    val camcookColor = Color(0xFFFAA935)
    val accentColor = Color(0xFF8C7B6B)
    val backgroundColor = Color(0xFFF6F6F6)
    val cardColor = Color(0xFFFFF3E0)

    val db = FirebaseFirestore.getInstance()
    var proveedor by remember { mutableStateOf<Map<String, Any>?>(null) }
    var productos by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }
    var motivoRechazo by remember { mutableStateOf("") } // ✨ nuevo campo

    LaunchedEffect(uid) {
        cargando = true
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
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📄 Detalle del Proveedor",
                        color = accentColor,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = camcookColor)
            )
        }
    ) { padding ->
        if (cargando) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = camcookColor)
            }
        } else {
            proveedor?.let { prov ->
                // 🔽 Scroll general de toda la pantalla
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // 👈 hace scroll en todo
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Información del proveedor
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("🏪 Nombre: ${prov["nombre"]}", color = accentColor)
                            Text("📞 Teléfono: ${prov["telefono"]}", color = accentColor)
                            Text("🧺 Tipo de proveedor: ${prov["tipoProveedor"]}", color = accentColor)
                            Text("📜 Descripción: ${prov["descripcion"]}", color = accentColor)
                            Text("🌐 Imagen: ${prov["imagen"] ?: "No disponible"}", color = accentColor)
                            Text("🕓 Estado actual: ${prov["estado_validacion"]}", color = accentColor)
                            prov["motivo_rechazo"]?.let {
                                Text("📄 Motivo de rechazo previo: $it", color = Color.Red)
                            }
                        }
                    }

                    Divider(thickness = 1.dp, color = accentColor)

                    Text(
                        "📦 Productos publicados:",
                        style = MaterialTheme.typography.titleMedium.copy(color = accentColor)
                    )

                    if (productos.isEmpty()) {
                        Text("No tiene productos registrados.", color = accentColor)
                    } else {
                        productos.forEach { prod ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = cardColor),
                                elevation = CardDefaults.cardElevation(2.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("🍽️ Nombre: ${prod["nombre"]}", color = accentColor)
                                    Text("💰 Precio: ${prod["precio"]}", color = accentColor)
                                    Text("📂 Tipo: ${prod["tipo"]}", color = accentColor)
                                    prod["cantidad"]?.let {
                                        Text("📦 Cantidad: $it", color = accentColor)
                                    }
                                    prod["unidad"]?.let {
                                        Text("⚖️ Unidad: $it", color = accentColor)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ✨ Campo de texto para el motivo del rechazo
                    OutlinedTextField(
                        value = motivoRechazo,
                        onValueChange = { motivoRechazo = it.take(200) },
                        label = { Text("Motivo del rechazo (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            cursorColor = accentColor
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    // Botones de acción
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                actualizarEstado(db, uid, "aprobado", null) {
                                    mensaje = if (it) "✅ Aprobado correctamente" else "❌ Error al aprobar"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = camcookColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Aprobar")
                        }

                        OutlinedButton(
                            onClick = {
                                if (motivoRechazo.isBlank()) {
                                    mensaje = "⚠️ Debes ingresar el motivo del rechazo"
                                } else {
                                    actualizarEstado(db, uid, "rechazado", motivoRechazo) {
                                        mensaje = if (it) "🚫 Rechazado correctamente" else "❌ Error al rechazar"
                                    }
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Rechazar")
                        }
                    }

                    if (mensaje.isNotEmpty()) {
                        Text(
                            mensaje,
                            color = if (mensaje.contains("✅") || mensaje.contains("🚫")) Color(0xFF388E3C) else Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }
            } ?: Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("❌ No se encontró el proveedor.", color = accentColor)
            }
        }
    }
}

// ✅ función actualizada: también guarda el motivo
private fun actualizarEstado(
    db: FirebaseFirestore,
    uid: String,
    estado: String,
    motivo: String?,
    onResult: (Boolean) -> Unit
) {
    val data = if (motivo != null) {
        mapOf("estado_validacion" to estado, "motivo_rechazo" to motivo)
    } else {
        mapOf("estado_validacion" to estado, "motivo_rechazo" to FieldValue.delete())
    }

    db.collection("proveedores").document(uid)
        .update(data)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}
