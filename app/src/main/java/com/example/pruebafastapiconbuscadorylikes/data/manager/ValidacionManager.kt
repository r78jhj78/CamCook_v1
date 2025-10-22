package com.example.pruebafastapiconbuscadorylikes.data.manager

import com.google.firebase.firestore.FirebaseFirestore

object ValidacionManager {

    private val db = FirebaseFirestore.getInstance()

    fun marcarPendiente(uid: String, tipo: String, onComplete: (Boolean) -> Unit) {
        val data = mapOf(
            "estado_validacion" to "pendiente",
            "rol_solicitado" to tipo
        )

        db.collection("usuarios").document(uid)
            .update(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun aprobarUsuario(uid: String, onComplete: (Boolean) -> Unit) {
        db.collection("usuarios").document(uid)
            .update(
                mapOf(
                    "estado_validacion" to "aprobado"
                )
            )
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
