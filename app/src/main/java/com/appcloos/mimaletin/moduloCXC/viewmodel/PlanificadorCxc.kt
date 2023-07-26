package com.appcloos.mimaletin.moduloCXC.viewmodel

data class PlanificadorCxc(
    val doc: String,
    val cliente: String,
    val codcliente: String,
    val monto: Double,
    val fechaVencimiento: String,
    val edoFact: String,
    val edoPedi: String,
    val fechaRecepcion: String,
    val diascred: Int,
)
