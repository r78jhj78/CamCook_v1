package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafastapiconbuscadorylikes.model.Receta

@Composable
fun DetalleRecetaScreen(
    receta: Receta,
    onBack: () -> Unit,
    onLike: () -> Unit,
    userId: String,
) {
    val ingredientesEstado = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(receta.ingredientes.size) { add(false) }
        }
    }
    val dioLike = receta.liked_by.contains(userId)
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(receta.titulo) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Imagen de la receta
            if (!receta.imagen_final_url.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(receta.imagen_final_url),
                    contentDescription = receta.titulo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
            }

            // Descripción
            Text("📝 ${receta.descripcion}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))

            // Info básica
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChip("🔥 ${receta.calorias} kcal")
                InfoChip("⏱️ ${receta.tiempoPreparacion}")
                InfoChip("🍽️ ${receta.porciones} porciones")
            }

            Spacer(Modifier.height(8.dp))

            if (!receta.ingrediente_principal.isNullOrEmpty()) {
                Text("🌟 Ingrediente principal: ${receta.ingrediente_principal}")
                Spacer(Modifier.height(8.dp))
            }

            // Ingredientes con checkbox
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

            // Pasos
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

            if (!dioLike) {
                Button(
                    onClick = onLike,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("❤️ Like")
                }
            } else {
                Button(
                    onClick = onLike,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💔 Quitar Like")
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⬅️ Volver")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

