package com.example.pruebafastapiconbuscadorylikes.data.network
import com.example.pruebafastapiconbuscadorylikes.model.ApiResponse
import com.example.pruebafastapiconbuscadorylikes.model.IdsResponse
import com.example.pruebafastapiconbuscadorylikes.model.InteraccionesResponse
import com.example.pruebafastapiconbuscadorylikes.model.LikeRequest
import com.example.pruebafastapiconbuscadorylikes.model.LikeResponse
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import com.example.pruebafastapiconbuscadorylikes.model.ViewRequest
import retrofit2.http.*




interface ApiService {

    @GET("buscar_ids")
    suspend fun buscarRecetas(
        @Query("query") query: String
    ): IdsResponse

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

    @POST("receta/{receta_id}/view")
    suspend fun agregarVista(
        @Path("receta_id") recetaId: String,
        @Body viewRequest: ViewRequest
    ): ApiResponse

    @GET("usuario/{uid}/interacciones")
    suspend fun getInteracciones(
        @Path("uid")
        uid: String
    ): InteraccionesResponse
    @GET("recetas/{receta_id}")
    suspend fun obtenerRecetaPorId(
        @Path("receta_id") recetaId: String
    ): Receta

}