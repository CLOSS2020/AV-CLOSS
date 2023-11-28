package com.appcloos.mimaletin.sincronizar.dataClass.articulos

data class ArticuloResponse(
    val articulo: List<Articulo>,
    val status: String?
)