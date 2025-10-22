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
    onBack: () -> Unit,
    onGoToProducts: () -> Unit
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

    val tipos = listOf("Frutas", "Verduras", "Carnes", "Huevos")
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

    if (yaRegistrado) {
        onGoToProducts()
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

            // Combo tipo de producto
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = tipoProducto,
                    onValueChange = {},
                    label = { Text("Tipo de producto") },
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    tipos.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                tipoProducto = tipo
                                expanded = false
                            }
                        )
                    }
                }
            }

            // 📸 Selector de imagen
            Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (imagenUri == null) "📷 Seleccionar imagen" else "✅ Imagen seleccionada")
            }

            // Preview de imagen seleccionada
            imagenUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            Button(
                onClick = {
                    if (nombre.isNotEmpty() && tipoProducto.isNotEmpty() && imagenUri != null) {
                        mensaje = "⏳ Subiendo imagen..."
                        scope.launch(Dispatchers.IO) {
                            val imageUrl = ImgBBApi.uploadImage(context, imagenUri!!)
                            if (imageUrl != null) {
                                val data = hashMapOf(
                                    "userId" to userId,
                                    "nombre" to nombre,
                                    "tipoProducto" to tipoProducto,
                                    "descripcion" to descripcion,
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
                                                    onGoToProducts()
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
