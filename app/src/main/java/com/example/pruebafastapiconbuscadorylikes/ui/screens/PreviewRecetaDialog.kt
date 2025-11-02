package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pruebafastapiconbuscadorylikes.model.Receta
@Composable
fun PreviewRecetaDialog(
    show: Boolean,
    receta: Receta?,
    onDismiss: () -> Unit,
    onVerReceta: () -> Unit
) {
    val camcookColor = Color(0xFFFAA935)
    val accentColor = Color(0xFF8C7B6B)
    val backgroundColor = Color(0xFFF6F6F6)
    val cardColor = Color(0xFFFFF3E0)

    if (show && receta != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = onVerReceta,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = camcookColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ver detalle")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar", color = accentColor)
                }
            },
            title = {
                Text(
                    text = receta.titulo.ifEmpty { "Receta detectada" },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .background(cardColor, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🧂 Ingredientes:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    )
                    Text(
                        text = receta.ingredientes.joinToString(", ") { it.nombre ?: "" },
                        fontSize = 14.sp,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📖 Descripción:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    )
                    Text(
                        text = receta.descripcion.ifEmpty { "Sin descripción" },
                        fontSize = 14.sp,
                        color = accentColor
                    )
                }
            },
            containerColor = backgroundColor,
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
