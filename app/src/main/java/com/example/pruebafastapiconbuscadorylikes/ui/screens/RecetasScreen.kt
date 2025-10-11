package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopAppBar(
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = title,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetasScreen(
    viewModel: RecetasViewModel,
    userId: String,
    onRecetaClick: (Receta) -> Unit,
    onGoToProfile: () -> Unit,
    onGoToFavorites: () -> Unit,
    onGoToSettings: () -> Unit,
    onGoBackToInicio: () -> Unit // Este se usará para volver al inicio
) {
    val recetas by viewModel.recetas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var query by remember { mutableStateOf("") }
    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    LaunchedEffect(recetas) {
        if (recetas.isEmpty()) {
            viewModel.escucharTodasRecetas()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔍 Buscar Receta") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Limpiar búsqueda y volver a recetas completas
                        query = ""
                        viewModel.escucharTodasRecetas()
                        onGoBackToInicio()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onGoToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onGoToFavorites) {
                    Text("Favoritos")
                }
                Button(onClick = onGoToSettings) {
                    Text("Configuración")
                }
                Button(onClick = onGoBackToInicio) {
                    Text("Inicio")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar receta...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.buscarRecetas(query) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔍 Buscar")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(state = listState) {
                    items(recetas) { receta ->
                        RecetaCard(
                            receta = receta,
                            onLike = { viewModel.darLike(receta.id, userId) },
                            onClick = {
                                viewModel.sumarVistaReceta(receta.id)
                                onRecetaClick(receta)
                            }
                        )
                    }
                }
            }
        }
    }
}

/*
@Composable
fun RecetasScreen(
    viewModel: RecetasViewModel,
    userId: String,
    onRecetaClick: (Receta) -> Unit,
    onGoToProfile: () -> Unit,
    onGoToFavorites: () -> Unit,
    onGoToSettings: () -> Unit,
    onGoBackToInicio: () -> Unit,
) {
    val recetas by viewModel.recetas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var query by remember { mutableStateOf("") }
    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    LaunchedEffect(recetas) {
        if (recetas.isEmpty()) {
            viewModel.escucharTodasRecetas()
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("🍳 Recetario") },
                actions = {
                    IconButton(onClick = onGoToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onGoToFavorites) {
                    Text("Favoritos")
                }
                Button(onClick = onGoToSettings) {
                    Text("Configuración")
                }
                Button(onClick = onGoBackToInicio) {
                    Text("Inicio")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar receta...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.buscarRecetas(query) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔍 Buscar")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(state = listState) {
                    items(recetas) { receta ->
                        RecetaCard(
                            receta = receta,
                            onLike = { viewModel.darLike(receta.id, userId) },
                            onClick = {
                                viewModel.sumarVistaReceta(receta.id)
                                onRecetaClick(receta)
                            }
                        )
                    }
                }
            }
        }
    }
}
*/


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopAppBar(title: @Composable () -> Unit) {
    TopAppBar(
        title = title,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun RecetaCard(
    receta: Receta,
    onLike: () -> Unit,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dioLike = receta.liked_by.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            // Cambiado: aquí el click abre el detalle
            .clickable { onClick() }
            .animateContentSize(animationSpec = spring()),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    receta.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            if (expanded && receta.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(receta.descripcion, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Likes y vistas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("❤️ ${receta.likes} likes  👁️ ${receta.popup_clicks} vistas")
                TextButton(onClick = onLike) {
                    Text(if (dioLike) "💔 Quitar Like" else "❤️ Like")
                }
            }
        }
    }
}
@Composable
fun InfoChip(text: String) {
    Surface(
        color = Color(0xFFF3E5F5),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4A148C)
        )
    }
}






