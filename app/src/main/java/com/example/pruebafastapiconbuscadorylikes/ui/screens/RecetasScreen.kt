package com.example.pruebafastapiconbuscadorylikes.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.navigation.Routes
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.example.pruebafastapiconbuscadorylikes.utils.PermissionRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetasScreen(
    navController: NavController,
    viewModel: RecetasViewModel,
    userId: String,
    onRecetaClick: (Receta) -> Unit,
    onGoToProfile: () -> Unit,
    onGoToFavorites: () -> Unit,
    onGoToSettings: () -> Unit,
    onGoBackToInicio: () -> Unit,
    initialQuery: String = ""
) {
    val recetas by viewModel.recetas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var query by remember { mutableStateOf(initialQuery) }
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val context = LocalContext.current

    val backgroundColor = Color(0xFFF6F6F6)
    val primaryColor = Color(0xFFE29A4C)
    val accentColor = Color(0xFFB3A99A)
    val beigeColor = Color(0xFFE5D9C5)

    var shouldRequestPermission by remember { mutableStateOf(false) }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) {
            viewModel.buscarRecetas(initialQuery)
        } else {
            viewModel.escucharTodasRecetas()
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Cook Cam",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        query = ""
                        viewModel.escucharTodasRecetas()
                        onGoBackToInicio()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (userId == "viewer") {
                            navController.navigate(Routes.LOGIN)
                        } else onGoToProfile()
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
                    }

                    IconButton(onClick = {
                        if (userId == "viewer") {
                            Toast.makeText(context, "Inicia sesión para acceder a la tienda", Toast.LENGTH_SHORT).show()
                        } else navController.navigate("marketplace")
                    }) {
                        Icon(Icons.Default.Store, contentDescription = "Tienda", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryColor)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = primaryColor, contentColor = Color.White) {
                IconButton(onClick = onGoBackToInicio, modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.List, contentDescription = "Recetas")
                        Text("Recetas", style = MaterialTheme.typography.labelSmall)
                    }
                }

                IconButton(
                    onClick = {
                        if (userId == "viewer") {
                            navController.navigate(Routes.LOGIN)
                        } else {
                            shouldRequestPermission = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
                        Text("Cámara", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (shouldRequestPermission) {
                    PermissionRequester(
                        permission = android.Manifest.permission.CAMERA,
                        onPermissionGranted = {
                            shouldRequestPermission = false
                            navController.navigate("camera")
                        }
                    )
                }

                IconButton(onClick = onGoToFavorites, modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favoritos")
                        Text("Favoritos", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // 🔍 Buscador con estilo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(beigeColor) // Fondo uniforme
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = accentColor, // Color del texto
                            fontSize = 16.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    text = "Buscar receta...",
                                    color = accentColor.copy(alpha = 0.6f),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    IconButton(
                        onClick = { viewModel.buscarRecetas(query) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = accentColor, // Color del ícono
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = primaryColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cargando recetas...", color = accentColor)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f), // ✅ Corrección clave
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(recetas) { receta ->
                        RecetaCard(
                            receta = receta,
                            userId = userId,
                            onClick = {
                                viewModel.registrarVista(receta.id, userId)
                                onRecetaClick(receta)
                            },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun BottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit),
    label: @Composable (() -> Unit)?
) {
    TODO("Not yet implemented")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecetaCard(
    receta: Receta,
    userId: String,
    onClick: () -> Unit,
    navController: NavController,
) {
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(receta.liked_by.containsKey(userId)) }
    var likesCount by remember { mutableStateOf(receta.likes) }
    val db = FirebaseFirestore.getInstance()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Imagen superior responsiva
            AsyncImage(
                model = receta.imagen_final_url,
                contentDescription = receta.titulo,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Título
            Text(
                text = receta.titulo,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B),
                    fontSize = 20.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Descripción
            Text(
                text = receta.descripcion.take(100) + if (receta.descripcion.length > 100) "..." else "",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 🔹 Chips informativos con íconos (usa tu InfoChip)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChipWithIcon(Icons.Default.AccessTime, "${receta.tiempoPreparacion ?: "N/A"} min", Color(0xFF2196F3))
                InfoChipWithIcon(Icons.Default.Restaurant, "${receta.porciones ?: 0} porciones", Color(0xFF4CAF50))
                InfoChipWithIcon(Icons.Default.Whatshot, "${receta.calorias ?: 0} cal", Color(0xFFFF5722))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ingredientes (máximo 3 visibles)
            if (receta.ingredientes.isNotEmpty()) {
                Text(
                    text = "Ingredientes:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column {
                    receta.ingredientes.take(3).forEach { ingrediente ->
                        Text(
                            text = "• ${ingrediente.nombre ?: ""} ${ingrediente.cantidad ?: ""} ${ingrediente.unidad ?: ""}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF4A4A4A),
                                fontSize = 13.sp
                            )
                        )
                    }
                    if (receta.ingredientes.size > 3) {
                        Text(
                            text = "… y más",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ❤️ Likes y 👁️ vistas
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (userId == "viewer") {
                                navController.navigate(Routes.LOGIN)
                            } else {
                                isLiked = !isLiked
                                likesCount = if (isLiked) likesCount + 1 else (likesCount - 1).coerceAtLeast(0)

                                val recetaRef = db.collection("recetas").document(receta.id)

                                db.runTransaction { transaction ->
                                    val snapshot = transaction.get(recetaRef)
                                    val currentLikes = snapshot.getLong("likes") ?: 0
                                    val likedBy = snapshot.get("liked_by") as? MutableMap<String, Boolean> ?: mutableMapOf()

                                    if (isLiked) {
                                        likedBy[userId] = true
                                        transaction.update(recetaRef, mapOf(
                                            "likes" to currentLikes + 1,
                                            "liked_by" to likedBy
                                        ))
                                    } else {
                                        likedBy.remove(userId)
                                        transaction.update(recetaRef, mapOf(
                                            "likes" to (currentLikes - 1).coerceAtLeast(0),
                                            "liked_by" to likedBy
                                        ))
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isLiked) Color(0xFFE53935) else Color.Gray
                        )
                    }

                    Text(
                        text = "$likesCount Me gusta",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${receta.views ?: 0}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                }
            }
        }
    }
}


@Composable
fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFF3F3F3), shape = RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF2C2C2C),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun InfoChipWithIcon(icon: ImageVector, text: String, iconTint: Color = Color(0xFF616161)) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        InfoChip(text = text)
    }
}




/*
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
fun RecetaCard(
    receta: Receta,
    userId: String,
    onLike: () -> Unit,
    onClick: () -> Unit,
    navController: NavController,
) {
    var isLiked by remember { mutableStateOf(receta.liked_by.contains(userId)) }
    var likesCount by remember { mutableStateOf(receta.likes) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(receta.titulo, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("❤️ $likesCount likes  👁️ ${receta.popup_clicks} vistas")
            Spacer(Modifier.height(4.dp))
            Text(
                receta.descripcion.take(50) + if (receta.descripcion.length > 50) "..." else "",
                style = MaterialTheme.typography.bodySmall
            )

            IconButton(
                onClick = {
                    if (userId == "viewer") {
                        navController.navigate(Routes.LOGIN)
                    } else {
                        isLiked = !isLiked
                        likesCount = if (isLiked) likesCount + 1 else (likesCount - 1).coerceAtLeast(0)
                        onLike()
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray
                )
            }
        }
    }
}*/