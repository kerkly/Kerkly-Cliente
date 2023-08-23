package com.example.kerklyv5.modelo

class modeloSolicituUrgente(idPresupuesto: Int, pago_total: String, problema:String, correo: String, TipoServicio:String, idKerklyAcepto: String, fechaHora: String, latitud: Double,longitud: Double) {
    var idPresupuesto: Int = idPresupuesto
    var pago_total: String = pago_total
    var problema : String = problema
    var correo : String = correo
    var TipoServicio : String = TipoServicio
    var idKerklyAcepto: String = idKerklyAcepto
    var fechaHora:String = fechaHora
    var latitud: Double = latitud
    var longitud:Double = longitud

    constructor() :this (0,"","","","","","",0.0,0.0)
}