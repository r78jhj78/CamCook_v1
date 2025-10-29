package com.example.pruebafastapiconbuscadorylikes.model

data class Receta(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val calorias: Int? = 0,
    val imagen_final_url: String = "",
    val ingrediente_principal: String = "",
    val ingredientes: List<Ingrediente> = listOf(),
    val pasos: List<Paso> = listOf(),
    val likes: Int = 0,
    val liked_by: MutableMap<String, Boolean> = mutableMapOf(),
    val popup_clicks: Int = 0,
    val porciones: Int? = 0,
    val tiempoPreparacion: Any? = null,
    val views: Int? = 0
)

data class Ingrediente(
    var nombre: String? = null,
    var cantidad: String? = null,
    var unidad: String? = null
)

data class Paso(
    val descripcion: String = "",
    val imagen_url: String = "",
    val orden: Int = 0
)
data class IngredienteSeleccionado(
    val ingrediente: Ingrediente,
    var precio: String = "",
    var cantidad: String = "",
    var unidad: String = ""
)
