package com.example.pruebafastapiconbuscadorylikes.data.network

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImgBBApi {

    private const val BASE_URL = "https://api.imgbb.com/"
    private const val API_KEY = "f77dd581f01f42c1a9fe2836b17320c0"

    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(ImgBBService::class.java)

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("upload", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
        return file
    }

    suspend fun uploadImage(context: Context, uri: Uri): String? {
        val file = uriToFile(context, uri)
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        return try {
            val response = service.uploadImage(API_KEY, body)
            if (response.success && response.data != null) {
                response.data.url
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private interface ImgBBService {
        @Multipart
        @POST("1/upload")
        suspend fun uploadImage(
            @Query("key") apiKey: String,
            @Part image: MultipartBody.Part
        ): ImgBBResponse
    }

    private data class ImgBBResponse(val data: ImgData?, val success: Boolean)
    private data class ImgData(val url: String)
}