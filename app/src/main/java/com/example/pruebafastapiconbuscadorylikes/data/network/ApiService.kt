package com.example.pruebafastapiconbuscadorylikes.data.network
import com.example.pruebafastapiconbuscadorylikes.model.ApiResponse
import com.example.pruebafastapiconbuscadorylikes.model.Receta
import retrofit2.http.*

data class LikeRequest(val uid: String)
data class ViewRequest(val uid: String)
data class LikeResponse(val message: String)
data class IdsResponse(val ids: List<String>)
data class InteraccionesResponse(
    val vistas: List<Receta>,
    val likes: List<Receta>
)


interface ApiService {

    // Buscar recetas
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

    // Incrementar vistas
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

}