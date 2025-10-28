package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.example.pruebafastapiconbuscadorylikes.model.Ingrediente
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProductosIngredientesScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: androidx.navigation.NavController,
    onBack: () -> Unit
) {
    data class IngredienteSeleccionado(
        val ingrediente: Ingrediente,
        var precio: String = "",
        var cantidad: String = "",
        var unidad: String = ""
    )

    var ingredientes by remember { mutableStateOf(listOf<Ingrediente>()) }
    var seleccionados by remember { mutableStateOf(listOf<IngredienteSeleccionado>()) }
    var contacto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }
    val recetas by viewModel.recetas.collectAsState(initial = emptyList())

    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val unidades = listOf("gramos", "kilogramos", "litros", "mililitros", "unidad")

    LaunchedEffect(recetas, userId) {
        try {
            val allIngredientes = recetas.flatMap { it.ingredientes ?: emptyList() }
            ingredientes = allIngredientes.distinctBy { it.nombre }

            val doc = db.collection("proveedores").document(userId).get().await()
            contacto = doc.getString("telefono") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🥕 Publicar ingredientes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (cargando) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Selecciona uno o más ingredientes de tus recetas para vender.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(ingredientes.size) { i ->
                val ing = ingredientes[i]
                val seleccionado = seleccionados.any { it.ingrediente.nombre == ing.nombre }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            seleccionados =
                                if (seleccionado)
                                    seleccionados.filterNot { it.ingrediente.nombre == ing.nombre }
                                else
                                    seleccionados + IngredienteSeleccionado(ing)
                        },
                    colors = if (seleccionado)
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    else
                        CardDefaults.cardColors()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            ing.nombre?.trim()?.replace(Regex("\\s+"), " ")
                                ?: "Ingrediente sin nombre",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        if (seleccionado) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Seleccionado",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (seleccionados.isNotEmpty()) {
                item {
                    Divider()
                    Text(
                        "Asigna precios y cantidades a los ingredientes seleccionados:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(seleccionados.size) { index ->
                    val sel = seleccionados[index]
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(sel.ingrediente.nombre ?: "Ingrediente sin nombre")

                            OutlinedTextField(
                                value = sel.cantidad,
                                onValueChange = { nuevo ->
                                    val limpio = nuevo.trim().replace(",", ".")
                                    if (limpio.isEmpty() || limpio.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                        seleccionados = seleccionados.toMutableList().also {
                                            it[index] = it[index].copy(cantidad = limpio)
                                        }
                                    }
                                },
                                label = { Text("Cantidad") },
                                placeholder = { Text("Ej: 500") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = sel.unidad,
                                    onValueChange = {},
                                    label = { Text("Unidad") },
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    unidades.forEach { unidad ->
                                        DropdownMenuItem(
                                            text = { Text(unidad) },
                                            onClick = {
                                                seleccionados = seleccionados.toMutableList().also {
                                                    it[index] = it[index].copy(unidad = unidad)
                                                }
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = sel.precio,
                                onValueChange = { nuevo ->
                                    val limpio = nuevo.trim().replace(",", ".")
                                    if (limpio.isEmpty() || limpio.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                        seleccionados = seleccionados.toMutableList().also {
                                            it[index] = it[index].copy(precio = limpio)
                                        }
                                    }
                                },
                                label = { Text("💰 Precio (Bs)") },
                                placeholder = { Text("Ej: 15.50") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = { seleccionados = emptyList() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("🗑 Limpiar selección")
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (seleccionados.isEmpty()) {
                            mensaje = "⚠️ Selecciona al menos un ingrediente"
                            return@Button
                        }

                        if (seleccionados.any { it.precio.isBlank() || it.cantidad.isBlank() || it.unidad.isBlank() }) {
                            mensaje = "⚠️ Completa todos los campos antes de publicar"
                            return@Button
                        }

                        scope.launch(Dispatchers.IO) {
                            try {
                                val proveedorRef = db.collection("proveedores").document(userId)
                                seleccionados.forEach { sel ->
                                    val data = mapOf(
                                        "tipo" to "ingrediente",
                                        "nombre" to (sel.ingrediente.nombre ?: "").trim().replace(Regex("\\s+"), " "),
                                        "cantidad" to sel.cantidad,
                                        "unidad" to sel.unidad,
                                        "precio" to "${sel.precio} Bs",
                                        "contacto" to contacto
                                    )
                                    proveedorRef.collection("productos").add(data).await()
                                }

                                withContext(Dispatchers.Main) {
                                    mensaje = "✅ Ingredientes publicados correctamente"
                                }

                                delay(1500)
                                withContext(Dispatchers.Main) {
                                    navController.popBackStack()
                                    navController.navigate("marketplace")
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    mensaje = "❌ Error al publicar: ${e.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aceptar y volver al Marketplace")
                }
            }

            mensaje?.let {
                item {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
