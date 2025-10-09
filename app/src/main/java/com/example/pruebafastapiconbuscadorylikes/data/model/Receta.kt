package com.example.pruebafastapiconbuscadorylikes.model

data class Receta(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val ingredientes: List<Ingrediente> = listOf(),
    val likes: Int = 0,
    val popup_clicks: Int = 0,
    val liked_by: Map<String, Boolean> = emptyMap()
)

data class Ingrediente(
    var nombre: String? = null,
    var cantidad: String? = null,
    var unidad: String? = null
)

data class Paso(
    val descripcion: String,
    val imagen_url: String,
    val orden: Int
)