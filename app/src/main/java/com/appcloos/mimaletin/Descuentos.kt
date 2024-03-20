package com.appcloos.mimaletin

data class Descuentos(
    var nrodoc: String = "",
    var cantdscto: Double = 0.00,
    var pordscto: Double = 0.00,
    var isSelected: Boolean = false, // <-- ayuda a saber si el vendedor dio el descuento al doc
    var show: Boolean = true
)
