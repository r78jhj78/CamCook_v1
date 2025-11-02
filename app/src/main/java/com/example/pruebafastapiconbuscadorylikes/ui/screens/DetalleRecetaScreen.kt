@file:OptIn(ExperimentalMaterial3Api::class)

package com.tu.paquete.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
            if (!receta.imagen_final_url.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(receta.imagen_final_url),
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
                    text = "${receta.tiempoPreparacion ?: "?"}",
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
                    if (!paso.imagen_url.isNullOrEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Image(
                            painter = rememberAsyncImagePainter(paso.imagen_url),
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


