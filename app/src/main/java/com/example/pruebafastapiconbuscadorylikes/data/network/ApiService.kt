package com.example.pruebafastapiconbuscadorylikes.data.network
import com.example.pruebafastapiconbuscadorylikes.model.ApiResponse
import retrofit2.http.*

data class LikeRequest(val uid: String)
data class ViewRequest(val uid: String)
data class LikeResponse(val message: String)

interface ApiService {

    // Buscar recetas
    @GET("buscar")
    suspend fun buscarRecetas(
        @Query("query") query: String
    ): ApiResponse

    @POST("receta/{receta_id}/like")
    suspend fun darLike(
        @Path("receta_id") recetaId: String,
        @Body request: LikeRequest
    ): LikeResponse

    @POST("receta/{receta_id}/unlike")
    suspend fun quitarLike(
        @Path("receta_id") recetaId: String,
        @Body request: LikeRequest
    ): LikeResponse

    // Incrementar vistas
    @POST("receta/{id}/view")
    suspend fun agregarVista(
        @Path("id") recetaId: String,
        @Body body: ViewRequest
    ): LikeResponse
}