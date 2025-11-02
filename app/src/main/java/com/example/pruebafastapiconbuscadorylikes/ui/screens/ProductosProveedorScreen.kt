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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
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
    val mustardColor = Color(0xFFFFC107) // color mostaza

    LaunchedEffect(Unit) {
        db.collection("proveedores").document(userId)
            .collection("productos")
            .addSnapshotListener { snap, _ ->
                productos = snap?.documents?.mapNotNull { it.data } ?: emptyList()
            }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imagenUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📦 Mis Productos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = mustardColor,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF8F8F8)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Agrega un nuevo producto a tu catálogo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del producto") },
                leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio (ej. 10 Bs/kg)") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = mustardColor)
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (imagenUri != null) "✅ Imagen seleccionada" else "Seleccionar imagen")
            }

            imagenUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

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
                                    mensaje = "✅ Producto agregado correctamente"
                                    nombre = ""
                                    precio = ""
                                    imagenUri = null
                                }
                                .addOnFailureListener {
                                    mensaje = "❌ Error al guardar el producto"
                                }
                        } else {
                            mensaje = "❌ Error al subir la imagen a ImgBB"
                        }
                        isUploading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = mustardColor)
            ) {
                Icon(
                    if (isUploading) Icons.Default.CloudUpload else Icons.Default.AddCircle,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isUploading) "Subiendo..." else "Agregar producto")
            }

            mensaje?.let {
                Text(it, color = mustardColor, fontWeight = FontWeight.Medium)
            }

            Divider(Modifier.padding(vertical = 8.dp))
            Text("🛒 Productos registrados:", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(productos.size) { i ->
                    val p = productos[i]
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Label, contentDescription = null, tint = mustardColor)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    p["nombre"]?.toString() ?: "Sin nombre",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("💰 ${p["precio"] ?: "No definido"}")

                            val url = p["imagen"] as? String
                            if (!url.isNullOrEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Image(
                                    painter = rememberAsyncImagePainter(url),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            if (productos.isNotEmpty()) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Volver al Marketplace", color = Color.White)
                }
            }
        }
    }
}

