package com.example.pruebafastapiconbuscadorylikes.model

data class Receta(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val calorias: Int? = 0,
    val imagenUrl: String = "",
    val mainImageUrl: String = "",
    val ingrediente_principal: String = "",
    val ingredientes: List<Ingrediente> = listOf(),
    val pasos: List<Paso> = listOf(),
    val likes: Int = 0,
    val liked_by: MutableMap<String, Boolean> = mutableMapOf(),
    val popup_clicks: Int = 0,
    val porciones: Int? = 0,

    // 🔹 Campos de tiempo
    val tiempoPreparacion: Any? = null,
    val tiempoPrep: String? = null,
    val prepTimeText: String? = null,
    val views: Int? = 0,

    // 🔹 Campos del autor
    val authorUid: String? = null,
    val authorEmail: String? = null,
    val authorName: String? = null
) {
    // 🔹 Getter auxiliar para usar en la UI
    val tiempoFormateado: String
        get() = when {
            tiempoPreparacion is String -> tiempoPreparacion
            tiempoPrep != null -> tiempoPrep
            prepTimeText != null -> prepTimeText
            tiempoPreparacion is Number -> "${tiempoPreparacion} min"
            else -> "N/A"
        }.toString()

    // 🔹 Getter seguro para el nombre del autor
    val autorMostrado: String
        get() = when {
            !authorName.isNullOrBlank() -> authorName
            !authorEmail.isNullOrBlank() -> authorEmail.substringBefore("@")
            else -> "Autor desconocido"
        }
}



data class Paso(
    val descripcion: String = "",
    val imagenUrl: String = "",
    val orden: Int = 0
)

data class Ingrediente(
    var nombre: String? = null,
    var cantidad: String? = null,
    var unidad: String? = null
)


data class IngredienteSeleccionado(
    val ingrediente: Ingrediente,
    var precio: String = "",
    var cantidad: String = "",
    var unidad: String = ""
)
data class InteraccionesResponse(
    val vistas: List<Receta>,
    val likes: List<Receta>
)
data class LikeRequest(val uid: String)
data class ViewRequest(val uid: String)
data class LikeResponse(val message: String)
data class IdsResponse(val ids: List<String>)
