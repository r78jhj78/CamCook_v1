package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.pruebafastapiconbuscadorylikes.model.Ingrediente
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.compose.foundation.lazy.items
import com.example.pruebafastapiconbuscadorylikes.data.manager.ValidacionManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProductosIngredientesScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: androidx.navigation.NavController,
    onBack: () -> Unit
) {
    class IngredienteSeleccionado(val ingrediente: Ingrediente) {
        val cantidad = mutableStateOf("")
        val precio = mutableStateOf("")
        val unidad = mutableStateOf("")
    }

    val recetas by viewModel.recetas.collectAsState(initial = emptyList())
    val seleccionados = remember { mutableStateListOf<IngredienteSeleccionado>() }
    var contacto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val unidades = listOf("gramos", "kilogramos", "litros", "mililitros", "unidad")
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val ingredientesFiltrados = if (searchQuery.isBlank()) {
        recetas.flatMap { it.ingredientes ?: emptyList() }.distinctBy { it.nombre }
    } else {
        recetas.filter { receta ->
            receta.titulo.contains(searchQuery, ignoreCase = true) ||
                    receta.descripcion.contains(searchQuery, ignoreCase = true)
        }.flatMap { it.ingredientes ?: emptyList() }.distinctBy { it.nombre }
    }

    LaunchedEffect(recetas, userId) {
        try {
            val doc = db.collection("proveedores").document(userId).get().await()
            contacto = doc.getString("telefono") ?: ""
        } catch (_: Exception) {
        } finally {
            cargando = false
        }
    }

    val camcookColor = Color(0xFFFAA935)
    val accentColor = Color(0xFF8C7B6B)
    val backgroundColor = Color(0xFFF6F6F6)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🥕 Publicar ingredientes", color = camcookColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = camcookColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
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
                CircularProgressIndicator(color = camcookColor)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por receta...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = camcookColor,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = camcookColor
                )
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (ingredientesFiltrados.isEmpty()) {
                    item {
                        Text("No se encontraron ingredientes", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                }

                items(ingredientesFiltrados) { ing ->
                val seleccionada = seleccionados.find { it.ingrediente.nombre == ing.nombre }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (seleccionada != null) seleccionados.remove(seleccionada)
                                else seleccionados.add(IngredienteSeleccionado(ing))
                            }
                            .border(
                                width = if (seleccionada != null) 2.dp else 0.dp,
                                color = if (seleccionada != null) Color(0xFFFFEB3B) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    ing.nombre ?: "Ingrediente sin nombre",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (seleccionada != null) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = camcookColor, modifier = Modifier.size(28.dp))
                                }
                            }

                            seleccionada?.let { sel ->
                                Spacer(Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = sel.cantidad.value,
                                        onValueChange = { nuevo ->
                                            val limpio = nuevo.trim().replace(",", ".")
                                            if (limpio.isEmpty() || limpio.matches(Regex("^\\d*\\.?\\d*\$"))) sel.cantidad.value = limpio
                                        },
                                        label = { Text("Cantidad", color = accentColor) },
                                        placeholder = { Text("Ej: 500") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = camcookColor,
                                            unfocusedBorderColor = camcookColor,
                                            cursorColor = camcookColor
                                        )
                                    )

                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded }
                                    ) {
                                        OutlinedTextField(
                                            value = sel.unidad.value,
                                            onValueChange = {},
                                            label = { Text("Unidad", color = accentColor) },
                                            readOnly = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                            },
                                            modifier = Modifier
                                                .menuAnchor() // ✅ necesario para el comportamiento correcto
                                                .fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = camcookColor,
                                                unfocusedBorderColor = camcookColor,
                                                cursorColor = camcookColor
                                            )
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            unidades.forEach { unidad ->
                                                DropdownMenuItem(
                                                    text = { Text(unidad) },
                                                    onClick = {
                                                        sel.unidad.value = unidad
                                                        expanded = false // ✅ cerrar menú al seleccionar
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = sel.precio.value,
                                        onValueChange = { nuevo ->
                                            val limpio = nuevo.trim().replace(",", ".")
                                            if (limpio.isEmpty() || limpio.matches(Regex("^\\d*\\.?\\d*\$"))) sel.precio.value = limpio
                                        },
                                        label = { Text("💰 Precio (Bs)", color = accentColor) },
                                        placeholder = { Text("Ej: 15.50") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = camcookColor,
                                            unfocusedBorderColor = camcookColor,
                                            cursorColor = camcookColor
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Botón publicar
                item {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (seleccionados.isEmpty()) {
                                mensaje = "⚠️ Selecciona al menos un ingrediente"
                                return@Button
                            }

                            // Validación básica
                            for (sel in seleccionados) {
                                val cant = sel.cantidad.value.toDoubleOrNull()
                                val precio = sel.precio.value.toDoubleOrNull()
                                if (sel.ingrediente.nombre.isNullOrBlank() ||
                                    cant == null || cant <= 0 ||
                                    precio == null || precio <= 0 ||
                                    sel.unidad.value.isBlank()
                                ) {
                                    mensaje = "⚠️ Completa todos los campos de '${sel.ingrediente.nombre}'"
                                    return@Button
                                }
                            }

                            // Guardar en Firestore
                            scope.launch {
                                try {
                                    val proveedorDoc = db.collection("proveedores").document(userId).get().await()
                                    if (!proveedorDoc.exists()) { mensaje = "⚠️ Primero debes registrarte como proveedor"; return@launch }
                                    val estado = proveedorDoc.getString("estado_validacion") ?: "pendiente"
                                    if (estado != "aprobado") { mensaje = "⏳ Tu cuenta aún no está aprobada"; return@launch }

                                    val proveedorRef = db.collection("proveedores").document(userId)
                                    seleccionados.forEach { sel ->
                                        val cantidad = sel.cantidad.value.replace(",", ".").toDoubleOrNull() ?: 0.0
                                        val precio = sel.precio.value.replace(",", ".").toDoubleOrNull() ?: 0.0
                                        val data = mapOf(
                                            "tipo" to "ingrediente",
                                            "nombre" to (sel.ingrediente.nombre ?: "").take(30),
                                            "cantidad" to cantidad,
                                            "unidad" to sel.unidad.value,
                                            "precio" to precio,
                                            "contacto" to contacto
                                        )
                                        proveedorRef.collection("productos").add(data).await()
                                    }
                                    ValidacionManager.asegurarRolProveedor(userId)
                                    mensaje = "✅ Ingredientes publicados correctamente"
                                    delay(1500)
                                    navController.popBackStack()
                                    navController.navigate("marketplace")
                                } catch (e: Exception) {
                                    mensaje = "❌ Error al publicar: ${e.message ?: "verifica conexión"}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = camcookColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Publicar ingredientes en el Marketplace", color = Color.White)
                    }
                }

                // Mensaje feedback
                mensaje?.let {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    it.startsWith("✅") -> Color(0xFFE8F5E9)
                                    it.startsWith("❌") -> Color(0xFFFFEBEE)
                                    else -> Color(0xFFFFF8E1)
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Text(it, modifier = Modifier.padding(12.dp), color = accentColor)
                        }
                    }
                }
            }
        }
    }
}


