@file:OptIn(ExperimentalMaterial3Api::class)

package com.tu.paquete.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage


@Composable
fun DetalleRecetaScreen(
    receta: Receta,
    userId: String,
    navController: NavController,
    onBack: () -> Unit,
    onLike: () -> Unit
) {
    val camcookColor = Color(0xFFFAA935)
    val accentColor = Color(0xFF8C7B6B)
    val backgroundColor = Color(0xFFF6F6F6)
    val cardColor = Color(0xFFFFF3E0)
    val context = LocalContext.current

    var isLiked by remember(receta.id, userId, receta.liked_by) {
        mutableStateOf(receta.liked_by.containsKey(userId))
    }
    var likesCount by remember { mutableStateOf(receta.likes) }

    val ingredientesEstado = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(receta.ingredientes.size) { add(false) }
        }
    }

    // 🔹 Proveedores que venden esta receta o sus ingredientes
    val db = FirebaseFirestore.getInstance()
    var proveedores by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val apiService = remember { com.example.pruebafastapiconbuscadorylikes.data.network.RetrofitClient.api }

    var nombreAutor by remember { mutableStateOf("Autor desconocido") }

    LaunchedEffect(receta.authorUid) {
        if (!receta.authorUid.isNullOrBlank()) {
            try {
                val usuarioDoc = db.collection("usuarios")
                    .document(receta.authorUid!!)
                    .get()
                    .await()

                val nombre = usuarioDoc.getString("usuario") ?: "Autor desconocido"
                nombreAutor = nombre
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    LaunchedEffect(receta.id) {
        val recetaRef = db.collection("recetas").document(receta.id)
        try {
            if (userId == "viewer") {
                // Incrementar vistas sin registrar usuario
                recetaRef.update("views", FieldValue.increment(1)).await()
            } else {
                // Incrementar vistas y registrar usuario
                recetaRef.update(
                    mapOf(
                        "views" to FieldValue.increment(1),
                        "viewed_by.$userId" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 🔹 Cargar proveedores como antes
        try {
            val snapshot = db.collection("proveedores").get().await()
            val lista = mutableListOf<Map<String, Any>>()

            for (doc in snapshot.documents) {
                val estado = doc.getString("estado_validacion") ?: ""
                if (estado == "aprobado") {
                    val productosSnap = doc.reference.collection("productos").get().await()

                    for (p in productosSnap.documents) {
                        val data = p.data ?: continue
                        val tipo = data["tipo"] ?: ""
                        val nombreProducto = (data["nombre"] ?: "").toString()

                        // 🔹 Coincidencia: receta o ingredientes de la receta
                        val esRecetaCoincidente =
                            tipo == "receta" && data["recetaId"] == receta.id
                        val esIngredienteCoincidente =
                            tipo == "ingrediente" &&
                                    receta.ingredientes.any { it.nombre.equals(nombreProducto, ignoreCase = true) }

                        if (esRecetaCoincidente || esIngredienteCoincidente) {
                            val proveedorImg = doc.getString("imagen") ?: ""
                            val proveedorTel = doc.getString("telefono") ?: ""
                            val proveedorNombre = doc.getString("nombre") ?: ""
                            val productoPrecio = data["precio"] ?: ""
                            val productoUnidad = data["unidad"] ?: ""
                            val productoCantidad = data["cantidad"] ?: ""

                            lista.add(
                                mapOf(
                                    "productoNombre" to nombreProducto,
                                    "productoPrecio" to productoPrecio,
                                    "productoCantidad" to productoCantidad,
                                    "productoUnidad" to productoUnidad,
                                    "proveedorImg" to proveedorImg,
                                    "proveedorTel" to proveedorTel,
                                    "proveedorNombre" to proveedorNombre
                                )
                            )
                        }
                    }
                }
            }
            proveedores = lista
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(receta.titulo, color = camcookColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = camcookColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 📸 Imagen principal
            if (!receta.mainImageUrl.isNullOrEmpty() || !receta.imagenUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(receta.mainImageUrl.ifEmpty { receta.imagenUrl }),
                    contentDescription = receta.titulo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))
            }

            // 📝 Descripción
            Text(
                "📝 ${receta.descripcion}",
                style = MaterialTheme.typography.bodyMedium,
                color = accentColor
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "👩‍🍳 Autor: $nombreAutor",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(16.dp))

            // 🌡️ Info principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChipWithIcon(
                    icon = Icons.Default.LocalFireDepartment,
                    text = "${receta.calorias ?: 0} kcal",
                    backgroundColor = camcookColor
                )
                InfoChipWithIcon(
                    icon = Icons.Default.Timer,
                    text = receta.tiempoFormateado,
                    backgroundColor = camcookColor
                )
                InfoChipWithIcon(
                    icon = Icons.Default.Restaurant,
                    text = "${receta.porciones ?: 0} porciones",
                    backgroundColor = camcookColor
                )
            }

            Spacer(Modifier.height(24.dp))

            // 🧂 Ingredientes
            Text("🧂 Ingredientes", color = camcookColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            receta.ingredientes.forEachIndexed { index, ing ->
                val texto = buildString {
                    append(ing.nombre ?: "Ingrediente")
                    if (!ing.cantidad.isNullOrBlank()) append(" - ${ing.cantidad}")
                    if (!ing.unidad.isNullOrBlank() && ing.unidad != "-") append(" ${ing.unidad}")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = ingredientesEstado[index],
                        onCheckedChange = { ingredientesEstado[index] = it },
                        colors = CheckboxDefaults.colors(checkedColor = camcookColor)
                    )
                    Text(
                        text = texto,
                        modifier = Modifier.padding(start = 4.dp),
                        color = if (ingredientesEstado[index]) Color.Gray else Color.Black
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 👨‍🍳 Pasos
            if (receta.pasos.isNotEmpty()) {
                Text("👨‍🍳 Pasos", color = camcookColor, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                receta.pasos.sortedBy { it.orden }.forEachIndexed { i, paso ->
                    Text("${i + 1}. ${paso.descripcion}", color = accentColor)
                    if (!paso.imagenUrl.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(paso.imagenUrl),
                            contentDescription = "Paso ${i + 1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // 🛒 Proveedores que venden esta receta o sus ingredientes
            if (proveedores.isNotEmpty()) {
                // Agrupar por proveedor
                val proveedoresAgrupados = proveedores.groupBy { it["proveedorNombre"] }

                var proveedorSeleccionado by remember { mutableStateOf<Map<String, Any>?>(null) }
                var productosSeleccionados by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

                Text(
                    "📦 Proveedores disponibles:",
                    color = camcookColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(8.dp))

                proveedoresAgrupados.forEach { (nombreProveedor, productos) ->
                    val proveedorInfo = productos.firstOrNull() ?: return@forEach

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                proveedorSeleccionado = proveedorInfo
                                productosSeleccionados = productos.distinctBy { it["productoNombre"] }
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Foto + nombre
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = rememberAsyncImagePainter(proveedorInfo["proveedorImg"]),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    nombreProveedor.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 16.sp
                                )
                            }

                            val context = LocalContext.current

                            IconButton(
                                onClick = {
                                    val phone = proveedorInfo["proveedorTel"]?.toString()?.filter { it.isDigit() } ?: ""
                                    val mensaje = "¡Hola ${proveedorInfo["proveedorNombre"]}! Estoy interesado en tus productos de la receta \"${receta.titulo}\" 🍳"
                                    if (phone.isNotEmpty()) {
                                        try {
                                            val url = "https://wa.me/$phone?text=${java.net.URLEncoder.encode(mensaje, "UTF-8")}"
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Número de WhatsApp no disponible", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(
                                    text = "WhatsApp",
                                    color = Color(0xFF25D366),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // 📦 Popup con los productos del proveedor seleccionado
                if (proveedorSeleccionado != null) {
                    AlertDialog(
                        onDismissRequest = {
                            proveedorSeleccionado = null
                            productosSeleccionados = emptyList()
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                proveedorSeleccionado = null
                                productosSeleccionados = emptyList()
                            }) {
                                Text("Cerrar", color = camcookColor)
                            }
                        },
                        title = {
                            Text(
                                "🧾 ${proveedorSeleccionado!!["proveedorNombre"]}",
                                fontWeight = FontWeight.Bold,
                                color = camcookColor
                            )
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                productosSeleccionados.forEach { p ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFAE5)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Text(
                                                p["productoNombre"].toString(),
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                            val cantidad = p["productoCantidad"]?.toString()
                                            val unidad = p["productoUnidad"]?.toString()
                                            if (!cantidad.isNullOrBlank() && !unidad.isNullOrBlank()) {
                                                Text("⚖️ $cantidad $unidad", color = Color.Gray, fontSize = 13.sp)
                                            }
                                            Text("💰 ${p["productoPrecio"]} Bs", color = Color.Gray, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Color.White
                    )
                }
            } else {
                Text(
                    "😞 Ningún proveedor ofrece esta receta todavía.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(32.dp))

            // ❤️ Like y calificación
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Te encantó esta receta?",
                        style = MaterialTheme.typography.titleMedium,
                        color = camcookColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Si te hizo agua la boca, ¡dale like y compártela con tus amigos!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = accentColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    IconButton(
                        onClick = {
                            if (userId == "viewer") {
                                Toast.makeText(
                                    context,
                                    "Inicia sesión para dar like",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val recetaRef = db.collection("recetas").document(receta.id)
                                scope.launch {
                                    if (!isLiked) {
                                        isLiked = true
                                        likesCount += 1
                                        recetaRef.update(
                                            mapOf(
                                                "likes" to FieldValue.increment(1),
                                                "liked_by.$userId" to true
                                            )
                                        ).await()
                                    } else {
                                        isLiked = false
                                        likesCount = (likesCount - 1).coerceAtLeast(0)
                                        recetaRef.update(
                                            mapOf(
                                                "likes" to FieldValue.increment(-1),
                                                "liked_by.$userId" to FieldValue.delete()
                                            )
                                        ).await()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = "$likesCount personas ya la han amado ❤️",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun InfoChipWithIcon(
    icon: ImageVector,
    text: String,
    backgroundColor: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor.copy(alpha = 0.15f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = backgroundColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

