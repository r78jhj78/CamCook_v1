package com.example.pruebafastapiconbuscadorylikes

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pruebafastapiconbuscadorylikes.ui.RecetasViewModel
import com.example.pruebafastapiconbuscadorylikes.ui.screens.RecetasScreen
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.navigation.navArgument
import com.example.pruebafastapiconbuscadorylikes.auth.LoginScreen
import com.example.pruebafastapiconbuscadorylikes.auth.RegisterScreen
import com.example.pruebafastapiconbuscadorylikes.data.network.RetrofitClient
import com.example.pruebafastapiconbuscadorylikes.data.network.RoboflowService
import com.example.pruebafastapiconbuscadorylikes.navigation.Routes
import com.example.pruebafastapiconbuscadorylikes.ui.screens.CameraScreen
import com.example.pruebafastapiconbuscadorylikes.ui.screens.DetalleRecetaScreen
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.camera.view.PreviewView
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.ui.screens.PerfilScreen
import com.example.pruebafastapiconbuscadorylikes.ui.screens.PreviewRecetaDialog
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: RecetasViewModel = viewModel()
            var userId by remember { mutableStateOf("") }
            var recetaDetectada by remember { mutableStateOf<Receta?>(null) }
            var mostrarDialogo by remember { mutableStateOf(false) }

            NavHost(
                navController = navController,
                startDestination = Routes.LOGIN
            ) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLoginSuccess = { uid ->
                            userId = uid
                            navController.navigate(Routes.RECETAS)
                        },
                        onNavigateToRegister = {
                            navController.navigate(Routes.REGISTER)
                        }
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreen(
                        onRegisterSuccess = { uid ->
                            userId = uid
                            navController.navigate(Routes.RECETAS)
                        },
                        onNavigateToLogin = {
                            navController.popBackStack() // Vuelve al login
                        }
                    )
                }

                composable(Routes.RECETAS) {
                    RecetasScreen(
                        navController = navController,
                        viewModel = viewModel,
                        userId = userId,
                        onRecetaClick = { receta ->
                            val recetaJson = Uri.encode(Gson().toJson(receta))
                            navController.navigate("detalle_receta/${receta.id}")
                        },
                        onGoToProfile = {
                            // Navega a pantalla de perfil
                            navController.navigate("perfil")
                        },
                        onGoToFavorites = {
                            // Navega a pantalla favoritos
                            navController.navigate("favoritos")
                        },
                        onGoToSettings = {
                            // Navega a configuración
                            navController.navigate("configuracion")
                        },
                        onGoBackToInicio = {
                            // Regresa a esta misma pantalla "Recetas" limpiando el backstack si quieres
                            navController.navigate(Routes.RECETAS) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }


                composable(
                    route = "detalle_receta/{recetaId}",
                    arguments = listOf(navArgument("recetaId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val recetaId = backStackEntry.arguments?.getString("recetaId") ?: return@composable
                    val recetas by viewModel.recetas.collectAsState()
                    val receta = recetas.find { it.id == recetaId }
                    if (receta != null) {
                        DetalleRecetaScreen(
                            receta = receta,
                            onBack = { navController.popBackStack() },
                            onLike = { viewModel.darLike(receta.id, userId) }
                        )
                    }
                }

                composable("camera") {
                    val context = LocalContext.current
                    val roboflowService = remember { RoboflowService(context) }
                    val scope = rememberCoroutineScope()
                    var mostrarDialogoSimple by remember { mutableStateOf(false) }

                    var showDialog by remember { mutableStateOf(false) }
                    var dialogMessage by remember { mutableStateOf("") }

                    fun showDialogMessage(message: String) {
                        dialogMessage = message
                        showDialog = true
                    }

                    if (showDialog) {
                        PreviewRecetaDialog(
                            show = mostrarDialogo,
                            receta = recetaDetectada,
                            onDismiss = { mostrarDialogo = false },
                            onVerReceta = {
                                recetaDetectada?.let {
                                    navController.navigate("detalle_receta/${it.id}")
                                }
                            }
                        )
                    }

                    CameraScreen(
                        onImageCaptured = { photoFile ->
                            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                            roboflowService.detectarIngredientes(
                                bitmap = bitmap,
                                onSuccess = { detections ->
                                    if (detections.isNotEmpty()) {
                                        val ingrediente = detections[0].label

                                        viewModel.buscarRecetas(ingrediente) // Esto ya busca recetas y actualiza el StateFlow

                                        scope.launch {
                                            val recetasDetectadas = viewModel.recetas.first() // Espera el primer valor emitido
                                            val primera = recetasDetectadas.firstOrNull()

                                            if (primera != null) {
                                                recetaDetectada = primera
                                                mostrarDialogo = true
                                            } else {
                                                dialogMessage = "No se encontraron recetas con $ingrediente"
                                                mostrarDialogoSimple = true
                                            }
                                        }
                                    } else {
                                        dialogMessage = "No se detectó ningún ingrediente."
                                        mostrarDialogoSimple = true
                                    }
                                },
                                onError = { errorMsg ->
                                    dialogMessage = "Error al detectar: $errorMsg"
                                    mostrarDialogoSimple = true
                                }
                            )
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("perfil") {
                    PerfilScreen(
                        userId = userId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

            }

            }
        }
    }

@Composable
fun CameraScreen(
    onImageCaptured: (File) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    if (!hasPermission) {
        // Solicitar permiso (requiere Activity, aquí se usa un side effect)
        LaunchedEffect(Unit) {
            val activity = context as? androidx.activity.ComponentActivity
            activity?.let {
                it.requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
            }
        }
        Text("Necesitas dar permiso a la cámara para usar esta función.")
        return
    }

    CameraCaptureContent(
        onImageCaptured = onImageCaptured,
        onBack = onBack
    )
}

@Composable
fun CameraCaptureContent(
    onImageCaptured: (File) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // PreviewView para cámara
    val previewView = remember { PreviewView(context) }

    // Instancia ImageCapture
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // Executor para cámara
    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            Log.e("CameraScreen", "Error al iniciar cámara: ${exc.message}")
            Toast.makeText(context, "No se pudo iniciar la cámara.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Botón para capturar foto
        androidx.compose.material3.Button(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(16.dp),
            onClick = {
                // Crear archivo donde guardar imagen
                val photoFile = createImageFile(context)

                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            // Volver al hilo principal para callback
                            android.os.Handler(context.mainLooper).post {
                                onImageCaptured(photoFile)
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraScreen", "Error capturando imagen: ${exception.message}")
                            Toast.makeText(context, "Error capturando imagen.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        ) {
            Text(text = "Capturar")
        }

        // Botón para regresar
        androidx.compose.material3.Button(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.TopStart)
                .padding(16.dp),
            onClick = { onBack() }
        ) {
            Text("Atrás")
        }
    }
}

// Función para crear archivo de imagen con timestamp
fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val storageDir = context.cacheDir
    return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
}
