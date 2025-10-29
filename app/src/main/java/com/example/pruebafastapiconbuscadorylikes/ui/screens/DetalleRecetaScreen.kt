package com.example.pruebafastapiconbuscadorylikes.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafastapiconbuscadorylikes.data.network.LikeRequest
import com.example.pruebafastapiconbuscadorylikes.data.network.RetrofitClient
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.navigation.Routes
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRecetaScreen(
    receta: Receta,
    userId: String,
    navController: NavController,
    onBack: () -> Unit,
    onLike: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLiked by remember(receta.id, userId, receta.liked_by) {
        mutableStateOf(receta.liked_by.containsKey(userId))
    }

    var likesCount by remember { mutableStateOf(receta.likes) }

    val ingredientesEstado = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(receta.ingredientes.size) { add(false) }
        }
    }

    var proveedor by remember { mutableStateOf<Map<String, Any>?>(null) }
    var productoProveedor by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(receta.id) {
        try {
            val db = FirebaseFirestore.getInstance()
            val proveedoresSnapshot = db.collection("proveedores").get().await()

            for (provDoc in proveedoresSnapshot.documents) {
                val productosSnapshot = provDoc.reference.collection("productos").get().await()
                val productoEncontrado = productosSnapshot.find {
                    it.getString("nombre")?.equals(receta.ingrediente_principal, ignoreCase = true) == true
                }

                if (productoEncontrado != null) {
                    proveedor = provDoc.data
                    productoProveedor = productoEncontrado.data
                    break
                }
            }

            db.collection("recetas").document(receta.id)
                .update("popup_clicks", FieldValue.increment(1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(receta.titulo) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (!receta.imagen_final_url.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(receta.imagen_final_url),
                        contentDescription = receta.titulo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Text("📝 ${receta.descripcion}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoChip("🔥 ${receta.calorias ?: 0} kcal")
                    InfoChip("⏱️ ${receta.tiempoPreparacion ?: "?"}")
                    InfoChip("🍽️ ${receta.porciones ?: 0} porciones")
                }

                Spacer(Modifier.height(8.dp))

                if (receta.ingrediente_principal.isNotEmpty()) {
                    Text("🌟 Ingrediente principal: ${receta.ingrediente_principal}")
                    Spacer(Modifier.height(8.dp))
                }

                Text("🧂 Ingredientes", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                receta.ingredientes.forEachIndexed { index, ing ->
                    val texto = buildString {
                        append(ing.nombre ?: "Ingrediente")
                        if (!ing.cantidad.isNullOrBlank()) append(" - ${ing.cantidad}")
                        if (!ing.unidad.isNullOrBlank() && ing.unidad != "-") append(" ${ing.unidad}")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = ingredientesEstado[index],
                            onCheckedChange = { ingredientesEstado[index] = it }
                        )
                        Text(
                            text = texto,
                            modifier = Modifier.padding(start = 4.dp),
                            color = if (ingredientesEstado[index]) Color.Gray else Color.Black
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (receta.pasos.isNotEmpty()) {
                    Text("👨‍🍳 Pasos", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    receta.pasos.sortedBy { it.orden }.forEachIndexed { i, paso ->
                        Text("${i + 1}. ${paso.descripcion}")
                        if (!paso.imagen_url.isNullOrEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Image(
                                painter = rememberAsyncImagePainter(paso.imagen_url),
                                contentDescription = "Paso ${i + 1}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))

                proveedor?.let { prov ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("🏪 Proveedor del producto", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("👨‍🍳 Nombre: ${prov["nombre"] ?: "Desconocido"}")
                            Text("🏷️ Tipo: ${prov["tipoProveedor"] ?: "No especificado"}")
                            Text("📞 Teléfono: ${prov["telefono"] ?: "Sin número"}")
                            Text("💬 WhatsApp: ${prov["whatsapp"] ?: "No disponible"}")
                            Text("📝 Descripción: ${prov["descripcion"] ?: "Sin descripción"}")

                            productoProveedor?.let { prod ->
                                Spacer(Modifier.height(12.dp))
                                Divider()
                                Spacer(Modifier.height(8.dp))
                                Text("🛒 Producto asociado", style = MaterialTheme.typography.titleMedium)
                                Text("📦 Nombre: ${prod["nombre"] ?: "Sin nombre"}")
                                Text("💲 Precio: ${prod["precio"] ?: "Sin precio"}")
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        if (userId == "viewer") {
                            navController.navigate(Routes.LOGIN)
                        } else {
                            val db = FirebaseFirestore.getInstance()
                            val recetaRef = db.collection("recetas").document(receta.id)

                            if (!isLiked) {
                                isLiked = true
                                likesCount += 1
                                recetaRef.update(
                                    mapOf(
                                        "likes" to FieldValue.increment(1),
                                        "liked_by.$userId" to true
                                    )
                                )
                            } else {
                                isLiked = false
                                likesCount = (likesCount - 1).coerceAtLeast(0)
                                recetaRef.update(
                                    mapOf(
                                        "likes" to FieldValue.increment(-1),
                                        "liked_by.$userId" to FieldValue.delete()
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }


                Text(
                    text = "$likesCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
        }
    }
}
