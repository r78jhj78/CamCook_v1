package com.example.pruebafastapiconbuscadorylikes.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser

    fun registerUser(
        nombre: String,
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                val userData = hashMapOf(
                    "nombre" to nombre,
                    "email" to email,
                    "rol" to "usuario"
                )
                firestore.collection("usuarios").document(uid).set(userData)
                    .addOnSuccessListener { onSuccess(uid) }
                    .addOnFailureListener { e -> onError(e.message ?: "Error Firestore") }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Error al registrar") }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                onSuccess(uid)
            }
            .addOnFailureListener { e -> onError(e.message ?: "Error al iniciar sesión") }
    }

    fun logout() {
        auth.signOut()
    }
}
