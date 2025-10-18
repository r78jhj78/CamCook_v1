package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pruebafastapiconbuscadorylikes.model.Receta

@Composable
fun PreviewRecetaDialog(
    show: Boolean,
    receta: Receta?,
    onDismiss: () -> Unit,
    onVerReceta: () -> Unit
) {
    if (show && receta != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onVerReceta) {
                    Text("Ver detalle")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            },
            title = {
                Text(
                    text = receta.titulo ?: "Receta detectada",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Ingredientes: ${receta.ingredientes?.joinToString(", ") ?: "No especificados"}",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Descripción: ${receta.descripcion ?: "Sin descripción"}",
                        fontSize = 14.sp
                    )
                }
            }
        )
    }
}
