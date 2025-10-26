package com.example.pruebafastapiconbuscadorylikes.ui

import android.R.attr.data
import android.util.Log
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebafastapiconbuscadorylikes.data.model.Usuario
import com.example.pruebafastapiconbuscadorylikes.data.network.InteraccionesResponse
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.data.network.RetrofitClient
import com.example.pruebafastapiconbuscadorylikes.data.network.LikeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.pruebafastapiconbuscadorylikes.data.network.ViewRequest
import com.example.pruebafastapiconbuscadorylikes.model.Ingrediente
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class RecetasViewModel : ViewModel() {
    private val _ingredientes = MutableStateFlow<List<Ingrediente>>(emptyList())
    val ingredientes: StateFlow<List<Ingrediente>> = _ingredientes
    private val _interacciones = MutableStateFlow<InteraccionesResponse?>(null)
    val interacciones = _interacciones.asStateFlow()
    private val _vistasPorReceta = MutableStateFlow<Map<String, Int>>(emptyMap())
    val vistasPorReceta: StateFlow<Map<String, Int>> = _vistasPorReceta

    private val _titulosVistas = MutableStateFlow<Map<String, String>>(emptyMap())
    val titulosVistas: StateFlow<Map<String, String>> = _titulosVistas
    private var bloqueandoSnapshot = false

    private val _recetas = MutableStateFlow<List<Receta>>(emptyList())
    val recetas = _recetas.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val listeners = mutableListOf<ListenerRegistration>()
    private val firestore = FirebaseFirestore.getInstance()


    init {
        escucharTodasRecetas()
    }

    fun buscarRecetas(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.api.buscarRecetas(query)
                val ids = response.ids

                listeners.forEach { it.remove() }
                listeners.clear()

                val db = FirebaseFirestore.getInstance()
                val recetasRef = db.collection("recetas")

                val recetas = ids.mapNotNull { id ->
                    try {
                        val doc = recetasRef.document(id).get().await()
                        doc.toObject(Receta::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }

                _recetas.value = recetas

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    /*fun darLike(recetaId: String, uid: String) {
        viewModelScope.launch {
            try {
                bloqueandoSnapshot = true

                _recetas.value = _recetas.value.map { receta ->
                    if (receta.id == recetaId) {
                        val yaDioLike = receta.liked_by.containsKey(uid)
                        val nuevosLikes = if (yaDioLike) receta.likes - 1 else receta.likes + 1
                        val nuevoMapa = receta.liked_by.toMutableMap().apply {
                            if (yaDioLike) remove(uid) else put(uid, true)
                        }
                        receta.copy(likes = nuevosLikes, liked_by = nuevoMapa)
                    } else receta
                }

                val receta = _recetas.value.find { it.id == recetaId }
                val yaDioLike = receta?.liked_by?.containsKey(uid) == true

                if (yaDioLike) {
                    RetrofitClient.api.darLike(recetaId, LikeRequest(uid))
                } else {
                    RetrofitClient.api.quitarLike(recetaId, LikeRequest(uid))
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                bloqueandoSnapshot = false
            }
        }
    }*/
    fun darLike(recetaId: String, uid: String) {
        viewModelScope.launch {
            try {
                bloqueandoSnapshot = true

                val recetaActual = _recetas.value.find { it.id == recetaId }
                val yaDioLikeAntes = recetaActual?.liked_by?.containsKey(uid) == true

                if (yaDioLikeAntes) {
                    RetrofitClient.api.quitarLike(recetaId, LikeRequest(uid))
                } else {
                    RetrofitClient.api.darLike(recetaId, LikeRequest(uid))
                }

                _recetas.value = _recetas.value.map { receta ->
                    if (receta.id == recetaId) {
                        val nuevosLikes = if (yaDioLikeAntes) {
                            (receta.likes - 1).coerceAtLeast(0)
                        } else {
                            receta.likes + 1
                        }

                        val nuevoMapa = receta.liked_by.toMutableMap().apply {
                            if (yaDioLikeAntes) remove(uid) else put(uid, true)
                        }

                        receta.copy(likes = nuevosLikes, liked_by = nuevoMapa)
                    } else receta
                }

                Log.d("Like", "❤️ Like actualizado correctamente (${if (yaDioLikeAntes) "quitado" else "añadido"})")

            } catch (e: Exception) {
                Log.e("Like", "❌ Error al dar like: ${e.message}")
            } finally {
                bloqueandoSnapshot = false
            }
        }
    }


    fun registrarVista(recetaId: String, uid: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.agregarVista(recetaId, ViewRequest(uid))
                Log.d("FastAPI", "✅ Vista registrada en backend para $recetaId")

                val current = _vistasPorReceta.value.toMutableMap()
                current[recetaId] = (current[recetaId] ?: 0) + 1
                _vistasPorReceta.value = current

            } catch (e: Exception) {
                Log.e("FastAPI", "❌ Error al registrar vista: ${e.message}")
            }
        }
    }

    fun escucharTodasRecetas() {
        val recetasRef = FirebaseFirestore.getInstance().collection("recetas")
        val listener = recetasRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !bloqueandoSnapshot) {
                val recetasActualizadas = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Receta::class.java)?.copy(id = doc.id)
                }
                _recetas.value = recetasActualizadas
            }
        }
        listeners.add(listener)
    }


    override fun onCleared() {
        super.onCleared()

        listeners.forEach { it.remove() }
    }
    fun getUserData(userId: String): Flow<Usuario> = callbackFlow {
        val listener = firestore.collection("usuarios").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    val vistasMap = data?.get("vistas") as? Map<String, Long> ?: emptyMap()
                    val usuario = Usuario(
                        nombre = data?.get("nombre") as? String ?: "",
                        email = data?.get("email") as? String ?: "",
                        roles = data?.get("roles") as? List<String> ?: listOf("usuario"),
                        likes = data?.get("likes") as? List<String> ?: emptyList(),
                        vistas = vistasMap.mapValues { it.value.toInt() }
                    )
                    trySend(usuario)
                }
            }
        awaitClose { listener.remove() }
    }

    fun obtenerRecetaPorId(recetaId: String, onSuccess: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("recetas").document(recetaId).get()
            .addOnSuccessListener { doc ->
                val titulo = doc.getString("titulo") ?: recetaId
                onSuccess(titulo)
            }
    }

    fun escucharVistasConTitulos(uid: String) {
        val vistasRef = firestore.collection("usuarios").document(uid).collection("vistas")
        vistasRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val vistasMap = mutableMapOf<String, Int>()
                snapshot.documents.forEach { doc ->
                    val recetaId = doc.id
                    val contador = doc.getLong("contador")?.toInt() ?: 1
                    vistasMap[recetaId] = contador
                }

                _vistasPorReceta.value = vistasMap

                val titulosMap = mutableMapOf<String, String>()
                vistasMap.keys.forEach { recetaId ->
                    obtenerRecetaPorId(recetaId) { titulo ->
                        titulosMap[recetaId] = titulo
                        _titulosVistas.value = titulosMap
                    }
                }
            }
        }
    }


    fun cargarInteracciones(uid: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getInteracciones(uid)
                _interacciones.value = response
                Log.d("Perfil", "✅ Interacciones cargadas: vistas=${response.vistas.size}, likes=${response.likes.size}")
            } catch (e: Exception) {
                Log.e("Perfil", "❌ Error al obtener interacciones", e)
            }
        }
    }

    fun escucharTodosIngredientes() {
        firestore.collection("ingredientes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ Error al escuchar ingredientes: ${error.message}")
                    return@addSnapshotListener
                }
                val lista = snapshot?.toObjects(Ingrediente::class.java) ?: emptyList()
                _ingredientes.value = lista
            }
    }
    fun buscarIngrediente(nombre: String) {
        if (nombre.isBlank()) {
            escucharTodosIngredientes()
            return
        }

        firestore.collection("ingredientes")
            .whereGreaterThanOrEqualTo("nombre", nombre)
            .whereLessThanOrEqualTo("nombre", nombre + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(Ingrediente::class.java)
                _ingredientes.value = lista
            }
            .addOnFailureListener { e ->
                println("❌ Error al buscar ingrediente: ${e.message}")
            }
    }
}
