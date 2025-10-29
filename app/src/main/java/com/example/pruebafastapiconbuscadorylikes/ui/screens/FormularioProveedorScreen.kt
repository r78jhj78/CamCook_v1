package com.example.pruebafastapiconbuscadorylikes.ui.screens

import android.net.Uri
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProveedorScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: androidx.navigation.NavController,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipoProveedor by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }

    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var formularioProveedorLleno by remember { mutableStateOf(false) }

    val tiposProveedor = listOf("Recetas completas", "Ingredientes")
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val doc = db.collection("proveedores").document(userId).get().await()
            formularioProveedorLleno = doc.getBoolean("formulario_completado") ?: false
            val formularioCompletado = doc.getBoolean("formulario_completado") ?: false
            val estado = doc.getString("estado_validacion") ?: ""

            formularioProveedorLleno = formularioCompletado

            if (formularioCompletado && estado != "aprobado") {
                mensaje = "⏳ Tu cuenta de proveedor está en revisión. Espera aprobación del administrador."
            }
        } catch (e: Exception) {
            formularioProveedorLleno = false
            Log.e("ProveedorScreen", "Error al cargar proveedor: ${e.message}")
        } finally {
            cargando = false
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imagenUri = uri
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (mensaje?.contains("revisión") == true) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(mensaje ?: "", color = MaterialTheme.colorScheme.primary)
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
            if (!formularioProveedorLleno) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { input ->
                        val cleaned = input
                            .replace(Regex("\\s{2,}"), " ")
                            .trimStart()
                            .take(31)

                        nombre = cleaned
                    },
                    label = { Text("Nombre del negocio (máx. 31 caracteres)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text("${nombre.length}/31")
                    }
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
                                        "descripcion" to descripcion,
                                        "telefono" to telefono,
                                        "imagen" to imageUrl,
                                        "formulario_completado" to true,
                                        "estado_validacion" to "pendiente"
                                    )

                                    db.collection("proveedores").document(userId)
                                        .set(data, SetOptions.merge())
                                        .addOnSuccessListener {
                                            scope.launch {
                                                val ok = ValidacionManager.marcarPendienteProveedor(userId)
                                                if (ok) {
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

                                                    mensaje = "📨 Solicitud enviada. Espera validación."

                                                    delay(2500)
                                                    if (tipoProveedor == "Recetas completas") {
                                                        navController.navigate("formulario_productos_receta")
                                                    } else {
                                                        navController.navigate("formulario_productos_ingredientes")
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
                                    mensaje = "❌ Error al subir imagen"
                                }
                            }
                        } else {
                            mensaje = "⚠️ Completa todos los campos e imagen"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar proveedor")
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Selecciona qué formulario deseas llenar:", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { navController.navigate("formulario_productos_ingredientes") }) {
                        Text("Ingredientes")
                    }
                    Button(onClick = { navController.navigate("formulario_productos_receta") }) {
                        Text("Recetas completas")
                    }
                }
            }

            mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
    }
}
