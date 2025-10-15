package com.example.pruebafastapiconbuscadorylikes.data.model

data class Usuario(
    val nombre: String = "",
    val email: String = "",
    val roles: List<String> = listOf("usuario"),
    val likes: List<String> = emptyList(),
    val vistas: Map<String, Int> = emptyMap()
)

