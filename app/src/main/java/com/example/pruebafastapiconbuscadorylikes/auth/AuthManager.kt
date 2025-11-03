package com.example.pruebafastapiconbuscadorylikes.auth

import android.app.DownloadManager
import android.content.Context
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.pruebafastapiconbuscadorylikes.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    /*fun loginUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                firestore.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val estado = doc.getString("estado_validacion")
                        if (estado == "pendiente") {
                            onError("Tu cuenta está pendiente de validación por el administrador.")
                        } else {
                            onSuccess(uid)
                        }
                    }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Error al iniciar sesión") }
    }*/
    fun loginUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                ensureUserDocumentExists(uid, result.user, {
                    onSuccess(uid)
                }, onError)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al iniciar sesión")
            }
    }


    fun logout(context: Context) {
        val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("saved_email")
            apply()
        }

        // Cerrar sesión de Firebase
        auth.signOut()

        // Cerrar sesión de Google (por si el usuario entró con Google)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(context, gso)
        googleClient.signOut()
    }

    // ✅ Cambiar cuenta (Firebase + Google, pero NO borra correo)
    fun switchAccount(context: Context) {
        val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val lastEmail = sharedPref.getString("saved_email", null)

        with(sharedPref.edit()) {
            putString("last_email", lastEmail)
            apply()
        }

        // Cerrar sesión de Firebase y Google
        auth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(context, gso)
        googleClient.signOut()
    }

    fun resetPassword(email: String, onComplete: (Boolean) -> Unit) {
        FirebaseAuth.getInstance()
            .sendPasswordResetEmail(email)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
    fun getUserRoles(uid: String, onResult: (List<String>) -> Unit) {
        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val roles = doc.get("roles") as? List<String> ?: listOf("usuario")
                onResult(roles)
            }
            .addOnFailureListener { _ ->
                onResult(listOf("usuario"))
            }
    }
    private fun ensureUserDocumentExists(uid: String, user: FirebaseUser?, onComplete: () -> Unit, onError: (String) -> Unit) {
        val userRef = firestore.collection("usuarios").document(uid)

        userRef.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    // 🔹 Crear documento nuevo si no existe
                    val userData = hashMapOf(
                        "email" to (user?.email ?: ""),
                        "nombre" to (user?.displayName ?: ""),
                        "rol" to "usuario",
                        "vistas" to hashMapOf<String, Int>(),
                        "likes" to listOf<String>()
                    )
                    userRef.set(userData)
                        .addOnSuccessListener { onComplete() }
                        .addOnFailureListener { e -> onError("Error creando usuario: ${e.message}") }
                } else {
                    // 🔹 Si existe, asegurarse de que tenga los campos básicos
                    val updates = mutableMapOf<String, Any>()
                    if (!doc.contains("email")) updates["email"] = user?.email ?: ""
                    if (!doc.contains("nombre")) updates["nombre"] = user?.displayName ?: ""
                    if (!doc.contains("rol")) updates["rol"] = "usuario"

                    if (updates.isNotEmpty()) {
                        userRef.update(updates)
                            .addOnSuccessListener { onComplete() }
                            .addOnFailureListener { e -> onError("Error actualizando usuario: ${e.message}") }
                    } else {
                        onComplete()
                    }
                }
            }
            .addOnFailureListener { e ->
                onError("Error obteniendo datos de usuario: ${e.message}")
            }
    }
    fun loginWithGoogle(credential: AuthCredential, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                ensureUserDocumentExists(uid, result.user, {
                    onSuccess(uid)
                }, onError)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error iniciando con Google")
            }
    }

}