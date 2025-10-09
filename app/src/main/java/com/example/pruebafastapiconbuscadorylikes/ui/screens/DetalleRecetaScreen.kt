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
/*
@Composable
fun DetalleRecetaScreen(
    receta: Receta,
    onBack: () -> Unit,
    onLike: () -> Unit
) {
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
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Mostrar datos completos de la receta
            if (receta.imagen_final_url?.isNotEmpty() == true) {
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text("Descripción:")
            Text(receta.descripcion, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Calorías: ${receta.calorias}")
            Text("Tiempo de preparación: ${receta.tiempoPreparacion}")
            Text("Porciones: ${receta.porciones}")

            Spacer(modifier = Modifier.height(12.dp))

            Text("Ingrediente principal: ${receta.ingrediente_principal}")

            Spacer(modifier = Modifier.height(12.dp))

            Text("Ingredientes:")
            LazyColumn {
                items(receta.ingredientes) { ing ->
                    val texto = buildString {
                        append(ing.nombre ?: "")
                        if (!ing.cantidad.isNullOrBlank()) append(" - ${ing.cantidad}")
                        if (!ing.unidad.isNullOrBlank() && ing.unidad != "-") append(" ${ing.unidad}")
                    }
                    Text(texto)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Pasos:")
            LazyColumn {
                items(receta.pasos.sortedBy { it.orden }) { paso ->
                    Text("${paso.orden}. ${paso.descripcion}")
                    if (!paso.imagen_url.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Image(
                            painter = rememberAsyncImagePainter(paso.imagen_url),
                            contentDescription = paso.descripcion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLike,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (receta.liked_by.isNotEmpty()) "💔 Quitar Like" else "❤️ Like")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⬅️ Volver")
            }
        }
    }
}
*/
@Composable
fun DetalleRecetaScreen(
    receta: Receta,
    onBack: () -> Unit,
    onLike: () -> Unit
) {
    // Para marcar ingredientes
    val ingredientesEstado = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(receta.ingredientes.size) { add(false) }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(receta.titulo) }
            )
        }
    ) { padding ->
        // TODO: Aquí usamos Column dentro de Scroll para que todo sea scrolleable
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

            // Botón de like
            Button(
                onClick = onLike,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (receta.liked_by.isNotEmpty()) "💔 Quitar Like" else "❤️ Like")
            }

            Spacer(Modifier.height(12.dp))

            // Botón para volver
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⬅️ Volver")
            }

            Spacer(Modifier.height(16.dp)) // margen final
        }
    }
}
