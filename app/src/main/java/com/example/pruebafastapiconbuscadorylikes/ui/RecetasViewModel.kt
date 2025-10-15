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

    private val _interacciones = MutableStateFlow<InteraccionesResponse?>(null)
    val interacciones = _interacciones.asStateFlow()
    private val _vistasPorReceta = MutableStateFlow<Map<String, Int>>(emptyMap())
    val vistasPorReceta: StateFlow<Map<String, Int>> = _vistasPorReceta

    private val _titulosVistas = MutableStateFlow<Map<String, String>>(emptyMap())
    val titulosVistas: StateFlow<Map<String, String>> = _titulosVistas


    private val _recetas = MutableStateFlow<List<Receta>>(emptyList())
    val recetas = _recetas.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val listeners = mutableListOf<ListenerRegistration>()
    private val firestore = FirebaseFirestore.getInstance()


    init {
        // Escuchar cambios en todas las recetas al iniciar el ViewModel
        escucharTodasRecetas()
    }

    /** 🔹 Buscar recetas usando tu API */
    fun buscarRecetas(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 🔸 1. Buscar en OpenSearch -> obtener IDs
                val response = RetrofitClient.api.buscarRecetas(query)
                val ids = response.ids

                // 🔸 2. Limpiar escuchas anteriores si quieres
                listeners.forEach { it.remove() }
                listeners.clear()

                // 🔸 3. Obtener recetas desde Firestore usando los IDs
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

    /** 🔹 Dar like a una receta */
    fun darLike(recetaId: String, uid: String) {
        viewModelScope.launch {
            try {
                // Actualiza localmente primero
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

                // Luego envía al servidor
                val response = RetrofitClient.api.darLike(recetaId, LikeRequest(uid))
                println(response.message)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun registrarVista(recetaId: String, uid: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.agregarVista(recetaId, ViewRequest(uid))
                Log.d("FastAPI", "✅ Vista registrada en backend para $recetaId")

                // Actualización local opcional:
                val current = _vistasPorReceta.value.toMutableMap()
                current[recetaId] = (current[recetaId] ?: 0) + 1
                _vistasPorReceta.value = current

            } catch (e: Exception) {
                Log.e("FastAPI", "❌ Error al registrar vista: ${e.message}")
            }
        }
    }






    /** 🔹 Escuchar toda la colección de recetas en tiempo real */
    fun escucharTodasRecetas() {
        val recetasRef = FirebaseFirestore.getInstance().collection("recetas")
        val listener = recetasRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val recetasActualizadas = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Receta::class.java)?.copy(id = doc.id)
                }
                _recetas.value = recetasActualizadas // ✅ actualiza el flujo
            }
        }
        listeners.add(listener)
    }

    override fun onCleared() {
        super.onCleared()
        // Cancelar todos los listeners al destruir el ViewModel
        listeners.forEach { it.remove() }
    }
/*
    fun sumarVistaReceta(recetaId: String) {
        viewModelScope.launch {
            val recetaRef = firestore.collection("recetas").document(recetaId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(recetaRef)
                val vistasActuales = snapshot.getLong("popup_clicks") ?: 0
                transaction.update(recetaRef, "popup_clicks", vistasActuales + 1)
            }.addOnSuccessListener {
                println("✅ Vista incrementada correctamente.")
            }.addOnFailureListener {
                it.printStackTrace()
            }
        }
    }
*/
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

    fun agregarRol(userId: String, nuevoRol: String, onComplete: () -> Unit) {
        val db = Firebase.firestore
        val docRef = db.collection("usuarios").document(userId)

        docRef.update("roles", FieldValue.arrayUnion(nuevoRol))
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { Log.e("Firebase", "Error agregando rol", it) }
    }

    fun quitarRol(userId: String, rol: String, onComplete: () -> Unit) {
        if (rol == "usuario") return // No permitir eliminar "usuario"

        val db = Firebase.firestore
        val docRef = db.collection("usuarios").document(userId)

        docRef.update("roles", FieldValue.arrayRemove(rol))
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { Log.e("Firebase", "Error quitando rol", it) }
    }


    fun cambiarRol(userId: String, nuevoRol: String, onComplete: () -> Unit) {
        val db = Firebase.firestore
        db.collection("usuarios").document(userId)
            .update("rol", nuevoRol)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { Log.e("Firebase", "Error actualizando rol", it) }
    }

    fun obtenerVistasDeUsuario(uid: String, onResult: (Map<String, Int>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").document(uid).collection("vistas")
            .get()
            .addOnSuccessListener { snapshot ->
                val conteo = mutableMapOf<String, Int>()
                for (doc in snapshot.documents) {
                    val recetaId = doc.id
                    val contador = doc.getLong("contador")?.toInt() ?: 1
                    conteo[recetaId] = contador
                }
                onResult(conteo)
            }
    }

    fun obtenerRecetaPorId(recetaId: String, onSuccess: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("recetas").document(recetaId).get()
            .addOnSuccessListener { doc ->
                val titulo = doc.getString("titulo") ?: recetaId
                onSuccess(titulo)
            }
    }

    /*fun registrarVistaDeUsuario(uid: String, recetaId: String) {
        val userRef = firestore.collection("usuarios").document(uid)
        val vistaRef = userRef.collection("vistas").document(recetaId)

        firestore.runTransaction { transaction ->
            val doc = transaction.get(vistaRef)
            val nuevoConteo = if (doc.exists()) {
                val actual = doc.getLong("contador") ?: 0
                actual + 1
            } else 1

            // ✅ Actualiza subcolección
            transaction.set(vistaRef, mapOf("contador" to nuevoConteo))

            // ✅ Actualiza campo vistas del documento principal
            transaction.update(userRef, "vistas.$recetaId", nuevoConteo)
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }*/

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

                // Obtener títulos de las recetas
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
}
