package com.example.pruebafastapiconbuscadorylikes.ui

import android.util.Log
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebafastapiconbuscadorylikes.data.model.Usuario
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
class RecetasViewModel : ViewModel() {

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


    /** 🔹 Agregar vista a una receta */
    fun agregarVista(recetaId: String, uid: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.agregarVista(recetaId, ViewRequest(uid))
                println(response.message)
            } catch (e: Exception) {
                e.printStackTrace()
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

    fun getUserData(userId: String): Flow<Usuario> = flow {
        val doc = Firebase.firestore.collection("usuarios").document(userId).get().await()
        val data = doc.data
        if (data != null) {
            val usuario = Usuario(
                nombre = data["nombre"] as? String ?: "",
                email = data["email"] as? String ?: "",
                roles = data["roles"] as? List<String> ?: listOf("usuario"),
                likes = data["likes"] as? List<String> ?: emptyList(),
                vistas = data["vistas"] as? List<String> ?: emptyList()
            )
            emit(usuario)
        }
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


}
