package com.example.pruebafastapiconbuscadorylikes.data.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidacionAdminScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    val camcookColor = Color(0xFFFAA935)
    val accentColor = Color(0xFF8C7B6B)
    val backgroundColor = Color(0xFFF6F6F6)
    val cardColor = Color(0xFFFFF3E0)

    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val adminEmail = "equipodecamcook@gmail.com"
    val scope = rememberCoroutineScope()

    var pendientes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    if (currentUser?.email != adminEmail) {
        Box(
            Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "🚫 No tienes permisos para acceder a esta pantalla.",
                color = accentColor,
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    LaunchedEffect(Unit) {
        db.collection("proveedores")
            .whereEqualTo("estado_validacion", "pendiente")
            .addSnapshotListener { snapshot, _ ->
                snapshot ?: return@addSnapshotListener
                pendientes = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    data + mapOf("uid" to doc.id)
                }
                cargando = false
            }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "👑 Validación de Proveedores",
                        color = accentColor,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = camcookColor
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (cargando) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = camcookColor)
                }
            } else if (pendientes.isEmpty()) {
                Text(
                    "✅ No hay solicitudes pendientes.",
                    color = accentColor,
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pendientes) { prov ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("detalle_proveedor/${prov["uid"]}")
                                },
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "🏪 ${prov["nombre"] ?: "Sin nombre"}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                )
                                Text(
                                    "🧺 Tipo: ${prov["tipoProveedor"] ?: "No especificado"}",
                                    color = accentColor
                                )
                                Text(
                                    "📞 Teléfono: ${prov["telefono"] ?: "N/A"}",
                                    color = accentColor
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val ok = ValidacionManager.aprobarProveedor(prov["uid"].toString())
                                                if (ok) {
                                                    // Acción adicional
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = camcookColor,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Aprobar")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                val ok = ValidacionManager.rechazarProveedor(
                                                    prov["uid"].toString(),
                                                    motivo = "No cumple requisitos"
                                                )
                                                if (ok) {
                                                    // Acción adicional
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = accentColor
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Rechazar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}




