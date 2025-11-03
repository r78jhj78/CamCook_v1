package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// MarketplaceScreen.kt
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
    val mustard = Color(0xFFF8A835)

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

    // ✅ Si hay proveedor seleccionado, mostramos la pantalla de detalle
    if (proveedorSeleccionado != null) {
        ProveedorDetailScreen(
            proveedor = proveedorSeleccionado!!.first,
            productos = proveedorSeleccionado!!.second,
            onBack = { proveedorSeleccionado = null }
        )
        return
    }

    // Pantalla normal de marketplace
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Marketplace",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Marketplace",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = mustard)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onGoToForm,
                containerColor = mustard,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar producto",
                    tint = Color.White
                )
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        if (cargando) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = mustard)
            }
        } else if (proveedores.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay proveedores disponibles 😔", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(proveedores) { (prov, productos) ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { proveedorSeleccionado = prov to productos }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = mustard)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    prov["nombre"]?.toString() ?: "Proveedor sin nombre",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Text(
                                "🧺 ${prov["tipoProveedor"] ?: "Proveedor"}",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )

                            Spacer(Modifier.height(8.dp))
                            val img = prov["imagen"]?.toString()
                            if (!img.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(img),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(6.dp))
                            }

                            if (productos.isEmpty()) {
                                Text("Sin productos aún.", color = Color.Gray)
                            } else {
                                productos.take(3).forEach { prod ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Label, contentDescription = null, tint = mustard)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "${prod["nombre"] ?: ""} - ${prod["precio"] ?: ""}",
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                if (productos.size > 3) {
                                    Text("Ver más...", color = mustard, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 📌 Nueva pantalla de detalle del proveedor
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedorDetailScreen(
    proveedor: Map<String, Any>,
    productos: List<Map<String, Any>>,
    onBack: () -> Unit
) {
    val mustard = Color(0xFFF8A835)
    val context = LocalContext.current
    val numeroTelefono = proveedor["telefono"]?.toString()?.replace("+", "")?.replace(" ", "") ?: ""
    val mensaje = "¡Hola! Estoy interesado en tus productos del Marketplace de CamCook 🍳"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(proveedor["nombre"]?.toString() ?: "Proveedor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (numeroTelefono.isNotEmpty()) {
                Button(
                    onClick = {
                        val url = "https://wa.me/$numeroTelefono?text=${java.net.URLEncoder.encode(mensaje, "UTF-8")}"
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        painter = rememberAsyncImagePainter("https://upload.wikimedia.org/wikipedia/commons/6/6b/WhatsApp.svg"),
                        contentDescription = "WhatsApp",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Contactar por WhatsApp", color = Color.White)
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val img = proveedor["imagen"]?.toString()
            if (!img.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(img),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Text("🧺 ${proveedor["tipoProveedor"] ?: "Proveedor"}", fontSize = 14.sp)
            Text(proveedor["descripcion"]?.toString() ?: "", color = Color.DarkGray)

            Divider(Modifier.padding(vertical = 8.dp))
            Text("📦 Productos disponibles:", fontWeight = FontWeight.SemiBold)

            if (productos.isEmpty()) {
                Text("Este proveedor aún no tiene productos registrados.", color = Color.Gray)
            } else {
                productos.forEach { prod ->
                    ProductoFila(prod, mustard)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
@Composable
fun ProductoFila(producto: Map<String, Any>, mustard: Color) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9E7)),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val img = producto["imagen"]?.toString()
            if (!img.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(img),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍽️", fontSize = 20.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    producto["nombre"]?.toString() ?: "Producto sin nombre",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    maxLines = 1
                )
            }

            Text(
                text = "${producto["precio"] ?: 0} Bs",
                color = mustard,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
