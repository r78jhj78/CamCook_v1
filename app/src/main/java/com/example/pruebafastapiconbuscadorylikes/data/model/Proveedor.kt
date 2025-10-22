package com.example.pruebafastapiconbuscadorylikes.data.model

data class Proveedor(
    val userId: String = "",
    val nombre: String = "",
    val tipoProducto: String = "",
    val descripcion: String = "",
    val imagen: String = ""
)
data class Producto(
    val nombre: String = "",
    val precio: String = "",
    val imagen: String = ""
)
