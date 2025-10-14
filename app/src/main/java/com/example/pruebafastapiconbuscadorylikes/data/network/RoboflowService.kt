package com.example.pruebafastapiconbuscadorylikes.data.network

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
class RoboflowService(private val context: Context) {
    private val modelEndpoint = "https://detect.roboflow.com/cookcamfood-jrrdw/25?api_key=wSRpQlKEKD5zEEiN0ywM"
    private val client = OkHttpClient()

    fun detectarIngredientes(
        bitmap: Bitmap,
        onSuccess: (List<DetectionResult>) -> Unit,
        onError: (String) -> Unit
    ) {
        // Redimensionar la imagen para no enviar demasiado peso
        val resizedBitmap = resizeBitmap(bitmap, 640, 640)

        // Convertir bitmap a byte array JPEG
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.jpg",
                imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(modelEndpoint)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("RoboflowService", "Error de red: ${e.message}")
                onError("Error de red: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    Log.d("RoboflowService", "Respuesta: $json")
                    val detections = parseDetections(json)
                    onSuccess(detections)
                } else {
                    onError("Error en respuesta: ${response.code}")
                }
            }
        })
    }

    private fun parseDetections(json: String?): List<DetectionResult> {
        val detections = mutableListOf<DetectionResult>()
        if (json == null) return detections

        try {
            val jsonObject = JSONObject(json)
            val predictions = jsonObject.getJSONArray("predictions")
            for (i in 0 until predictions.length()) {
                val obj = predictions.getJSONObject(i)
                val label = obj.getString("class")
                val confidence = obj.getDouble("confidence").toString()

                val x = obj.getDouble("x").toFloat()
                val y = obj.getDouble("y").toFloat()
                val width = obj.getDouble("width").toFloat()
                val height = obj.getDouble("height").toFloat()
                val boundingBox = RectF(x, y, x + width, y + height)

                detections.add(DetectionResult(boundingBox, label, confidence))
            }
        } catch (e: Exception) {
            Log.e("RoboflowService", "Error parseando JSON: ${e.message}")
        }
        return detections
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
}

// Modelo DetectionResult como antes
data class DetectionResult(
    val boundingBox: RectF,
    val label: String,
    val confidence: String
)
