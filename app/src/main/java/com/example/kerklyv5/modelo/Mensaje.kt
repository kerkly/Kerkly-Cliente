package com.example.kerklyv5.modelo

class Mensaje(m: String, h: String, mensajeLeido: String){
    var mensaje = m
    var hora = h
    var tipo_usuario = "cliente"
    var mensajeLeido = mensajeLeido


    constructor(): this("", "","")

}