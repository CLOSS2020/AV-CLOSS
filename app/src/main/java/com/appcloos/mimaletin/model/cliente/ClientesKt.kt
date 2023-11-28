package com.appcloos.mimaletin.model.cliente


data class ClientesKt(
    var codigo: String = "", // 14206060
    var nombre: String = "", // CRISPIN RIOS
    var direccion: String = "", // MARACAIBO
    var telefonos: String = "", // 0424-6006030
    var perscont: String = "",
    var vendedor: String = "",
    var contribespecial: Double = 0.0, // 0
    var status: Double = 0.0, // 2
    var sector: String = "", // 01
    var subcodigo: String = "", // 01
    var fechamodifi: String = "", // 2023-08-16 15:46:12
    var precio: Double = 0.0, // 1
    var email: String = "",
    var kne_activa: String = "", // 0
    var kne_mtomin: Double = 0.0, // 0.0000000
    var noemifac: Int = 0, // 0
    var noeminota: Int = 0, // 0
    var fchultvta: String = "", // 0000-00-00
    var mtoultvta: Double = 0.0, // 0.000000
    var prcdpagdia: Double = 0.0, // 0.000000
    var promdiasp: Double = 0.0, // 0.000000
    var riesgocrd: Double = 0.0, // 0.000000
    var cantdocs: Double = 0.0, // 0.000000
    var totmtodocs: Double = 0.0, // 0.000000
    var prommtodoc: Double = 0.0, // 0.000000
    var diasultvta: Double = 0.0, // 0.000000
    var promdiasvta: Double = 0.0, // 0.000000
    var limcred: Double = 0.0, // 0.000000
    var fchcrea: String = "" // 0000-00-00
)