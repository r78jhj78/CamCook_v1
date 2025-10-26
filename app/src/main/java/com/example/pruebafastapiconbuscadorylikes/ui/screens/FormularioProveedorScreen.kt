package com.example.pruebafastapiconbuscadorylikes.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafastapiconbuscadorylikes.data.manager.ValidacionManager
import com.example.pruebafastapiconbuscadorylikes.data.model.EmailSender
import com.example.pruebafastapiconbuscadorylikes.data.network.ImgBBApi
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProveedorScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: androidx.navigation.NavController,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var tipoProducto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var yaRegistrado by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }

    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imagenUri = uri
    }
    var expanded by remember { mutableStateOf(false) }

    val tipos = listOf("Frutas", "Verduras", "Carnes", "Huevos")
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tiposProveedor = listOf("Recetas completas", "Ingredientes")
    var tipoProveedor by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("proveedores").document(userId).get()
            .addOnSuccessListener { doc ->
                yaRegistrado = doc.exists()
                cargando = false
            }
            .addOnFailureListener {
                cargando = false
            }
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧾 Registro de Proveedor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del negocio") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción del negocio") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = tipoProveedor,
                    onValueChange = {},
                    label = { Text("Tipo de proveedor") },
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    tiposProveedor.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                tipoProveedor = tipo
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (imagenUri == null) "📷 Seleccionar imagen" else "✅ Imagen seleccionada")
            }


            imagenUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Número o link de WhatsApp") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (nombre.isNotEmpty() && tipoProveedor.isNotEmpty() && imagenUri != null) {
                        mensaje = "⏳ Subiendo imagen..."
                        scope.launch(Dispatchers.IO) {
                            val imageUrl = ImgBBApi.uploadImage(context, imagenUri!!)
                            if (imageUrl != null) {
                                val data = hashMapOf(
                                    "userId" to userId,
                                    "nombre" to nombre,
                                    "tipoProveedor" to tipoProveedor,
                                    "tipoProducto" to tipoProducto,
                                    "descripcion" to descripcion,
                                    "telefono" to telefono,
                                    "imagen" to imageUrl
                                )

                                db.collection("proveedores").document(userId)
                                    .set(data + mapOf("estado_validacion" to "pendiente"))
                                    .addOnSuccessListener {
                                        ValidacionManager.marcarPendiente(userId, "proveedor") { ok ->
                                            if (ok) {
                                                scope.launch {
                                                    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

                                                    EmailSender.sendAdminNotification(
                                                        context,
                                                        adminEmail = "equipodecamcook@gmail.com",
                                                        nombreUsuario = nombre,
                                                        tipo = "proveedor"
                                                    )

                                                    EmailSender.sendUserPendingEmail(
                                                        context,
                                                        userEmail = email,
                                                        nombreUsuario = nombre,
                                                        tipo = "proveedor"
                                                    )
                                                }
                                                mensaje = "📨 Se envió tu solicitud. Espera la validación del admin."
                                                scope.launch {
                                                    delay(2500)
                                                    if (tipoProveedor == "Recetas completas") {
                                                        navController.navigate("formulario_productos_receta")
                                                    } else {
                                                        navController.navigate("formulario_productos_ingredientes")
                                                    }
                                                }
                                            } else {
                                                mensaje = "⚠️ Error al marcar como pendiente"
                                            }
                                        }
                                    }
                                    .addOnFailureListener {
                                        mensaje = "❌ Error al registrar proveedor"
                                    }
                            } else {
                                mensaje = "❌ Error al subir imagen a ImgBB"
                            }
                        }
                    } else {
                        mensaje = "⚠️ Completa todos los campos y selecciona una imagen"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar proveedor")
            }

            mensaje?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
