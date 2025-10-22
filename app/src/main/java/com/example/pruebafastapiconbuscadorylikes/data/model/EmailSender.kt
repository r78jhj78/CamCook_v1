package com.example.pruebafastapiconbuscadorylikes.data.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

object EmailSender {
    suspend fun sendWelcomeEmail(context: Context, email: String, nombre: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

                val json = JSONObject().apply {
                    put("service_id", "SERVICE_ID_AQUI")
                    put("template_id", "TEMPLATE_ID_AQUI")
                    put("user_id", "PUBLIC_KEY_AQUI")
                    put("template_params", JSONObject().apply {
                        put("to_email", email)
                        put("user_name", nombre)
                        put("message", "Bienvenido a CamCook, $nombre! 🍳✨\nGracias por unirte a nuestra comunidad.")
                    })
                }

                val body = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    json.toString()
                )

                val request = Request.Builder()
                    .url("https://api.emailjs.com/api/v1.0/email/send")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { it.isSuccessful }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    suspend fun sendAdminNotification(
        context: Context,
        adminEmail: String,
        nombreUsuario: String,
        tipo: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

                val json = JSONObject().apply {
                    put("service_id", "service_k33o40i")
                    put("template_id", "template_5n8f4lu")
                    put("user_id", "4oI8CDn8RCVohwX3urxwV")

                    put("template_params", JSONObject().apply {
                        put("to_email", adminEmail)
                        put("subject", "Nuevo $tipo pendiente de validación")
                        put("message", """
                        El usuario $nombreUsuario ha completado su perfil como $tipo.
                        Revisa el panel de Firestore para aprobarlo o rechazarlo.
                    """.trimIndent())
                    })
                }

                val body = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    json.toString()
                )

                val request = Request.Builder()
                    .url("https://api.emailjs.com/api/v1.0/email/send")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { it.isSuccessful }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    suspend fun sendUserPendingEmail(
        context: Context,
        userEmail: String,
        nombreUsuario: String,
        tipo: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

                val json = JSONObject().apply {
                    put("service_id", "service_k33o40i")
                    put("template_id", "template_user_espera")
                    put("user_id", "4oI8CDn8RCVohwX3urxwV")
                    put("template_params", JSONObject().apply {
                        put("to_email", userEmail)
                        put("user_name", nombreUsuario)
                        put("tipo", tipo)
                    })
                }

                val body = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    json.toString()
                )

                val request = Request.Builder()
                    .url("https://api.emailjs.com/api/v1.0/email/send")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { it.isSuccessful }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
