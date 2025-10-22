package com.example.pruebafastapiconbuscadorylikes.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafastapiconbuscadorylikes.data.network.ImgBBApi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosProveedorScreen(
    userId: String,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var productos by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 🔁 Escuchar productos del proveedor en tiempo real
    LaunchedEffect(Unit) {
        db.collection("proveedores").document(userId)
            .collection("productos")
            .addSnapshotListener { snap, _ ->
                productos = snap?.documents?.mapNotNull { it.data } ?: emptyList()
            }
    }

    // 📸 Seleccionar imagen desde galería
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imagenUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📦 Mis Productos") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 🧾 Campos
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio (ej. 10 Bs/kg)") },
                modifier = Modifier.fillMaxWidth()
            )

            // 📷 Selector de imagen
            Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (imagenUri != null) "✅ Imagen seleccionada" else "📷 Seleccionar imagen")
            }

            imagenUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            // 🚀 Botón para subir producto
            Button(
                onClick = {
                    if (nombre.isEmpty()) {
                        mensaje = "⚠️ Completa el nombre del producto"
                        return@Button
                    }

                    if (imagenUri == null) {
                        mensaje = "⚠️ Selecciona una imagen"
                        return@Button
                    }

                    isUploading = true
                    mensaje = "⏳ Subiendo imagen..."

                    scope.launch(Dispatchers.IO) {
                        val imageUrl = ImgBBApi.uploadImage(context, imagenUri!!)
                        if (imageUrl != null) {
                            val prod = mapOf(
                                "nombre" to nombre,
                                "precio" to precio,
                                "imagen" to imageUrl
                            )

                            db.collection("proveedores").document(userId)
                                .collection("productos")
                                .add(prod)
                                .addOnSuccessListener {
                                    mensaje = "✅ Producto agregado"
                                    nombre = ""
                                    precio = ""
                                    imagenUri = null
                                }
                                .addOnFailureListener {
                                    mensaje = "❌ Error al guardar producto"
                                }
                        } else {
                            mensaje = "❌ Error al subir imagen a ImgBB"
                        }
                        isUploading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) {
                Text(if (isUploading) "Subiendo..." else "Agregar producto")
            }

            mensaje?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Divider(Modifier.padding(vertical = 8.dp))
            Text("🛒 Productos registrados:", style = MaterialTheme.typography.titleMedium)

            // 📋 Lista de productos
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(productos.size) { i ->
                    val p = productos[i]
                    Column {
                        Text("• ${p["nombre"]} - ${p["precio"]}")
                        val url = p["imagen"] as? String
                        if (!url.isNullOrEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                        }
                    }
                }
            }

            if (productos.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Finalizar y volver al Marketplace")
                }
            }
        }
    }
}
