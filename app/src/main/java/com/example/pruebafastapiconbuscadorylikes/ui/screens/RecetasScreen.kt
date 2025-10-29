package com.example.pruebafastapiconbuscadorylikes.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.navigation.Routes
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.example.pruebafastapiconbuscadorylikes.utils.PermissionRequester
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) {
            viewModel.buscarRecetas(initialQuery)
        } else {
            viewModel.escucharTodasRecetas()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔍 Buscar Receta") },
                navigationIcon = {
                    IconButton(onClick = {
                        query = ""
                        viewModel.escucharTodasRecetas()
                        onGoBackToInicio()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (userId == "viewer") {
                            navController.navigate(Routes.LOGIN)
                        } else {
                            onGoToProfile()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Perfil")
                    }

                    IconButton(onClick = {
                        if (userId == "viewer") {
                            Toast.makeText(context, "Inicia sesión para acceder a la tienda", Toast.LENGTH_SHORT).show()
                        } else {
                            navController.navigate("marketplace")
                        }
                    }) {
                        Icon(Icons.Default.Store, contentDescription = "Tienda")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                IconButton(onClick = onGoBackToInicio, modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.List, contentDescription = "Recetas")
                        Text("Recetas", style = MaterialTheme.typography.labelSmall)
                    }
                }

                var shouldRequestPermission by remember { mutableStateOf(false) }

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
                        Icon(Icons.Default.RestaurantMenu, contentDescription = "Favoritos")
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

                        val recetaRef = db.collection("recetas").document(receta.id)
                        if (isLiked) {
                            recetaRef.update(
                                mapOf(
                                    "likes" to FieldValue.increment(1),
                                    "liked_by.$userId" to true
                                )
                            )
                        } else {
                            recetaRef.update(
                                mapOf(
                                    "likes" to FieldValue.increment(-1),
                                    "liked_by.$userId" to FieldValue.delete()
                                )
                            )
                        }
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
}

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
