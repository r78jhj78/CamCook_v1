package com.example.pruebafastapiconbuscadorylikes.data.manager

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object ValidacionManager {

    private val db = FirebaseFirestore.getInstance()

    suspend fun registrarProveedorPendiente(
        userId: String,
        nombre: String,
        tipoProveedor: String,
        descripcion: String,
        telefono: String,
        imagen: String
    ): Boolean {
        return try {
            val data = mapOf(
                "userId" to userId,
                "nombre" to nombre,
                "tipoProveedor" to tipoProveedor,
                "descripcion" to descripcion,
                "telefono" to telefono,
                "imagen" to imagen,
                "formulario_completado" to true,
                "estado_validacion" to "pendiente"
            )
            db.collection("proveedores").document(userId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun agregarProductoProveedor(
        userId: String,
        tipo: String,
        nombre: String,
        precio: String,
        cantidad: String? = null,
        unidad: String? = null,
        imagen: String? = null
    ): Boolean {
        return try {
            val producto = mutableMapOf<String, Any>(
                "tipo" to tipo,
                "nombre" to nombre,
                "precio" to precio
            )
            if (!cantidad.isNullOrEmpty()) producto["cantidad"] = cantidad
            if (!unidad.isNullOrEmpty()) producto["unidad"] = unidad
            if (!imagen.isNullOrEmpty()) producto["imagen"] = imagen

            db.collection("proveedores").document(userId)
                .collection("productos")
                .add(producto)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun actualizarEstadoProveedor(
        userId: String,
        nuevoEstado: String,
        motivo: String? = null
    ): Boolean {
        return try {
            val data = mutableMapOf<String, Any>(
                "estado_validacion" to nuevoEstado
            )
            if (!motivo.isNullOrEmpty()) data["motivo_rechazo"] = motivo

            db.collection("proveedores").document(userId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerProveedoresPendientes(): List<Map<String, Any>> {
        return try {
            val snap = db.collection("proveedores")
                .whereEqualTo("estado_validacion", "pendiente")
                .get()
                .await()
            snap.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtenerProveedoresAprobados(): List<Map<String, Any>> {
        return try {
            val snap = db.collection("proveedores")
                .whereEqualTo("estado_validacion", "aprobado")
                .get()
                .await()
            snap.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun aprobarProveedor(userId: String): Boolean {
        return actualizarEstadoProveedor(userId, "aprobado")
    }

    suspend fun rechazarProveedor(userId: String, motivo: String): Boolean {
        return actualizarEstadoProveedor(userId, "rechazado", motivo)
    }

    suspend fun marcarPendienteProveedor(userId: String): Boolean {
        return actualizarEstadoProveedor(userId, "pendiente")
    }
}
