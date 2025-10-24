package com.example.pruebafastapiconbuscadorylikes.ui.screens

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.pruebafastapiconbuscadorylikes.data.network.ClarifaiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CameraScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val outputDirectory = remember { context.cacheDir }
    val executor = ContextCompat.getMainExecutor(context)
    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    var cargando by remember { mutableStateOf(false) }
    var alimentoDetectado by remember { mutableStateOf<String?>(null) }
    val clarifaiService = remember { ClarifaiService() }

    AndroidView(factory = {
        previewView.apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
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
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        Row(
            Modifier.padding(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("Volver")
            }

            Button(onClick = {
                val photoFile = File(outputDirectory, "IMG_${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                imageCapture?.takePicture(
                    outputOptions,
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Toast.makeText(context, "Error al guardar foto", Toast.LENGTH_SHORT).show()
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            cargando = true
                            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                            clarifaiService.detectarAlimento(bitmap) { alimento ->
                                cargando = false
                                if (alimento != null) {
                                    alimentoDetectado = alimento
                                    Toast.makeText(context, "🍎 Detectado: $alimento", Toast.LENGTH_LONG).show()

                                    navController.navigate("recetas/${alimento}")
                                } else {
                                    Toast.makeText(context, "No se detectó alimento", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Tomar foto")
            }
        }
    }
}
