package com.example.pruebafastapiconbuscadorylikes.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
//import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.material3.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.draw.clip
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProveedorScreen(
    userId: String,
    viewModel: RecetasViewModel,
    navController: NavController,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var estadoValidacion by remember { mutableStateOf<String?>(null) }
    var formularioProveedorLleno by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cargar datos del proveedor
    LaunchedEffect(Unit) {
        try {
            val doc = db.collection("proveedores").document(userId).get().await()
            formularioProveedorLleno = doc.getBoolean("formulario_completado") ?: false
            estadoValidacion = doc.getString("estado_validacion") ?: ""
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

    val camcookColor = Color(0xFFFAA935)
    val accentColor = Color(0xFF8C7B6B)

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧾 Registro de Proveedor", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = camcookColor,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            when {
                // 🧾 Si no está registrado todavía
                !formularioProveedorLleno -> {
                    Text("🧾 Registro de proveedor", style = MaterialTheme.typography.titleMedium, color = camcookColor)

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it.take(200) },
                        label = { Text("Nombre del negocio") },
                        placeholder = { Text("Ej: Sabores Andinos") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = camcookColor,
                            cursorColor = camcookColor
                        )
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { nuevo ->
                            val lineCount = nuevo.count { it == '\n' } + 1
                            if (lineCount <= 5 && nuevo.length <= 500) {
                                descripcion = nuevo
                            }
                        },
                        label = { Text("Descripción del negocio") },
                        placeholder = { Text("Ej: Especialidades andinas") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp), // Ajusta la altura para 5 líneas
                        maxLines = 5,
                    )

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Número o link de WhatsApp") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = camcookColor)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (imagenUri == null) "Seleccionar imagen" else "Imagen seleccionada", color = Color.White)
                    }

                    imagenUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray)
                        )
                    }

                    Button(
                        onClick = {
                            if (nombre.isNotEmpty() && imagenUri != null) {
                                mensaje = "⏳ Subiendo imagen..."
                                scope.launch(Dispatchers.IO) {
                                    val imageUrl = ImgBBApi.uploadImage(context, imagenUri!!)
                                    if (imageUrl != null) {
                                        val data = hashMapOf(
                                            "userId" to userId,
                                            "nombre" to nombre,
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

                                                    withContext(Dispatchers.Main) {
                                                        mensaje = "📨 Solicitud enviada. Espera validación del administrador."
                                                        formularioProveedorLleno = true
                                                        estadoValidacion = "pendiente"
                                                    }
                                                }
                                            }
                                    } else {
                                        mensaje = "❌ Error al subir imagen"
                                    }
                                }
                            } else {
                                mensaje = "⚠️ Completa todos los campos e imagen"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = camcookColor)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar proveedor", color = Color.White)
                    }
                }

                // 🕓 Si ya se registró pero está pendiente
                estadoValidacion == "pendiente" -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = camcookColor)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "⏳ Tu cuenta está en revisión. Espera la aprobación del administrador.",
                                color = accentColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // ✅ Si ya está aprobado
                estadoValidacion == "aprobado" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "✅ Tu cuenta de proveedor fue aprobada",
                            color = accentColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "¿Qué deseas publicar?",
                            color = camcookColor,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(24.dp))

                        // 🧂 Tarjeta Ingredientes
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clickable { navController.navigate("formulario_productos_ingredientes") },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Ingredientes",
                                    tint = camcookColor,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        "🧂 Publicar Ingredientes",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                                    )
                                    Text(
                                        "Vende tus ingredientes individuales.",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 🍽 Tarjeta Recetas completas
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clickable { navController.navigate("formulario_productos_receta") },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEFD5)),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Restaurant,
                                    contentDescription = "Recetas",
                                    tint = camcookColor,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        "🍽 Publicar Recetas Completas",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                                    )
                                    Text(
                                        "Vende la receta lista para disfrutar.",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            mensaje?.let {
                Text(it, color = accentColor)
            }
        }
    }
}


