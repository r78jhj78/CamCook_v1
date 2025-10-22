package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafastapiconbuscadorylikes.data.model.Proveedor
import com.example.pruebafastapiconbuscadorylikes.data.model.Producto
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedorDetallePopup(
    proveedor: Proveedor,
    onClose: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }

    // 🔥 Escucha productos del proveedor
    LaunchedEffect(proveedor.userId) {
        db.collection("proveedores").document(proveedor.userId)
            .collection("productos")
            .addSnapshotListener { snap, _ ->
                productos = snap?.toObjects(Producto::class.java) ?: emptyList()
            }
    }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = onClose) { Text("Cerrar") }
        },
        title = { Text(proveedor.nombre, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(proveedor.imagen),
                    contentDescription = proveedor.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                Text("🧺 ${proveedor.tipoProducto}", style = MaterialTheme.typography.labelLarge)
                Text(proveedor.descripcion)

                Divider()
                Text("📦 Productos disponibles:", style = MaterialTheme.typography.titleMedium)

                if (productos.isEmpty()) {
                    Text("No hay productos registrados aún.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 250.dp)
                    ) {
                        items(productos) { p ->
                            Column {
                                Text("• ${p.nombre} - ${p.precio}")
                                if (p.imagen.isNotEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(p.imagen),
                                        contentDescription = p.nombre,
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