/*
@Composable
fun RecetasScreen(
    viewModel: RecetasViewModel,
    userId: String,
    onRecetaClick: (Receta) -> Unit
) {
    val recetas by viewModel.recetas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.escucharTodasRecetas()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("🍳 Recetario") })
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar receta...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.buscarRecetas(query) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔍 Buscar")
            }

            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(recetas) { receta ->
                        RecetaCard(
                            receta = receta,
                            onLike = { viewModel.darLike(receta.id, userId) },
                            onClick = { onRecetaClick(receta) } // Aquí se llama el callback
                        )
                    }
                }
            }
        }
    }
}

/*
@Composable
fun RecetasScreen(viewModel: RecetasViewModel, userId: String) {
    val recetas by viewModel.recetas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.escucharTodasRecetas()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("🍳 Recetario") })
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar receta...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.buscarRecetas(query) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔍 Buscar")
            }

            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(recetas) { receta ->
                        RecetaCard(
                            receta = receta,
                            onLike = {
                                viewModel.darLike(receta.id, userId)
                            }
                        )
                    }
                }
            }
        }
    }
}
*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopAppBar(title: @Composable () -> Unit) {
    TopAppBar(
        title = title,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun RecetaCard(
    receta: Receta,
    onLike: () -> Unit,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dioLike = receta.liked_by.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { expanded = !expanded }
            .animateContentSize(animationSpec = spring()),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(12.dp)) {

            // 🔹 Encabezado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    receta.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            if (expanded && receta.descripcion.isNotBlank()) {
                Text(receta.descripcion, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(4.dp))

            // Likes
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("❤️ ${receta.likes} likes  👁️ ${receta.popup_clicks} vistas")
                TextButton(onClick = onLike) {
                    Text(if (dioLike) "💔 Quitar Like" else "❤️ Like")
                }
            }
            /*
            // 🔸 Contenido expandido
            if (expanded) {
                Spacer(Modifier.height(8.dp))

                // Imagen principal
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

                // 🔹 Info básica
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoChip("🔥 ${receta.calorias} kcal")
                    InfoChip("⏱️ ${receta.tiempoPreparacion}")
                    InfoChip("🍽️ ${receta.porciones} porciones")
                }

                Spacer(Modifier.height(8.dp))

                if (!receta.ingrediente_principal.isNullOrEmpty()) {
                    Text(
                        "🌟 Ingrediente principal: ${receta.ingrediente_principal}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4E342E)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // 🔹 Ingredientes
                Text("🧂 Ingredientes", fontWeight = FontWeight.SemiBold)
                val ingredientes = receta.ingredientes
                val estados = remember {
                    mutableStateListOf<Boolean>().apply {
                        repeat(ingredientes.size) { add(false) }
                    }
                }

                ingredientes.forEachIndexed { index, ing ->
                    val texto = buildString {
                        append(ing.nombre ?: "Ingrediente")
                        if (!ing.cantidad.isNullOrBlank()) append(" - ${ing.cantidad}")
                        if (!ing.unidad.isNullOrBlank() && ing.unidad != "-") append(" ${ing.unidad}")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = estados[index],
                            onCheckedChange = { estados[index] = it }
                        )
                        Text(
                            text = texto,
                            color = if (estados[index]) Color.Gray else Color.Black,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 🔹 Pasos
                if (receta.pasos.isNotEmpty()) {
                    Text("👨‍🍳 Pasos", fontWeight = FontWeight.SemiBold)
                    receta.pasos.sortedBy { it.orden }.forEachIndexed { i, paso ->
                        Spacer(Modifier.height(6.dp))
                        Text("${i + 1}. ${paso.descripcion}", style = MaterialTheme.typography.bodyMedium)
                        if (!paso.imagen_url.isNullOrEmpty()) {
                            Spacer(Modifier.height(6.dp))
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
                    }
                }

                Spacer(Modifier.height(8.dp))
            }*/
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Surface(
        color = Color(0xFFF3E5F5),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4A148C)
        )
    }
}
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
                // Puedes agregar botón de "back" aquí si quieres
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(receta.descripcion, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))

            // Botón para dar like
            Button(onClick = onLike, modifier = Modifier.fillMaxWidth()) {
                Text(if (receta.liked_by.isNotEmpty()) "💔 Quitar Like" else "❤️ Like")
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("⬅️ Volver")
            }
        }
    }
}
*/