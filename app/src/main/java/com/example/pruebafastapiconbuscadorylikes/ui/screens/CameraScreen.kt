package com.example.pruebafastapiconbuscadorylikes.ui.screens

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onBack: () -> Unit,
    rol: String = "viewer"
) {
    val camcookColor = Color(0xFFFAA935)
    val accentColor = Color(0xFF8C7B6B)
    val backgroundColor = Color(0xFFF6F6F6)

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

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                previewView.apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = camcookColor)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(backgroundColor.copy(alpha = 0.9f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Volver")
            }

            Button(
                onClick = {
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
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = camcookColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Tomar foto")
                Spacer(Modifier.width(8.dp))
                Text("Capturar")
            }
        }
    }

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
}
