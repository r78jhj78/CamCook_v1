package com.example.pruebafastapiconbuscadorylikes.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.data.network.RetrofitClient
import com.example.pruebafastapiconbuscadorylikes.data.network.LikeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.pruebafastapiconbuscadorylikes.data.network.ViewRequest

class RecetasViewModel : ViewModel() {

    private val _recetas = MutableStateFlow<List<Receta>>(emptyList())
    val recetas = _recetas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val listeners = mutableListOf<ListenerRegistration>()

    init {
        // Escuchar cambios en todas las recetas al iniciar el ViewModel
        escucharTodasRecetas()
    }

    /** 🔹 Buscar recetas usando tu API */
    fun buscarRecetas(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.api.buscarRecetas(query)
                _recetas.value = response.resultados
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
}
