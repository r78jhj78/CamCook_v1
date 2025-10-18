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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pruebafastapiconbuscadorylikes.utils.PermissionRequester

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
    onGoBackToInicio: () -> Unit
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

                val context = LocalContext.current
                var shouldRequestPermission by remember { mutableStateOf(false) }

                IconButton(
                    onClick = {
                        shouldRequestPermission = true
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
                        Icon(Icons.Default.RestaurantMenu, contentDescription = "Ingredientes")
                        Text("Ingredientes", style = MaterialTheme.typography.labelSmall)
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
                            onLike = {
                                viewModel.darLike(receta.id, userId)
                            },
                            onClick = {
                                viewModel.registrarVista(receta.id, userId)
                                onRecetaClick(receta)
                            }
                        )
                    }
                }

            }
        }
    }
}

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
    userId: String,
    onLike: () -> Unit,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dioLike = receta.liked_by.contains(userId)
    val ingredientesEstado = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(receta.ingredientes.size) { add(false) }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                receta.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text("❤️ ${receta.likes} likes  👁️ ${receta.popup_clicks} vistas")

            if (receta.descripcion.length > 50) {
                Text(receta.descripcion.take(50) + "...", style = MaterialTheme.typography.bodySmall)
            } else {
                Text(receta.descripcion, style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onLike, modifier = Modifier.align(Alignment.End)) {
                Icon(
                    imageVector = if (dioLike) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (dioLike) Color.Red else Color.Gray
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
