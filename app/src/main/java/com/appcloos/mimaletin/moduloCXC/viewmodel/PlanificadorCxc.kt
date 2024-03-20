package com.appcloos.mimaletin.moduloCXC.viewmodel

data class PlanificadorCxc(
    val doc: String = "",
    val cliente: String = "",
    val codcliente: String = "",
    val monto: Double = 0.0,
    val fechaVencimiento: String = "",
    val edoFact: String = "",
    val edoPedi: String = "",
    val fechaRecepcion: String = "",
    val diascred: Int = 0,
    val reclamo: Boolean = false,
    val dolarFlete: Boolean = false,
    val edoentrega: Int = 2
)
