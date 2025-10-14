package com.example.pruebafastapiconbuscadorylikes.ui.screens

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import coil.compose.AsyncImage

@Composable
fun CameraScreen(
    onImageCaptured: (File) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val outputDirectory = remember { context.cacheDir }
    val executor = ContextCompat.getMainExecutor(context)
    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    AndroidView(factory = {
        previewView.apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }, modifier = Modifier.fillMaxSize())

    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder().build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("Volver")
            }

            Button(onClick = {
                val photoFile = File(
                    outputDirectory,
                    "IMG_${System.currentTimeMillis()}.jpg"
                )

                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                imageCapture?.takePicture(
                    outputOptions,
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            exc.printStackTrace()
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            onImageCaptured(photoFile)
                        }
                    }
                )
            }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Tomar foto")
            }
        }
    }
}
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
                Button(onClick = {
                    onDismiss()
                    onVerReceta()
                }) {
                    Text("Ver receta completa")
                }
            },
            title = {
                Text(text = receta.titulo, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column {
                    if (receta.imagen_final_url.isNotBlank()) {
                        AsyncImage(
                            model = receta.imagen_final_url,
                            contentDescription = "Imagen de la receta",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Ingredientes principales:")
                    receta.ingredientes
                        .take(3)
                        .forEach { ingrediente ->
                            Text("- ${ingrediente.nombre ?: "Ingrediente desconocido"}")
                        }
                }
            }
        )
    }
}