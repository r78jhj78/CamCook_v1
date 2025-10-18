package com.example.pruebafastapiconbuscadorylikes.data.network

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Locale

class ClarifaiService {

    private val apiKey = "30ca978ea11c49f78cd373476d73eb39" // tu clave Clarifai
    private val client = OkHttpClient()

    fun detectarAlimento(bitmap: Bitmap, callback: (String?) -> Unit) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val imageBytes = stream.toByteArray()
        val imageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val jsonBody = """
            {
              "user_app_id": {"user_id": "clarifai", "app_id": "main"},
              "inputs": [
                {"data": {"image": {"base64": "$imageBase64"}}}
              ]
            }
        """.trimIndent()

        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.clarifai.com/v2/users/clarifai/apps/main/models/food-item-recognition/outputs")
            .addHeader("Authorization", "Key $apiKey")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e("ClarifaiService", "Error: ${e.message}")
                Handler(Looper.getMainLooper()).post { callback(null) }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string()
                val alimento = parseResponse(jsonString)
                Handler(Looper.getMainLooper()).post {
                    callback(alimento)
                }
            }
        })
    }

    private fun parseResponse(jsonString: String?): String? {
        if (jsonString == null) return null
        return try {
            val json = JSONObject(jsonString)
            val outputs = json.getJSONArray("outputs")
            val concepts = outputs.getJSONObject(0)
                .getJSONObject("data")
                .getJSONArray("concepts")

            if (concepts.length() > 0) {
                val name = concepts.getJSONObject(0).getString("name")
                traducirAlimento(name)
            } else null
        } catch (e: Exception) {
            Log.e("ClarifaiService", "Parse error: ${e.message}")
            null
        }
    }

    fun traducirAlimento(nombre: String): String {
        val traducciones = mapOf(
            "chicken" to "pollo",
            "egg" to "huevo",
            "beef" to "carne de res",
            "pork" to "cerdo",
            "rice" to "arroz",
            "peanut" to "maní",
            "broccoli" to "brócoli",
            "lettuce" to "lechuga",
            "tomato" to "tomate",
            "apple" to "manzana",
            "banana" to "banana",
            "strawberry" to "frutilla",
            "carrot" to "zanahoria",
            "onion" to "cebolla",
            "cheese" to "queso",
            "fish" to "pescado",
            "potato" to "papa",
            "milk" to "leche",
            "bread" to "pan",
            "butter" to "mantequilla",
            "corn" to "maíz",
            "cucumber" to "pepino",
            "grape" to "uva",
            "orange" to "naranja",
            "pear" to "pera"
        )

        return traducciones[nombre] ?: nombre
    }
}
