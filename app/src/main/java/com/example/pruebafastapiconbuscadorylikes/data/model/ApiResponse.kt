package com.example.pruebafastapiconbuscadorylikes.model

data class ApiResponse(
    val query_original: String,
    val total_resultados: Int,
    val resultados: List<Receta>
)