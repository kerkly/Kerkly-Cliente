package com.example.kerklyv5.modelo.serial

class modeloSolicitudNormal(idPresupuesto: Int, pago_total: String, problema:String, correo: String, TipoServicio:String, idKerkly: String,clienteAcepta: Boolean, fechaHora: String, latitud: Double, longitud: Double,trabajoTerminado: Boolean) {
    var idPresupuesto: Int = idPresupuesto
    var pago_total: String = pago_total
    var problema : String = problema
    var correo : String = correo
    var TipoServicio : String = TipoServicio
    var idKerkly: String = idKerkly
    var clienteAcepta: Boolean = clienteAcepta
    var fechaHora:String = fechaHora
    var latitud: Double = latitud
    var longitud:Double = longitud
    var trabajoTerminado: Boolean =trabajoTerminado

    constructor() :this (0,"","","","","",false,"",0.0,0.0,false)
}